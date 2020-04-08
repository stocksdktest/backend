package com.cvicse.leasing.repository.Impl;


import com.alibaba.fastjson.JSONObject;
import com.cvicse.leasing.model.Document;
import com.cvicse.leasing.model.ResultDocument;
import com.cvicse.leasing.model.Status;
import com.cvicse.leasing.repository.CustomDocumentRepository;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Set;

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
        return mongoTemplate.find(query,Document.class,collectionName);
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
        return mongoTemplate.find(query,ResultDocument.class,collectionName);
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
