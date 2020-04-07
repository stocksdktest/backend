package com.cvicse.leasing.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import com.mongodb.QueryBuilder;
import java.math.BigDecimal;
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
     * 查询问题列表中有返回对比结果的计划集合信息
     * @param collectionName
     * @return
     */
    public List<Document> findQuestionCollection(String collectionName){
        logger.info("get all documents by collectionName.");
        return documentRepository.findQuestionCollection(collectionName);
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
     * 对比结果中查询问题列表信息
     * @param filters
     * @param collectionName
     * @return
     */
    public List<Map<String,Object>> getResultDocumentsByCriteriaList(JSONArray filters,String collectionName){
        List<Criteria> criteriaList = new ArrayList<>();
        for(int i=0;i<filters.size();i++){
            JSONObject filterFactor = filters.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
            Criteria criteria = Criteria.where(documentSearchFactor.getMatchKey())
                    .is(documentSearchFactor.getMatchValue());
            criteriaList.add(criteria);
        }
        List<ResultDocument> list = documentRepository.findResultDocumentsByCriterias(criteriaList,collectionName);
        List<Map<String,Object>> questionList =  new ArrayList<Map<String,Object>>();
        if(list!=null && !list.isEmpty()){
            JSONArray mismatchArray = list.get(0).getMismatch();
            JSONArray errorArray = list.get(0).getError();
            JSONArray emptyArray = list.get(0).getEmpty();
            JSONArray comparedFalseArray = list.get(0).getResult().getJSONArray("false");//compared中为false的数组
            String id = list.get(0).get_id();
            String paramStr;//用例参数
            String testcaseID1;//方法名
            JSONArray details;//详细信息
            String recordID;//recordID，后续更新状态需要
            String status;//状态：默认0，确认1，忽略2
            if(!comparedFalseArray.isEmpty()&&comparedFalseArray.size()!=0){
                for(int i=0;i<comparedFalseArray.size();i++){
                    Map<String,Object> questionMap =new HashMap<String,Object>();
                    paramStr = comparedFalseArray.getJSONObject(i).getString("paramStr");
                    testcaseID1 = comparedFalseArray.getJSONObject(i).getString("testcaseID1");
                    details = comparedFalseArray.getJSONObject(i).getJSONArray("details");
                    recordID = comparedFalseArray.getJSONObject(i).getString("recordID1");
                    status = comparedFalseArray.getJSONObject(i).getString("status");
                    questionMap.put("status",status);
                    questionMap.put("id",id);
                    questionMap.put("paramStr",paramStr);
                    questionMap.put("testcaseID",testcaseID1);
                    questionMap.put("details",details);
                    questionMap.put("recordID",recordID);
                    questionMap.put("type","false");
                    questionList.add(questionMap);
                }
            }
            if(!mismatchArray.isEmpty()&&mismatchArray.size()!=0){
                for(int i=0;i<mismatchArray.size();i++){
                    Map<String,Object> questionMap2 =new HashMap<String,Object>();
                    paramStr = mismatchArray.getJSONObject(i).getString("paramStr");
                    testcaseID1 = mismatchArray.getJSONObject(i).getString("testcaseID");
                    recordID = mismatchArray.getJSONObject(i).getString("recordID");
                    status = mismatchArray.getJSONObject(i).getString("status");
                    questionMap2.put("status",status);
                    questionMap2.put("id",id);
                    questionMap2.put("paramStr",paramStr);
                    questionMap2.put("testcaseID",testcaseID1);
                    questionMap2.put("recordID",recordID);
                    questionMap2.put("type","mismatch");
                    questionList.add(questionMap2);
                }
            }
            if(!errorArray.isEmpty()&&errorArray.size()!=0){
                for(int i=0;i<errorArray.size();i++){
                    Map<String,Object> questionMap3 =new HashMap<String,Object>();
                    paramStr = errorArray.getJSONObject(i).getString("paramStr");
                    testcaseID1 = errorArray.getJSONObject(i).getString("testcaseID");
                    recordID = errorArray.getJSONObject(i).getString("recordID");
                    status = errorArray.getJSONObject(i).getString("status");
                    questionMap3.put("status",status);
                    questionMap3.put("id",id);
                    questionMap3.put("paramStr",paramStr);
                    questionMap3.put("testcaseID",testcaseID1);
                    questionMap3.put("recordID",recordID);
                    questionMap3.put("type","error");
                    questionList.add(questionMap3);
                }
            }
            if(!emptyArray.isEmpty()&&emptyArray.size()!=0){
                for(int i=0;i<emptyArray.size();i++){
                    Map<String,Object> questionMap4 =new HashMap<String,Object>();
                    paramStr = emptyArray.getJSONObject(i).getString("paramStr");
                    testcaseID1 = emptyArray.getJSONObject(i).getString("testcaseID");
                    recordID = emptyArray.getJSONObject(i).getString("recordID");
                    status = emptyArray.getJSONObject(i).getString("status");
                    questionMap4.put("status",status);
                    questionMap4.put("id",id);
                    questionMap4.put("paramStr",paramStr);
                    questionMap4.put("testcaseID",testcaseID1);
                    questionMap4.put("recordID",recordID);
                    questionMap4.put("type","empty");
                    questionList.add(questionMap4);
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
        List<Map<String,Object>> bugList =  new ArrayList<Map<String,Object>>();//缺陷列表
        Map<String,Object> reportMap =new HashMap<String,Object>();
        Set methodsAllSet = new HashSet();//将方法放入set集合进行自动去重，计数，用于计算覆盖率，通过率
        Set methodsErrorSet = new HashSet();
        /*---------------------查询对比结果集合-------------------------*/
        for(int i=0;i<filters.size();i++){
            JSONObject filterFactor = filters.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
            Criteria criteria = Criteria.where(documentSearchFactor.getMatchKey()).is(documentSearchFactor.getMatchValue());
            criteriaList.add(criteria);
        }
        List<ResultDocument> list = documentRepository.findResultDocumentsByCriterias(criteriaList,collectionName);
        JSONArray mismatchArray = new JSONArray();
        JSONArray errorArray = new JSONArray();
        JSONArray emptyArray = new JSONArray();
        JSONArray comparedFalseArray = new JSONArray();
        JSONArray comparedTrueArray = new JSONArray();
        if(list!=null && !list.isEmpty()){
            mismatchArray = list.get(0).getMismatch();
            errorArray = list.get(0).getError();
            emptyArray = list.get(0).getEmpty();
            comparedFalseArray = list.get(0).getResult().getJSONArray("false");//compared中为false的数组
            comparedTrueArray = list.get(0).getResult().getJSONArray("true");//compared中为false的数组
            String id = list.get(0).get_id();
            List<Map<String,Object>> questionList =  new ArrayList<Map<String,Object>>();
            String paramStr;//用例参数
            String testcaseID1;//方法名
            String status;//状态：默认0，确认1，忽略2
            String bugDescribe;//bug描述
            if(!comparedFalseArray.isEmpty()&&comparedFalseArray.size()!=0){
                for(int i=0;i<comparedFalseArray.size();i++){
                    Map<String,Object> questionMap =new HashMap<String,Object>();
                    paramStr = comparedFalseArray.getJSONObject(i).getString("paramStr");
                    testcaseID1 = comparedFalseArray.getJSONObject(i).getString("testcaseID1");
                    bugDescribe = comparedFalseArray.getJSONObject(i).getString("bugDescribe");
                    status = comparedFalseArray.getJSONObject(i).getString("status");
                    methodsAllSet.add(testcaseID1);//将方法名加入set，实现去重
                    methodsErrorSet.add(testcaseID1);
                    if(("1").equals(status)){
                        questionMap.put("paramStr",paramStr);
                        questionMap.put("testcaseID",testcaseID1);
                        questionMap.put("bugDescribe",bugDescribe);
                        bugList.add(questionMap);
                    }else{
                        continue;
                    }
                }
            }
            if(!mismatchArray.isEmpty()&&mismatchArray.size()!=0){
                for(int i=0;i<mismatchArray.size();i++){
                    Map<String,Object> questionMap2 =new HashMap<String,Object>();
                    paramStr = mismatchArray.getJSONObject(i).getString("paramStr");
                    testcaseID1 = mismatchArray.getJSONObject(i).getString("testcaseID");
                    bugDescribe = mismatchArray.getJSONObject(i).getString("bugDescribe");
                    status = mismatchArray.getJSONObject(i).getString("status");
                    methodsAllSet.add(testcaseID1);//将方法名加入set，实现去重
                    methodsErrorSet.add(testcaseID1);
                    if(("1").equals(status)){
                        questionMap2.put("paramStr",paramStr);
                        questionMap2.put("testcaseID",testcaseID1);
                        questionMap2.put("bugDescribe",bugDescribe);
                        bugList.add(questionMap2);
                    }else{
                        continue;
                    }
                }
            }
            if(!errorArray.isEmpty()&&errorArray.size()!=0){
                for(int i=0;i<errorArray.size();i++){
                    Map<String,Object> questionMap3 =new HashMap<String,Object>();
                    paramStr = errorArray.getJSONObject(i).getString("paramStr");
                    testcaseID1 = errorArray.getJSONObject(i).getString("testcaseID");
                    bugDescribe = errorArray.getJSONObject(i).getString("bugDescribe");
                    status = errorArray.getJSONObject(i).getString("status");
                    methodsAllSet.add(testcaseID1);//将方法名加入set，实现去重
                    methodsErrorSet.add(testcaseID1);
                    if(("1").equals(status)){
                        questionMap3.put("paramStr",paramStr);
                        questionMap3.put("testcaseID",testcaseID1);
                        questionMap3.put("bugDescribe",bugDescribe);
                        bugList.add(questionMap3);
                    }else{
                        continue;
                    }
                }
            }
            if(!emptyArray.isEmpty()&&emptyArray.size()!=0){
                for(int i=0;i<emptyArray.size();i++){
                    Map<String,Object> questionMap4 =new HashMap<String,Object>();
                    paramStr = emptyArray.getJSONObject(i).getString("paramStr");
                    testcaseID1 = emptyArray.getJSONObject(i).getString("testcaseID");
                    bugDescribe = emptyArray.getJSONObject(i).getString("bugDescribe");
                    status = emptyArray.getJSONObject(i).getString("status");
                    methodsAllSet.add(testcaseID1);//将方法名加入set，实现去重
                    methodsErrorSet.add(testcaseID1);
                    if(("1").equals(status)){
                        questionMap4.put("paramStr",paramStr);
                        questionMap4.put("testcaseID",testcaseID1);
                        questionMap4.put("bugDescribe",bugDescribe);
                        bugList.add(questionMap4);
                    }else{
                        continue;
                    }
                }
            }
            if(!comparedTrueArray.isEmpty()&&comparedTrueArray.size()!=0){
                for(int i=0;i<comparedTrueArray.size();i++){
                    testcaseID1 = comparedTrueArray.getJSONObject(i).getString("testcaseID1");
                    methodsAllSet.add(testcaseID1);//将方法名加入set，实现去重
                }
            }
            reportMap.put("bugList",bugList);//将缺陷列表放入结果map中
        }
        /*---------------------查询计划集合-------------------------*/
        List<Criteria> criteriaList2 = new ArrayList<>();
        for(int i=0;i<filters.size();i++){
            JSONObject filterFactor = filters.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
            Criteria criteria = Criteria.where(("data.time_stamp")).is(documentSearchFactor.getMatchValue());
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
            reportMap.put("planName",planName);
            reportMap.put("planType",planType);
            reportMap.put("environment",environment);
            reportMap.put("startTime",startTime);
            reportMap.put("sdkVersion",sdkVersion);
            reportMap.put("runTimes",runTimes);
            /*---------------------统计图数据查询-------------------------*/
            //查询计算本次版本下的方法数和用例数
            List<Criteria> criteriaList3 = new ArrayList<>();
            Criteria criteria = Criteria.where(("data.sdk_version")).is(sdkVersion);
            criteriaList3.add(criteria);
            collectionName = "testInformation";
            List<Document> versionList = documentRepository.findDocumentsByCriterias(criteriaList3,collectionName);
            JSONArray interfacesList = versionList.get(0).getData().getJSONArray("interfaces");
            double versionMethods = 0.00;//该版本下的方法总数
            double versionTestcases = 0.00;//该版本下的用例总数
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
            //方法覆盖率：  本次计划总方法数/本版本总方法数
            double thisMethods = methodsAllSet.toArray().length;
            double methodCoverRate = divide(thisMethods,versionMethods,4);//方法覆盖率
            reportMap.put("methodCoverRate",methodCoverRate);
            //方法通过率：  1-本次计划方法错误数/本次计划总方法数
            double methodsNotPass = methodsErrorSet.toArray().length;//先算错误未通过的个数
            double methodNotPassRate = divide(methodsNotPass,thisMethods,4);
            double methodPassRate = 1.00-methodNotPassRate;
            reportMap.put("methodPassRate",methodPassRate);
            //用例覆盖率  本次计划用例个数/本版本总用例数
            double thisTestcases = comparedFalseArray.size()+comparedTrueArray.size()+emptyArray.size()+mismatchArray.size()+errorArray.size();//本次计划总用例数
            double testcaseCoverRate = divide(thisTestcases,versionTestcases,4);
            reportMap.put("testcaseCoverRate",testcaseCoverRate);
            //用例通过率  本次计划用例正确数/本次使用总用例数
            double thisTestcasePass = comparedTrueArray.size();
            double testcasePassRate = divide(thisTestcasePass,thisTestcases,4);
            reportMap.put("testcasePassRate",testcasePassRate);
        }

        //将map放入一个list中，可能业务扩展需求
        reportList.add(reportMap);
        return reportList;
    }

    /**
     * 提供精确的除法运算方法divide
     * @param value1 被除数
     * @param value2 除数
     * @param scale 精确范围
     * @return 两个参数的商
     */
    public double divide(double value1,double value2,int scale){
        BigDecimal b1 = new BigDecimal(Double.valueOf(value1));
        BigDecimal b2 = new BigDecimal(Double.valueOf(value2));
        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
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
