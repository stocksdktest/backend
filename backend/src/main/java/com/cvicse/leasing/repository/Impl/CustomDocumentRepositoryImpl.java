package com.cvicse.leasing.repository.Impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cvicse.leasing.model.Document;
import com.cvicse.leasing.model.ResultDocument;
import com.cvicse.leasing.model.Status;
import com.cvicse.leasing.repository.CustomDocumentRepository;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.*;

@RequiredArgsConstructor
public class CustomDocumentRepositoryImpl implements CustomDocumentRepository {
    private static final Logger logger = LoggerFactory.getLogger(CustomDocumentRepositoryImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Set<String> findDataItemsInCollection(String collectionName){
        List<Document> documentList = this.findAllDocumentsInCollection(collectionName);
        Document document = documentList.get(0);
        JSONObject data = document.getData();
        return data.keySet();
    }

    @Override
    public Set<String> findCollections(){
        logger.info("find all collections.");
        return mongoTemplate.getCollectionNames();
    }

    @Override
    public List<Document> findAllDocumentsInCollection(String collectionName){
        logger.info("get all documents by collectionName in CustomDocumentRepositoryImpl "+collectionName);
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is("Created"));
        List<Document> list = mongoTemplate.find(query,Document.class,collectionName);
        Collections.reverse(list);//list元素倒序
        return list;
    }

    @Override
    public JSONObject findAllDocumentsPagination(String collectionName,JSONObject filters){
        JSONObject queryField = filters.getJSONObject("queryField");//进行模糊查询的字段
        Integer pageNumber = filters.getInteger("pageNumber");//页码
        Integer pageSize = filters.getInteger("pageSize");//页面最大数
        JSONObject jsonObject = new JSONObject();
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is("Created"));
        Iterator iter = queryField.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String  key = entry.getKey().toString();
            String value = entry.getValue().toString();
            if(!"".equals(value)){
                query.addCriteria(Criteria.where(key).regex(value));
            }
        }
        //查总条数
        long count=mongoTemplate.count(query,Document.class,collectionName);
        //query.skip((pageNumber-1)*pageSize);//跳过前几页
        int intCount = (int)count;//long转int，参与运算
        int skipNum = intCount-pageNumber*pageSize;
        if(skipNum<0){
            pageSize += skipNum;
            skipNum = 0;
        }
        query.skip(skipNum);//跳过前几页
        query.limit(pageSize);
        List<Document> list = mongoTemplate.find(query,Document.class,collectionName);
        Collections.reverse(list);//list元素倒序
        jsonObject.put("count",count);
        jsonObject.put("planList",list);

        return jsonObject;
    }

    @Override
    public List<ResultDocument> findQuestionCollection(String collectionName){
        logger.info("get all documents by collectionName in CustomDocumentRepositoryImpl "+collectionName);
       /* Criteria criteria = new Criteria();
        Criteria criteria2 = new Criteria();
        Criteria criteria3 = new Criteria();
        Criteria criteria4 = new Criteria();
        criteria.and("data.time_stamp").ne("");
        criteria2.and("data.time_stamp").ne(null);
        criteria3.and("status").is(Status.Created);
        criteria4.andOperator(criteria,criteria2,criteria3);*/
        Query query = new Query();
        //query.addCriteria(criteria4);
        //query.addCriteria(Criteria.where("status").is(Status.Created));
        //query.addCriteria(Criteria.where("data.time_stamp").ne("").ne(null));
        List<ResultDocument> list = mongoTemplate.find(query,ResultDocument.class,collectionName);
        Collections.reverse(list);//list元素倒序
        return list;
    }

    @Override
    public JSONArray findTestReportList(String collectionName){
        DBObject dbObject = new BasicDBObject();//查询条件
        dbObject.put("reportFlag","1");
        BasicDBObject fieldsObject=new BasicDBObject();
        //指定返回的字段
        fieldsObject.put("planName", true);
        fieldsObject.put("jobID", true);
        fieldsObject.put("sdkVersion", true);
        //置顶返回内嵌文档的某个属性
/*		fieldsObject.put("bookList.bookCurrencylist.currencyNumber", true);
		fieldsObject.put("bookList.bookCurrencylist.currencyProperty", true); */
        Query query = new BasicQuery(dbObject.toString(), fieldsObject.toString());
        List<ResultDocument> find = this.mongoTemplate.find(query, ResultDocument.class,collectionName);
        JSONArray array= JSONArray.parseArray(JSON.toJSONString(find));
        Collections.reverse(array);//list元素倒序
        return array;
    }

    @Override
    public  Document findDocumentByIdInCollection(String id,String collectionName)throws NullPointerException{
        return this.mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)),Document.class,collectionName);
    }


//    @Override
//    public JSONObject findDocumentContentsByIdAndProjectionOperations(String id, String collectionName, List<ProjectionOperation> projectionOperationList){
//        JSONObject jsonObject = new JSONObject();
//        for(ProjectionOperation projectionOperation:projectionOperationList){
//            this.getOnePieceContent(id,collectionName,projectionOperation);
//        }
//        return jsonObject;
//    }

    /**
     * 对比结果中查询问题列表信息
     * @param criteriaList
     * @param collectionName
     * @return
     */
    @Override
    public  List<ResultDocument> findResultDocumentsByCriterias(List<Criteria> criteriaList, String collectionName){
        List<AggregationOperation> operations = Lists.newArrayList();
        for(Criteria criteria: criteriaList){
            operations.add(Aggregation.match(criteria));
        }
        //operations.add(Aggregation.match(Criteria.where("status").is(Status.Created)));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<ResultDocument> contractAggregationResults= mongoTemplate.aggregate(aggregation,collectionName, ResultDocument.class);
        return contractAggregationResults.getMappedResults();
    }

    @Override
    public  List<Document> findDocumentsByCriterias(List<Criteria> criteriaList, String collectionName){
        List<AggregationOperation> operations = Lists.newArrayList();
        for(Criteria criteria: criteriaList){
            operations.add(Aggregation.match(criteria));
        }
        operations.add(Aggregation.match(Criteria.where("status").is(Status.Created)));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<Document> contractAggregationResults= mongoTemplate.aggregate(aggregation,collectionName, Document.class);
        return contractAggregationResults.getMappedResults();
    }

    @Override
    public JSONObject findDocumentByIdWithLookUpOperations(String id, String collectionName, Integer hierarchicalLevels, List<LookupOperation> lookupOperations){
        List<AggregationOperation> operations = Lists.newArrayList();
        operations.add(Aggregation.match(Criteria.where("_id").is(id)));
        for(LookupOperation lookupOperation:lookupOperations){
            operations.add(lookupOperation);
        }
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<JSONObject> contractAggregationResults= mongoTemplate.aggregate(aggregation,collectionName, JSONObject.class);
        List<JSONObject> documents = contractAggregationResults.getMappedResults();
        if(documents.size()==1)
            return documents.get(0);
        else return null;
    }

    @Override
    public  Document createDocument(Document document){
        return mongoTemplate.insert(document,document.getCollectionName());
    }

    @Override
    public  ResultDocument createResultDocument(ResultDocument document){
        return mongoTemplate.insert(document,"testResult");
    }

    @Override
    public Document saveDocument(Document document){
       return mongoTemplate.save(document,document.getCollectionName());
    }

    @Override
    public  Document deleteDocument(Document document){
        //Document document = this.getDocumentByIdAndCollectionName(id,collectionName);
        document.setStatus(Status.Deleted);
        //javers.commit(id,document);
        return mongoTemplate.save(document,document.getCollectionName());
    }

    @Override
    public JSONArray findDocumentContentByCollection(String collectionName){
        DBObject dbObject = new BasicDBObject();//查询条件
        BasicDBObject fieldsObject=new BasicDBObject();
        //指定返回的字段
        fieldsObject.put("planName", true);
        fieldsObject.put("jobID", true);
        fieldsObject.put("sdkVersion", true);
        //置顶返回内嵌文档的某个属性
/*		fieldsObject.put("bookList.bookCurrencylist.currencyNumber", true);
		fieldsObject.put("bookList.bookCurrencylist.currencyProperty", true); */
        Query query = new BasicQuery(dbObject.toString(), fieldsObject.toString());
        List<ResultDocument> find = this.mongoTemplate.find(query, ResultDocument.class,collectionName);
        JSONArray array= JSONArray.parseArray(JSON.toJSONString(find));
        Collections.reverse(array);//list元素倒序
        return array;
    }

    @Override
    public JSONObject findDocumentContentByIdAndProjectionOperation(String id, String collectionName, ProjectionOperation projectionOperation){
        logger.info("get special Document by id "+id);
        List<AggregationOperation> operations = Lists.newArrayList();
        operations.add(Aggregation.match(Criteria.where("_id").is(id)));
        operations.add(projectionOperation);
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<JSONObject> contractAggregationResults= mongoTemplate.aggregate(aggregation,collectionName, JSONObject.class);
        List<JSONObject> documents = contractAggregationResults.getMappedResults();
        if(documents.size()==1){
            JSONObject jsonObject =  documents.get(0);
            jsonObject.remove("_id");
            return jsonObject;
        }
        else return null;
    }

    @Override
    public  List<JSONObject> findEmbeddedDocumentByAggregationOperations(String collectionName, List<AggregationOperation> aggregationOperations){
        Aggregation aggregation = Aggregation.newAggregation(aggregationOperations);
        AggregationResults<JSONObject> contractAggregationResults= mongoTemplate.aggregate(aggregation,collectionName, JSONObject.class);
        List<JSONObject> documents = contractAggregationResults.getMappedResults();
        return documents;
    }

    @Override
    public void updateEmbeddedDocument(String collectionName, Query query, Update update){
        mongoTemplate.upsert(query,update,collectionName);
    }

}
