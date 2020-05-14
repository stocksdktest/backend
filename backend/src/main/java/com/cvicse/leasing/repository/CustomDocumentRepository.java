package com.cvicse.leasing.repository;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//import com.cvicse.leasing.auth.framwork.auth.QueryModel;
//import com.cvicse.leasing.auth.framwork.auth.enums.ActionType;
import com.cvicse.leasing.model.Document;
import com.cvicse.leasing.model.ResultDocument;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Set;

public interface CustomDocumentRepository {
     //根据collectionName获取所有document
    //行过滤
//     @QueryModel(actionType = ActionType.QUERY)
     JSONObject findAllDocumentsPagination(String collectionName,JSONObject filters);

    List<Document> findAllDocumentsInCollection(String collectionName);

    //查询问题列表中有返回对比结果的计划集合信息
    List<ResultDocument> findQuestionCollection(String collectionName);

    //查询测试报告中可以展示测试报告（reprotFlag为1）的计划
    JSONArray findTestReportList(String collectionName);

    //查找所有的collection
    Set<String> findCollections();

    Set<String> findDataItemsInCollection(String collectionName);

    //根据collectionName和documentId获取具体的document
    Document findDocumentByIdInCollection(String id,String collectionName);

    // getOneDocumentByIdWithFilter(?)
    JSONArray findDocumentContentByCollection(String collectionName);

    //根据过滤条件查询某个document中的具体的某些内容
    // getOneDocumentByIdWithFilter(?)
    JSONObject findDocumentContentByIdAndProjectionOperation(String id, String collectionName, ProjectionOperation projectionOperation);

    //根据过滤条件查询某些具体的documents
    //getDocumentsByCriteria
    List<Document> findDocumentsByCriterias(List<Criteria> criterias, String collectionName);

    List<ResultDocument> findResultDocumentsByCriterias(List<Criteria> criterias, String collectionName);


    //getOneDocumentByIdWithAggregations
    JSONObject  findDocumentByIdWithLookUpOperations(String id, String collectionName, Integer hierarchicalLevels, List<LookupOperation> lookupOperations);

    //loop 查询内嵌文档
    List<JSONObject> findEmbeddedDocumentByAggregationOperations(String collectionName, List<AggregationOperation> aggregationOperations);
    //创建document
    Document createDocument(Document document);

    //创建ResultDocument
    ResultDocument createResultDocument(ResultDocument document);
    //保存document
    Document saveDocument(Document document);

    //删除document
    Document deleteDocument(Document document);

    void updateEmbeddedDocument(String collectionName, Query query, Update update);

}
