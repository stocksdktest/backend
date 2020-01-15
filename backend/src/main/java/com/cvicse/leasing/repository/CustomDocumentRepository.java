package com.cvicse.leasing.repository;

import com.alibaba.fastjson.JSONObject;
//import com.cvicse.leasing.auth.framwork.auth.QueryModel;
//import com.cvicse.leasing.auth.framwork.auth.enums.ActionType;
import com.cvicse.leasing.model.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;
import java.util.Set;

public interface CustomDocumentRepository {
     //根据collectionName获取所有document
    //行过滤
//     @QueryModel(actionType = ActionType.QUERY)
    List<Document> findAllDocumentsInCollection(String collectionName);
    //查找所有的collection
    Set<String> findCollections();

    Set<String> findDataItemsInCollection(String collectionName);

    //根据collectionName和documentId获取具体的document
    Document findDocumentByIdInCollection(String id,String collectionName);

    //根据过滤条件查询某个document中的具体的某些内容
    // getOneDocumentByIdWithFilter(?)
    JSONObject findDocumentContentByIdAndProjectionOperation(String id, String collectionName, ProjectionOperation projectionOperation);

    //根据过滤条件查询某些具体的documents
    //getDocumentsByCriteria
    List<Document> findDocumentsByCriterias(List<Criteria> criterias, String collectionName);

    //getOneDocumentByIdWithAggregations
    JSONObject  findDocumentByIdWithLookUpOperations(String id, String collectionName, Integer hierarchicalLevels, List<LookupOperation> lookupOperations);

    //loop 查询内嵌文档
    List<JSONObject> findEmbeddedDocumentByAggregationOperations(String collectionName, List<AggregationOperation> aggregationOperations);
    //创建document
    Document createDocument(Document document);
    //保存document
    Document saveDocument(Document document);

    //删除document
    Document deleteDocument(Document document);

}
