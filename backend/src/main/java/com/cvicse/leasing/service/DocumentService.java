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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import com.mongodb.QueryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
//            System.out.println(hierarchicalFactor);
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
}
