package com.cvicse.leasing.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.cvicse.leasing.model.*;
import com.cvicse.leasing.repository.DocumentRepository;
//import com.cvicse.leasingauthmanage.model.User;
//import com.cvicse.leasingauthmanage.repository.UserRepository;
import com.google.common.collect.Lists;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;


@Service
public class DocumentService {
    @Autowired
    private DocumentRepository documentRepository;

//    @Autowired
//    private UserRepository userRepository;

    @Autowired
    private Javers javers;

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    public List<String> getCollections(){
        Set<String> collectionSet = documentRepository.findCollections();
        List<String> collections = new ArrayList<>();
        for(String collectionName: collectionSet){
            if(!collectionName.contains("jv_head_id")&&!collectionName.contains("jv_snapshots"))
                collections.add(collectionName);
        }
        return collections;
    }

    public Set<String> getDataKeys(String collectionName){
        return documentRepository.findDataItemsInCollection(collectionName);
    }

    public List<DocumentSearchFactor> getDocumentSearchFactors(JSONArray filterFactors){
        List<DocumentSearchFactor> documentSearchFactors = new ArrayList<>();
        for(int i=0;i<filterFactors.size();i++){
            JSONObject filterFactor = filterFactors.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
            documentSearchFactors.add(documentSearchFactor);
        }
        return documentSearchFactors;
    }

    /**
     * @param embeddedDocumentPath A(O/A).B(O/A).C(O/A)
     * @param documentSearchFactors
     * @return List<EmbeddedDocumentPath> : [A JSONObject {A.key,value} , A.B JSONArray {A.key,value} , ...]
     *
     */
    public List<EmbeddedDocumentPath> getEmbeddedDocumentPath(String embeddedDocumentPath, List<DocumentSearchFactor> documentSearchFactors){
        String[] locations =  embeddedDocumentPath.split("\\.");  //[A(O/A),B(O/A),C(O/A)]
        List<EmbeddedDocumentPath> embeddedDocumentPathList = new ArrayList<>();
        EmbeddedDocumentPath firstPath = new EmbeddedDocumentPath();
        String[] first = locations[0].split("\\(|\\)"); //[A,O/A]
        firstPath.setPath(first[0]);
        if(first[1].equals("O"))
            firstPath.setEmbeddedDocumentType(EmbeddedDocumentType.JSONObject);
        else firstPath.setEmbeddedDocumentType(EmbeddedDocumentType.JSONArray);  //path = A,type = JSONObject
        embeddedDocumentPathList.add(firstPath);
        for(int i=1;i<locations.length;i++){
            EmbeddedDocumentPath embeddedDocumentLocation = new EmbeddedDocumentPath();
            String[] tmp = locations[i].split("\\(|\\)");
            String s = first[0]+"."+tmp[0];
            embeddedDocumentLocation.setPath(s);
            first = tmp;
            if(tmp[1].equals("O"))
                embeddedDocumentLocation.setEmbeddedDocumentType(EmbeddedDocumentType.JSONObject);
            else embeddedDocumentLocation.setEmbeddedDocumentType(EmbeddedDocumentType.JSONArray);
            embeddedDocumentPathList.add(embeddedDocumentLocation);
        }
        for(int i = 0; i< embeddedDocumentPathList.size(); i++) {
            String[] tmp = documentSearchFactors.get(i).getMatchKey().split("\\.");
            String s = tmp[tmp.length-2]+"."+tmp[tmp.length-1];
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor();
            documentSearchFactor.setMatchKey(s);
            documentSearchFactor.setMatchValue(documentSearchFactors.get(i).getMatchValue());
            embeddedDocumentPathList.get(i).setDocumentSearchFactor(documentSearchFactor); //path = A,type = JSONObject documentSearchFactor{key="$A.key",value = "$value"}
        }
        return embeddedDocumentPathList;
    }

    /**
     *
     * @param collectionName
     * @param filterFactors
     * @param embeddedDocumentPath
     * @return
     */
    public List<JSONObject> getEmbeddedDocuments(String collectionName,JSONArray filterFactors,String embeddedDocumentPath ){
        List<AggregationOperation> operations = Lists.newArrayList();
        List<DocumentSearchFactor> documentSearchFactors = this.getDocumentSearchFactors(filterFactors);
        List<EmbeddedDocumentPath> embeddedDocumentPathList = this.getEmbeddedDocumentPath(embeddedDocumentPath,documentSearchFactors);
        for(DocumentSearchFactor documentSearchFactor:documentSearchFactors){
            Criteria criteria = Criteria.where(documentSearchFactor.getMatchKey())
                    .is(documentSearchFactor.getMatchValue());
            operations.add(Aggregation.match(criteria));
        }
        operations.add(Aggregation.match(Criteria.where("status").is(Status.Created)));  //select root document R

        for(EmbeddedDocumentPath documentLocation: embeddedDocumentPathList){
//            System.out.println(documentLocation.getPath()+" "+documentLocation.getEmbeddedDocumentType()+" "+documentLocation.getDocumentSearchFactor().getMatchKey()
//                    +" " +documentLocation.getDocumentSearchFactor().getMatchValue());
            ProjectionOperation projectionOperation = Aggregation.project(documentLocation.getPath()); // A.B
            operations.add(projectionOperation);
            if(documentLocation.getEmbeddedDocumentType().equals(EmbeddedDocumentType.JSONArray))  //if JSONArray then unwind
                operations.add(Aggregation.unwind(documentLocation.getPath().split("\\.")[1]));  //select B
            Criteria criteria = Criteria.where(documentLocation.getDocumentSearchFactor().getMatchKey())
                    .is(documentLocation.getDocumentSearchFactor().getMatchValue());  // filter under B
            operations.add(Aggregation.match(criteria));
        }
        return documentRepository.findEmbeddedDocumentByAggregationOperations(collectionName,operations);
    }

    /**
     * 获取某个Collecton下的所有Documents
     * @param collectionName
     * @return
     */
    public List<Document> getDocumentsInCollection(String collectionName){
        logger.info("get all documents by collectionName.");
        return documentRepository.findAllDocumentsInCollection(collectionName);
    }

    /**
     * 获取某个Collecton下的所有Documents  分页
     * @param collectionName
     * @param filters  pageNumber,pageSize,queryFields
     * @return
     */
    public JSONObject getDocumentsPagination(String collectionName,JSONObject filters){
        logger.info("get all documents by collectionName.");
        return documentRepository.findAllDocumentsPagination(collectionName,filters);
    }

    /**
     * 查询问题列表中有返回对比结果的计划集合信息
     * @param collectionName
     * @return
     */
    public JSONArray findQuestionCollection(String collectionName){
        logger.info("get all documents by collectionName.");
        return documentRepository.findDocumentContentByCollection(collectionName);
    }

    /**
     * 查询测试报告中可以展示测试报告（reprotFlag为1）的计划
     * @param collectionName
     * @return
     */
    public JSONArray findTestReportList(String collectionName){
        logger.info("get all documents by collectionName.");
        return documentRepository.findTestReportList(collectionName);
    }

    /**
     * 根据id和collection获取某个具体的Document
     * @param id
     * @param collectionName
     * @return
     */
    public Document getDocumentByIdInCollection(String id,String collectionName){
        logger.info("get document by id and collectionName"+id);
        return documentRepository.findDocumentByIdInCollection(id,collectionName);
//        return this.mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)),Document.class,collectionName);
    }

    /**
     * 聚合查询 获取完整的Document
     * @param id
     * @param collectionName
     * @return
     */
    public JSONObject getDocumentByHierarchicalQueries(String id, String collectionName, Integer level,JSONArray filterFactors){
        logger.info("hierarchicalQueries");
        if(this.getDocumentByIdInCollection(id,collectionName)==null){
            return null;
        }
        List<LookupOperation> lookupOperations = new ArrayList<>();
        for(int i=0;i<filterFactors.size();i++){
            JSONObject filterFactor = filterFactors.getJSONObject(i);
            System.out.println(filterFactor);
            HierarchicalFactor hierarchicalFactor = new HierarchicalFactor(filterFactor);
            LookupOperation lookupOperation = LookupOperation.newLookup().from(hierarchicalFactor.getCollectionName())
                    .localField(hierarchicalFactor.getLocalParam())
                    .foreignField(hierarchicalFactor.getForeignParam())
                    .as(hierarchicalFactor.getAs());
            lookupOperations.add(lookupOperation);
        }
        return documentRepository.findDocumentByIdWithLookUpOperations(id,collectionName,level,lookupOperations);
    }


    /**
     * 根据特定的查询条件 返回查询信息
     * @param id
     * @param collectionName
     * @param filters
     * @return
     */
    public JSONObject getDocumentContents(String id, String collectionName,JSONArray filters){
        JSONObject jsonObject = new JSONObject();
        for(int i=0;i<filters.size();i++){
            JSONObject filterFactor = filters.getJSONObject(i);
            logger.info(filterFactor.getString("content"));
            ProjectionOperation projectionOperation = Aggregation.project(filterFactor.getString("content"));
            jsonObject.put(filterFactor.getString("content")
                    ,documentRepository.findDocumentContentByIdAndProjectionOperation(id,collectionName,projectionOperation));
        }
        return jsonObject;
    }

    public List<Document> getDocumentsByCriteriaList(JSONArray filters,String collectionName){
        List<Criteria> criteriaList = new ArrayList<>();
        for(int i=0;i<filters.size();i++){
            JSONObject filterFactor = filters.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
            Criteria criteria = Criteria.where(documentSearchFactor.getMatchKey())
                    .is(documentSearchFactor.getMatchValue());
            criteriaList.add(criteria);
        }
        return documentRepository.findDocumentsByCriterias(criteriaList,collectionName);
    }

    /**
     * 传入两个版本名，查询对应版本下用例，比较得到相同用例
     * @param filters
     * @param collectionName
     * @return
     */
    public JSONArray getSameTestcasesByVersion(JSONArray filters,String collectionName){
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("data.sdk_version").is(filters.getString(0)));//查询第一个版本信息
        List<Document> version1 = documentRepository.findDocumentsByCriterias(criteriaList,collectionName);
        JSONArray interfaces1 = version1.get(0).getData().getJSONArray("interfaces");
        JSONArray methods1 = new JSONArray();
        JSONArray testcase1 = new JSONArray();
        for (int i = 0; i < interfaces1.size(); i++) {//若接口下无方法或者该方法为空，则跳过，不进行加入操作
            if(interfaces1.getJSONObject(i).size()<3||interfaces1.getJSONObject(i).getJSONArray("methods")==null){
                continue;
            }
            methods1.addAll(interfaces1.getJSONObject(i).getJSONArray("methods"));
        }
        for (int j = 0; j < methods1.size(); j++) {//若方法下无用例或者该用例为空，则跳过，不进行加入操作
            if(methods1.getJSONObject(j).size()<3||methods1.getJSONObject(j).getJSONArray("testcases")==null){
                continue;
            }
            testcase1.addAll(methods1.getJSONObject(j).getJSONArray("testcases"));
        }
        criteriaList.remove(0);//移除第一个条件
        criteriaList.add(Criteria.where("data.sdk_version").is(filters.getString(1)));//查询第二个版本信息
        List<Document> version2 = documentRepository.findDocumentsByCriterias(criteriaList,collectionName);
        JSONArray interfaces2 = version2.get(0).getData().getJSONArray("interfaces");
        JSONArray methods2 = new JSONArray();
        JSONArray testcase2 = new JSONArray();
        for (int i = 0; i < interfaces2.size(); i++) {
            if(interfaces2.getJSONObject(i).size()<3||interfaces2.getJSONObject(i).getJSONArray("methods")==null){
                continue;
            }
            methods2.addAll(interfaces2.getJSONObject(i).getJSONArray("methods"));
        }
        for (int j = 0; j < methods2.size(); j++) {
            if(methods2.getJSONObject(j).size()<3||methods2.getJSONObject(j).getJSONArray("testcases")==null){
                continue;
            }
            testcase2.addAll(methods2.getJSONObject(j).getJSONArray("testcases"));
        }
        testcase1.retainAll(testcase2);//用例的去重<<--------------------------------------------------
        JSONArray returnMethods = new JSONArray();
        String methodsName;
        boolean flag = true;//标志，方法名是否已存在。true：不存在
        for (int k = 0; k < testcase1.size(); k++) {
            methodsName = testcase1.getJSONObject(k).getString("case_name").split("-")[0];
            for (int i = 0; i < returnMethods.size(); i++) {//遍历方法数组，判断方法名是否存在
                String returnMethodName = returnMethods.getJSONObject(i).getString("method_name");
                if(methodsName.equals(returnMethodName)){//如果有同名方法，就把当前用例放到该方法的用例数组里
                    returnMethods.getJSONObject(i).getJSONArray("testcases").add(testcase1.getJSONObject(k));
                    flag = false;
                }
            }
            if(flag){////如果没有同名方法，则新增一个方法数组元素并且将方法名和用例数组放入
                JSONObject newMethod = new JSONObject();
                newMethod.put("method_name",methodsName);
                JSONArray testcaseArray = new JSONArray();
                testcaseArray.add(testcase1.getJSONObject(k));
                newMethod.put("testcases",testcaseArray);
                returnMethods.add(newMethod);
            }
        }
        return returnMethods;
    }

    /**
     * 对比结果中查询问题列表信息
     * @param filters
     * @param collectionName
     * @return
     */
    public JSONArray getResultDocumentsByCriteriaList(JSONArray filters,String collectionName){
        List<Criteria> criteriaList = new ArrayList<>();
        for(int i=0;i<filters.size();i++){
            JSONObject filterFactor = filters.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
            Criteria criteria = Criteria.where(documentSearchFactor.getMatchKey()).is(documentSearchFactor.getMatchValue());
            criteriaList.add(criteria);
        }
        List<ResultDocument> list = documentRepository.findResultDocumentsByCriterias(criteriaList,collectionName);
        JSONArray questionList =  new JSONArray();
        if(list!=null && !list.isEmpty()){
            if(list.get(0).getError_msg()!=null){
                JSONObject errorMsg = new JSONObject();
                errorMsg.put("type","DataTooLarge");
                errorMsg.put("errorMsg",list.get(0).getError_msg());
                questionList.add(errorMsg);
            }else{
                JSONArray mismatchArray = list.get(0).getMismatch();
                JSONArray errorArray = list.get(0).getError();
                JSONArray emptyArray = list.get(0).getEmpty();
                JSONArray resultFalseArray = list.get(0).getResult().getJSONArray("false");//result中为false的数组
                JSONArray resultArray = new JSONArray();
                resultArray.add(mismatchArray);
                resultArray.add(errorArray);
                resultArray.add(emptyArray);
                resultArray.add(resultFalseArray);
                JSONArray detailType = list.get(0).getDetailType();//对比环境或者版本的标识;
                if(list.get(0).getResult().size()>2){//则为排序类型结果
                    JSONArray resultSort1UnknownArray = list.get(0).getResult().getJSONObject("sort1").getJSONArray("unknown");//result中为sort1下为unknown的数组
                    JSONArray resultSort2UnknownArray = list.get(0).getResult().getJSONObject("sort2").getJSONArray("unknown");//result中为sort2下为unknown的数组
                    JSONArray resultSort1FalseArray = list.get(0).getResult().getJSONObject("sort1").getJSONArray("false");//result中为sort1下为false的数组
                    JSONArray resultSort2FalseArray = list.get(0).getResult().getJSONObject("sort2").getJSONArray("false");//result中为sort2下为false的数组
                    resultArray.add(resultSort1UnknownArray);
                    resultArray.add(resultSort2UnknownArray);
                    resultArray.add(resultSort1FalseArray);
                    resultArray.add(resultSort2FalseArray);
                }
                String id = list.get(0).get_id();
                String quoteDetail = list.get(0).getQuoteDetail();//0基准1行情  排序2
                String runnerID1 = list.get(0).getRunnerID1();//获取runnerID，根据传入顺序确定对应的环境，1就是第一个传入的，2是后面传入的
                String runnerID2 = list.get(0).getRunnerID2();
                JSONObject conditions = new JSONObject();
                conditions.put("id",id);
                conditions.put("quoteDetail",quoteDetail);
                conditions.put("detailType",detailType);
                conditions.put("runnerID1",runnerID1);
                conditions.put("runnerID2",runnerID2);
                questionList = getResultInfor(resultArray,conditions);//抽成公共方法，取值放值
            }

        }
        return questionList;
    }

    /**
     * 多种比对类型信息查询过滤方法
     * @param resultArray
     * @param conditions
     * @return
     */
    public JSONArray getResultInfor(JSONArray resultArray,JSONObject conditions){
        JSONArray questionList = new JSONArray();
        String id = conditions.getString("id");
        String quoteDetail = conditions.getString("quoteDetail");//比对类型 0基准 1行情 2排序
        JSONArray detailType = conditions.getJSONArray("detailType");//两边环境标志
        String runnerID1 = conditions.getString("runnerID1");//用于判断error，empty，mismatch错误中出错环境信息
        String runnerID2 = conditions.getString("runnerID2");//1,2按照计划传入顺序决定
        JSONObject paramData;//用例参数
        String testcaseID1;//方法名
        JSONArray details;//详细信息
        String recordID;//recordID，后续更新状态需要
        String status;//状态：默认0，确认1，忽略2
        //行情结果查询字段
        JSONArray missTime;//两边错过的datetime
        int numbers;//datetime的总条数
        String missRate;//datetime错过率
        String matchRate;//datetime正确率
        String errorRate;//datetime错误率
        JSONArray result;//每个用例中的详细信息
        String dateTime;//行情执行的时间
        String errorMsg = "";//排序每个用例报错信息
        String runnerID = "";//用例出错信息中的runnerID，标志环境信息
        if("0".equals(quoteDetail)){//基准比较的值查询
            for(int i=0;i<resultArray.size();i++){//获取错误类型数组中的一个，数组
                JSONArray eachArray = resultArray.getJSONArray(i);
                if(eachArray.size()!=0){
                    for(int j=0;j<eachArray.size();j++){
                        Map<String,Object> eachQuestionMap = new HashMap<>();
                        paramData = eachArray.getJSONObject(j).getJSONObject("paramData");
                        testcaseID1 = eachArray.getJSONObject(j).getString("testcaseID");
                        recordID = eachArray.getJSONObject(j).getString("recordID");
                        status = eachArray.getJSONObject(j).getString("status");
                        if(i!=3){
                            runnerID = eachArray.getJSONObject(j).getString("runnerID");
                            if(runnerID.equals(runnerID1)){
                                eachQuestionMap.put("environment",detailType.getString(0));
                            }else{
                                eachQuestionMap.put("environment",detailType.getString(1));
                            }
                        }
                        if(i==0){
                            eachQuestionMap.put("type","mismatch");
                        }else if(i==1){
                            eachQuestionMap.put("type","error");
                        }else if(i==2){
                            eachQuestionMap.put("type","empty");
                        }else{
                            eachQuestionMap.put("type","false");
                            recordID = eachArray.getJSONObject(j).getString("recordID1");
                            testcaseID1 = eachArray.getJSONObject(j).getString("testcaseID1");
                            paramData = eachArray.getJSONObject(j).getJSONObject("paramData1");
                            details = eachArray.getJSONObject(j).getJSONArray("details");
                            eachQuestionMap.put("details",details);
                        }
                        eachQuestionMap.put("status",status);
                        eachQuestionMap.put("id",id);
                        eachQuestionMap.put("paramData",paramData);
                        eachQuestionMap.put("testcaseID",testcaseID1);
                        eachQuestionMap.put("recordID",recordID);
                        eachQuestionMap.put("quoteDetail",quoteDetail);
                        eachQuestionMap.put("detailType",detailType);
                        questionList.add(eachQuestionMap);
                    }
                }
            }
        }else if("1".equals(quoteDetail)){//行情比较的查询
            for(int i=0;i<resultArray.size();i++){//获取错误类型数组中的一个，数组
                JSONArray eachArray = resultArray.getJSONArray(i);
                if(eachArray.size()!=0){
                    for(int j=0;j<eachArray.size();j++){
                        JSONObject eachQuestionMap =new JSONObject();
                        JSONArray questionDetails = new JSONArray();//存所有details和datetime的数组
                        paramData = eachArray.getJSONObject(j).getJSONObject("paramData");
                        testcaseID1 = eachArray.getJSONObject(j).getString("testcaseID");
                        recordID = eachArray.getJSONObject(j).getString("recordID");
                        status = eachArray.getJSONObject(j).getString("status");
                        missTime = eachArray.getJSONObject(j).getJSONArray("miss_time");
                        numbers = eachArray.getJSONObject(j).getIntValue("numbers");
                        missRate = eachArray.getJSONObject(j).getString("miss_rate");
                        matchRate = eachArray.getJSONObject(j).getString("match_rate");
                        errorRate = eachArray.getJSONObject(j).getString("error_rate");
                        result = eachArray.getJSONObject(j).getJSONArray("result");
                        for(int z=0;z<result.size();z++){
                            JSONObject questionDetail = new JSONObject();//存details和datetime
                            details = result.getJSONObject(z).getJSONArray("details");
                            dateTime = result.getJSONObject(z).getString("datetime");
                            questionDetail.put("details",details);
                            questionDetail.put("dateTime",dateTime);
                            questionDetails.add(questionDetail);
                        }
                        if(i==0){
                            eachQuestionMap.put("type","mismatch");
                        }else if(i==1){
                            eachQuestionMap.put("type","error");
                        }else if(i==2){
                            eachQuestionMap.put("type","empty");
                        }else{
                            eachQuestionMap.put("type","false");
                        }
                        if(i!=3){
                            runnerID = eachArray.getJSONObject(j).getString("runnerID");
                            if(runnerID.equals(runnerID1)){
                                eachQuestionMap.put("environment",detailType.getString(0));
                            }else{
                                eachQuestionMap.put("environment",detailType.getString(1));
                            }
                        }
                        eachQuestionMap.put("id",id);
                        eachQuestionMap.put("quoteDetail",quoteDetail);
                        eachQuestionMap.put("paramData",paramData);
                        eachQuestionMap.put("testcaseID",testcaseID1);
                        eachQuestionMap.put("recordID",recordID);
                        eachQuestionMap.put("status",status);
                        eachQuestionMap.put("missTime",missTime);
                        eachQuestionMap.put("number",numbers);
                        eachQuestionMap.put("missRate",missRate);
                        eachQuestionMap.put("matchRate",matchRate);
                        eachQuestionMap.put("errorRate",errorRate);
                        eachQuestionMap.put("questionDetails",questionDetails);
                        eachQuestionMap.put("detailType",detailType);
                        questionList.add(eachQuestionMap);
                    }
                }
            }
        }else{//排序
            for(int i=0;i<resultArray.size();i++){//获取错误类型数组中的一个，数组
                JSONArray eachArray = resultArray.getJSONArray(i);
                if(eachArray.size()!=0){
                    for(int j=0;j<eachArray.size();j++){
                        Map<String,Object> eachQuestionMap = new HashMap<>();
                        paramData = eachArray.getJSONObject(j).getJSONObject("paramData");
                        testcaseID1 = eachArray.getJSONObject(j).getString("testcaseID");
                        recordID = eachArray.getJSONObject(j).getString("recordID");
                        status = eachArray.getJSONObject(j).getString("status");
                        if(i==0){
                            eachQuestionMap.put("type","mismatch");
                        }else if(i==1){
                            eachQuestionMap.put("type","error");
                        }else if(i==2){
                            eachQuestionMap.put("type","empty");
                        }else if(i==3){
                            eachQuestionMap.put("type","false");
                            recordID = eachArray.getJSONObject(j).getString("recordID1");
                            testcaseID1 = eachArray.getJSONObject(j).getString("testcaseID1");
                            paramData = eachArray.getJSONObject(j).getJSONObject("paramData1");
                            details = eachArray.getJSONObject(j).getJSONArray("details");
                            eachQuestionMap.put("details",details);
                        }else if(i==4){
                            eachQuestionMap.put("type","sort1_unknown");//sort1_unknown
                            eachQuestionMap.put("environment",detailType.getString(0));
                        }else if(i==5){
                            eachQuestionMap.put("type","sort2_unknown");//sort2_unknown
                            eachQuestionMap.put("environment",detailType.getString(1));
                        }else if(i==6){
                            eachQuestionMap.put("type","sort1_false");//sort1_false
                            eachQuestionMap.put("environment",detailType.getString(0));
                        }else{
                            eachQuestionMap.put("type","sort2_false");//sort2_false
                            eachQuestionMap.put("environment",detailType.getString(1));
                        }
                        if(i<3){
                            runnerID = eachArray.getJSONObject(j).getString("runnerID");
                            if(runnerID.equals(runnerID1)){
                                eachQuestionMap.put("environment",detailType.getString(0));
                            }else{
                                eachQuestionMap.put("environment",detailType.getString(1));
                            }
                        }
                        errorMsg = eachArray.getJSONObject(j).getString("error_msg");
                        if(!"".equals(errorMsg)&&errorMsg!=null){
                            String[] errorMsgArr = errorMsg.split("\\s+");//截取errorMsg中出错的字段值
                            eachQuestionMap.put("errorMsg",errorMsgArr[1]);
                        }
                        eachQuestionMap.put("status",status);
                        eachQuestionMap.put("id",id);
                        eachQuestionMap.put("paramData",paramData);
                        eachQuestionMap.put("testcaseID",testcaseID1);
                        eachQuestionMap.put("recordID",recordID);
                        eachQuestionMap.put("quoteDetail",quoteDetail);
                        eachQuestionMap.put("detailType",detailType);
                        questionList.add(eachQuestionMap);
                    }
                }
            }
        }
        return questionList;
    }

    /**
     * 测试报告页面查询（包含计划集合，对比结果集合）
     * @param filters
     * @param collectionName  默认collectionName是testResult集合，testPlan集合需要自己赋值
     * @return
     */
    public List<Map<String,Object>> getTestReport(JSONArray filters,String collectionName){
        List<Criteria> criteriaList = new ArrayList<>();
        List<Map<String,Object>> reportList =  new ArrayList<Map<String,Object>>();//返回的总List
        Map<String,Object> reportMap =new HashMap<String,Object>();
        JSONObject interfaceMap = new JSONObject();//有错误的接口及接口中错误用例数
        JSONObject testcaseMap = new JSONObject();//已确认bug的用例参数
        JSONArray methodArray = new JSONArray();//有错误的方法名数组
        /*---------------------查询对比结果集合-------------------------*/
        for(int i=0;i<filters.size();i++){
            JSONObject filterFactor = filters.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
            Criteria criteria = Criteria.where(documentSearchFactor.getMatchKey()).is(documentSearchFactor.getMatchValue());
            criteriaList.add(criteria);
        }
        List<ResultDocument> list = documentRepository.findResultDocumentsByCriterias(criteriaList,collectionName);
        String planID = "";
        if(list!=null && !list.isEmpty()){
            JSONArray mismatchArray = list.get(0).getMismatch();
            JSONArray errorArray = list.get(0).getError();
            JSONArray emptyArray = list.get(0).getEmpty();
            JSONArray resultFalseArray = list.get(0).getResult().getJSONArray("false");//result中为false的数组
            JSONArray resultArray = new JSONArray();
            resultArray.add(mismatchArray);
            resultArray.add(errorArray);
            resultArray.add(emptyArray);
            resultArray.add(resultFalseArray);
            if(list.get(0).getResult().size()>2){//则为排序类型结果
                JSONArray resultSort1FalseArray = list.get(0).getResult().getJSONObject("sort1").getJSONArray("fasle");//result中sort1中的false数组
                JSONArray resultSort1UnknownArray = list.get(0).getResult().getJSONObject("sort1").getJSONArray("unknown");//result中sort1中的unknown数组
                JSONArray resultSort2FalseArray = list.get(0).getResult().getJSONObject("sort2").getJSONArray("fasle");//result中sort2中的false数组
                JSONArray resultSort2UnknownArray = list.get(0).getResult().getJSONObject("sort2").getJSONArray("unknown");//result中sort2中的unknown数组
                resultArray.add(resultSort1FalseArray);
                resultArray.add(resultSort1UnknownArray);
                resultArray.add(resultSort2FalseArray);
                resultArray.add(resultSort2UnknownArray);
            }
            String id = list.get(0).get_id();
            String quoteDetail = list.get(0).getQuoteDetail();//0基准1行情  排序待定
            planID = list.get(0).getPlanID();//计划ID
            //行情，基准的报告信息查询方法
            JSONObject reportInfo = getReportInfo(resultArray,quoteDetail);
            Set methodSetFromInfo = new HashSet(Arrays.asList(reportInfo.getJSONArray("methodSet")));
            methodArray = JSONArray.parseArray(methodSetFromInfo.iterator().next().toString());//错误的方法名数组
            JSONArray bugList = reportInfo.getJSONArray("bugList");//缺陷列表
            interfaceMap = reportInfo.getJSONObject("interfaceMap");
            testcaseMap = reportInfo.getJSONObject("testcaseMap");//已确认bug的错误用例map
            reportMap.put("bugList",bugList);//将缺陷列表放入结果map中
            reportMap.put("interfaceErrorMap",interfaceMap);//将接口已确认错误用例数放入结果map中-------柱状图
        }
        /*---------------------查询计划集合-------------------------*/
        List<Criteria> criteriaList2 = new ArrayList<>();
        for(int i=0;i<filters.size();i++){
            JSONObject filterFactor = filters.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
            Criteria criteria = Criteria.where(("_id")).is(planID);
            criteriaList2.add(criteria);
        }
        collectionName = "testPlan";
        List<Document> planList = documentRepository.findDocumentsByCriterias(criteriaList2,collectionName);
        if(planList!=null && !planList.isEmpty()){
            JSONObject data = planList.get(0).getData();
            String planName = data.getString("plan_name");
            String planType = data.getString("plan_type");
            //环境 0测试 1全真 2生产
            String environment1 = data.getJSONObject("tp_environment1").getString("environment");
            String environment2 = data.getJSONObject("tp_environment2").getString("environment");
            String environment = environment1 +"," +environment2;
            String sdkVersion = data.getString("sdk_version");
            String startTime = data.getString("start_time");
            String runTimes = data.getString("run_times");
            JSONArray methods = data.getJSONArray("methods");
            int planTestcases = 0;//计划所传的用例总数
            Set planInterfacesSet = new HashSet();
            Set planMethodsSet = new HashSet();
            for(int i=0;i<methods.size();i++){
                String methodName = methods.getJSONObject(i).getString("method_name");
                planMethodsSet.add(methodName);
                String interfaceName = methodName.substring(0,methodName.lastIndexOf("_"));//根据方法名截取接口名
                planInterfacesSet.add(interfaceName);
                planTestcases += methods.getJSONObject(i).getJSONArray("testcases").size();
            }
            reportMap.put("planName",planName);
            reportMap.put("planType",planType);
            reportMap.put("environment",environment);
            reportMap.put("startTime",startTime);
            reportMap.put("sdkVersion",sdkVersion);
            reportMap.put("runTimes",runTimes);
            /*---------------------版本信息集合查询，统计图数据计算-------------------------*/
            //查询计算本次版本下的方法数和用例数
            List<Criteria> criteriaList3 = new ArrayList<>();
            Criteria criteria = Criteria.where(("data.sdk_version")).is(sdkVersion);
            criteriaList3.add(criteria);
            collectionName = "testInformation";
            List<Document> versionList = documentRepository.findDocumentsByCriterias(criteriaList3,collectionName);
            JSONArray interfacesList = versionList.get(0).getData().getJSONArray("interfaces");
            int versionMethods = 0;//该版本下的方法总数
            int versionTestcases = 0;//该版本下的用例总数
            for(int i=0;i<interfacesList.size();i++){
                JSONArray methodsList = interfacesList.getJSONObject(i).getJSONArray("methods");
                if(methodsList!=null){
                    versionMethods+= methodsList.size();
                    for(int j=0;j<methodsList.size();j++){
                        JSONArray testcaseList = methodsList.getJSONObject(j).getJSONArray("testcases");
                        if(testcaseList!=null){
                            versionTestcases+= testcaseList.size();
                        }
                    }
                }
            }
            //本次计划执行用例数，方法数，接口数，全部从计划集合中得到而不是从结果集合
            //接口覆盖率  本次接口总数/本版本接口总数----------------
            int thisInterfaces = planInterfacesSet.toArray().length;
            JSONArray interfaceCoverRate = new JSONArray();
            interfaceCoverRate.add(thisInterfaces);
            interfaceCoverRate.add(interfacesList.size());//版本下接口数
            reportMap.put("interfaceCoverRate",interfaceCoverRate);
            //接口通过率 （本次接口总数-出错接口数）/本次接口总数
            int interfacesNotPass = interfaceMap.size();//先算错误未通过的个数
            int interfacesPass = thisInterfaces - interfacesNotPass;//通过的方法个数
            JSONArray interfacePassRate = new JSONArray();
            interfacePassRate.add(interfacesPass);
            interfacePassRate.add(thisInterfaces);
            reportMap.put("interfacePassRate",interfacePassRate);
            //方法覆盖率：  本次计划总方法数/本版本总方法数--------------------
            int thisMethods = planMethodsSet.toArray().length;
            JSONArray methodCoverRate = new JSONArray();
            methodCoverRate.add(thisMethods);
            methodCoverRate.add(versionMethods);
            reportMap.put("methodCoverRate",methodCoverRate);//方法覆盖率
            //方法通过率：  （本次计划总方法数-本次计划方法错误数）/本次计划总方法数
            int methodsNotPass = methodArray.size();//先算错误未通过的个数
            int methodPass = thisMethods - methodsNotPass;//通过的方法个数
            JSONArray methodPassRate = new JSONArray();
            methodPassRate.add(methodPass);
            methodPassRate.add(thisMethods);
            reportMap.put("methodPassRate",methodPassRate);
            //用例覆盖率  本次计划用例个数/本版本总用例数---------------------
            //int thisTestcases = ((comparedFalseArray.size()+comparedTrueArray.size())*2+emptyArray.size()+mismatchArray.size()+errorArray.size())/2;//本次计划总用例数
            JSONArray testcaseCoverRate = new JSONArray();
            testcaseCoverRate.add(planTestcases);
            testcaseCoverRate.add(versionTestcases);
            reportMap.put("testcaseCoverRate",testcaseCoverRate);
            //用例通过率  （本次计划总用例数-本次计划用例错误数）/本次计划使用总用例数
            int testcaseNotPass = testcaseMap.size();
            int testcasePass = planTestcases - testcaseNotPass;
            JSONArray testcasePassRate = new JSONArray();
            testcasePassRate.add(testcasePass);
            testcasePassRate.add(planTestcases);
            reportMap.put("testcasePassRate",testcasePassRate);
            //各接口已确认错误用例数  柱状图--------查询对比结果中已计算  interfaceMap
        }
        //将map放入一个list中，可能业务扩展需求
        reportList.add(reportMap);
        return reportList;
    }

    /**
     * //行情，基准，排序的报告信息查询方法
     * @param resultArray
     * @param quoteDetail
     * @return
     */
    public JSONObject getReportInfo(JSONArray resultArray,String quoteDetail){
        JSONObject reportMapInfo = new JSONObject();
        JSONObject paramData;//用例参数
        String testcaseID;//方法名
        String status;//状态：默认0，确认1，忽略2
        String bugDescribe;//bug描述
        JSONObject interfaceMap = new JSONObject();//有已确认bug的接口数和已确认的bug数（不重复）
        JSONObject testcaseMap = new JSONObject();//已确认bug的错误用例（不重复）
        JSONArray bugList =  new JSONArray();//缺陷列表
        Set methodSet = new HashSet();
        for(int i=0;i<resultArray.size();i++){//获取错误类型数组中的一种数组
            JSONArray eachArray = resultArray.getJSONArray(i);
            if(eachArray!=null&&eachArray.size()!=0){
                for(int j=0;j<eachArray.size();j++){//遍历每种错误类型数组
                    Map<String,Object> questionMap =new HashMap<String,Object>();
                    if("0".equals(quoteDetail)&&i==3) {//基准比较且为false错误类型
                        paramData = eachArray.getJSONObject(j).getJSONObject("paramData1");
                        testcaseID = eachArray.getJSONObject(j).getString("testcaseID1");
                    }else{
                        testcaseID = eachArray.getJSONObject(j).getString("testcaseID");
                        paramData = eachArray.getJSONObject(j).getJSONObject("paramData");
                    }
                    bugDescribe = eachArray.getJSONObject(j).getString("bugDescribe");
                    status = eachArray.getJSONObject(j).getString("status");
                    //将paramData转换为有序字符串，用于接下来的比对
                    String paramDataStr = JSONObject.toJSONString(paramData, SerializerFeature.SortField.MapSortField);
                    String interfaceName = testcaseID.substring(0,testcaseID.lastIndexOf("_"));//根据方法名截取接口名
                    if(("1").equals(status)){//只有确认了bug的方法和接口才算入错误个数
                        /**
                         *1.判断用例是否出现在用例JSONObject的key中，存在就忽略，不需要计入用例方法接口数。不存在就将其
                         * 放入用例JSONObject中。
                         * 2.只有用例未出现过，计算它的方法接口数才有意义，反之则无需计算
                         * 3.第二步在未出现的用例判断体内将该用例方法名放入Set集合去重，计数
                         * 4.第三步在未出现的用例判断体内将该用例出现次数和接口名存入接口map
                         */
                        if(!testcaseMap.containsKey(paramDataStr)){
                            testcaseMap.put(paramDataStr,"1");
                            methodSet.add(testcaseID);
                            if(interfaceMap.containsKey(interfaceName)){//遍历判断所有的接口名，已经存在就加1，不存在就给初始值1
                                int val = (Integer.parseInt(interfaceMap.get(interfaceName).toString())+1);
                                interfaceMap.put(interfaceName, val+"");
                            }else{
                                interfaceMap.put(interfaceName, "1");
                            }
                        }
                        questionMap.put("paramStr",paramData);
                        questionMap.put("testcaseID",testcaseID);
                        questionMap.put("bugDescribe",bugDescribe);
                        bugList.add(questionMap);
                    }else{
                        continue;
                    }

                }
            }
        }
        reportMapInfo.put("bugList",bugList);
        reportMapInfo.put("methodSet",methodSet.toArray());
        reportMapInfo.put("interfaceMap",interfaceMap);
        reportMapInfo.put("testcaseMap",testcaseMap);
        return reportMapInfo;
    }

    /**
     * 根据schemaId和内容创建一个新的document
     * @param schemaId
     * @param content
     * @return
     */
    @Transactional
    public Document createDocument(String schemaId,JSONObject content){
        logger.info("create new document by schemaId "+schemaId);
//        User user = userRepository.findUserByUsername(SecurityContextHolder.getContext()
//                .getAuthentication().getName());
        Document document = new Document();
        document.setSchemaId(schemaId);
        document.setData(content);
        document.setStatus(Status.Created);
        document.setCollectionName(content.getString("title"));

//        document.setUserId(String.valueOf(user.getId()));
//        document.setOrganizationId(String.valueOf(user.getOrganizationId()));

        Document newDoc =  documentRepository.createDocument(document);
        javers.commit(newDoc.getId(),newDoc);
        return newDoc;
    }

    /**
     * 根据内容创建一个新的document
     * @return
     */
    @Transactional
    public ResultDocument createResultDocument(ResultDocument result){
        ResultDocument newDoc =  documentRepository.createResultDocument(result);
        javers.commit(newDoc.get_id(),newDoc);
        return newDoc;
    }

    /**
     * 更新Docuemnt
     * @param id
     * @param collectionName
     * @param content
     * @return
     */
    public Document updateDocument(String id,String collectionName,JSONObject content){
        logger.info("update document by id.");
        Document document = documentRepository.findDocumentByIdInCollection(id,collectionName);
        document.setData(content);
        Document newDoc = documentRepository.saveDocument(document);
        javers.commit(newDoc.getId(),newDoc);
        return newDoc;
    }

    /**
     * 更新删除状态
     * @param id
     * @param collectionName
     */
    public Document deleteDocument(String id,String collectionName){
        Document document = documentRepository.findDocumentByIdInCollection(id,collectionName);
        return documentRepository.deleteDocument(document);
    }

    public JSONArray trackDocumentChangesWithJavers(String documentId,String collectionName) {
        Document document = this.documentRepository.findDocumentByIdInCollection(documentId,collectionName);
        JSONArray jsonArray = new JSONArray();
        JqlQuery jqlQuery = QueryBuilder.byInstance(document).build();
        List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery);
        for (CdoSnapshot snapshot : snapshots) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("commitId", snapshot.getCommitId().getMajorId());
            jsonObject.put("commitDate", snapshot.getCommitMetadata().getCommitDate());
            jsonObject.put("data", JSON.parseObject(javers.getJsonConverter().toJson(snapshot.getState()), Document.class));
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public JSONObject getDocumentWithJaversCommitId(String documentId,String collectionName, String commitId){
        Document document = this.documentRepository.findDocumentByIdInCollection(documentId,collectionName);
        JqlQuery jqlQuery = QueryBuilder.byInstance(document).build();
        List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery);
        for (CdoSnapshot snapshot : snapshots) {
            if (snapshot.getCommitId().getMajorId() == Integer.parseInt(commitId))
                return JSON.parseObject(javers.getJsonConverter().toJson(snapshot.getState()));
        }
        return null;
    }

    //内嵌文档的插入、删除
    public Boolean updateEmbeddedDocument(String id,String collectionName,JSONObject updateInfo){
        String type = updateInfo.getString("type");
        String location = updateInfo.getString("location");
        JSONArray filterFactors = updateInfo.getJSONArray("filterFactors");
        JSONArray contents = updateInfo.getJSONArray("content");
        /*if(type==null||location==null) {
            logger.info("loss update Info.");
            return false;
        }*/
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        if(filterFactors!= null){
            for(int i=0;i<filterFactors.size();i++){
                JSONObject filterFactor = filterFactors.getJSONObject(i);
                DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
                update.filterArray(documentSearchFactor.getMatchKey(),documentSearchFactor.getMatchValue());
            }
        }
        if(contents!= null) {
            for (int i = 0; i < contents.size(); i++) {
                JSONObject content = contents.getJSONObject(i);
                if (type.equals("insert")) {
                    update.push(location, content);
                } else if (type.equals("update")) {
                    update.set(location, content);
                } else if (type.equals("delete")) {
                    DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(content);
                    update.pull(location, Query.query(Criteria.where(documentSearchFactor.getMatchKey()).is(documentSearchFactor.getMatchValue())).getQueryObject());
                }
                documentRepository.updateEmbeddedDocument(collectionName, query, update);
            }
        }
        return true;
    }
    //更新内嵌文档指定字段
    public Boolean updateEmbeddedDocument2(String id,String collectionName,JSONObject updateInfo){
        JSONArray filterFactors = updateInfo.getJSONArray("filterFactors");
        JSONArray contents = updateInfo.getJSONArray("content");
        if(contents==null) {
            logger.info("loss update Info.");
            return false;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        if(filterFactors!= null){
            for(int i=0;i<filterFactors.size();i++){
                JSONObject filterFactor = filterFactors.getJSONObject(i);
                DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
                update.filterArray(documentSearchFactor.getMatchKey(),documentSearchFactor.getMatchValue());
            }
        }

        for(int i=0;i<contents.size();i++){
            JSONObject content = contents.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(content);
            update.set(documentSearchFactor.getMatchKey(),documentSearchFactor.getMatchValue());
            documentRepository.updateEmbeddedDocument(collectionName,query,update);
        }

        return true;
    }

    //更新非内嵌文档指定字段
    public Boolean updateNotEmbeddedDocument(String id,String collectionName,JSONObject updateInfo){
        JSONArray contents = updateInfo.getJSONArray("content");
        if(contents==null) {
            logger.info("loss update Info.");
            return false;
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();

        for(int i=0;i<contents.size();i++){
            JSONObject content = contents.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(content);
            update.set(documentSearchFactor.getMatchKey(),documentSearchFactor.getMatchValue());
            documentRepository.updateEmbeddedDocument(collectionName,query,update);
        }

        return true;
    }
}
