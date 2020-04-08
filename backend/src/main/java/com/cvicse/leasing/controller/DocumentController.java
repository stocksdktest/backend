package com.cvicse.leasing.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
//import com.cvicse.leasing.auth.framwork.auth.AuthModel;
//import com.cvicse.leasing.auth.framwork.auth.enums.ActionType;
import com.cvicse.leasing.model.Document;
import com.cvicse.leasing.model.ResultDocument;
import com.cvicse.leasing.service.DocumentService;
//import com.cvicse.leasingauthmanage.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class DocumentController {
    @Autowired
    DocumentService documentService;

//    @Autowired
//    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @GetMapping("/collections")
    public List<String> getCollections(){
        return documentService.getCollections();
    }

    @GetMapping("/collections/{collectionName}")
    public Set<String> getDataKeys(@PathVariable String collectionName){
        return documentService.getDataKeys(collectionName);
    }

    @PostMapping("/result/new")
    public void createResultDocument(@RequestBody JSONArray testResult) {
        for(int i=0;i<testResult.size();i++){
            ObjectMapper objectMapper = new ObjectMapper();
            ResultDocument result = objectMapper.convertValue(testResult.get(i),ResultDocument.class);
            documentService.createResultDocument(result);
        }
    }

    @PostMapping("/documents/new")
    public Document createDocument(@RequestBody JSONObject params
            , @RequestParam(value = "schemaId", defaultValue = "null") String schemaId
            , @RequestParam(value = "collectionName", defaultValue = "null") String collectionName) {
        logger.info("create new Document by SchemaId " + schemaId + " and collectionName " + collectionName);
        return documentService.createDocument(schemaId, params);
    }

    @DeleteMapping("/documents/{id}")
    public List<Document> deleteDocument(@PathVariable String id
            , @RequestParam(value = "collectionName", defaultValue = "null") String collectionName) {
        logger.info("delete document " + id);
        this.documentService.deleteDocument(id, collectionName);
        return this.documentService.getDocumentsInCollection(collectionName);
    }

    /**
     * 问题列表查询
     * @param collectionName
     * @param filterFactors
     * @return
     */
    @GetMapping("/questionList")
    public List<Map<String,Object>> getQuestionList(
            @RequestParam(value = "collectionName", defaultValue = "null") String collectionName
            , @RequestParam(value = "filterFactors") String filterFactors) {
        logger.info(filterFactors);
        logger.info("get specialDocumentList by filterFactors " + filterFactors);
        JSONArray filters = JSONArray.parseArray(filterFactors);
        return this.documentService.getResultDocumentsByCriteriaList(filters, collectionName);
    }

    /**
     *测试报告页面查询（包含计划集合，对比结果集合）
     * @param collectionName
     * @param filterFactors  时间戳
     * @return
     */
    @GetMapping("/testReport")
    public List<Map<String,Object>> getTestReport(
            @RequestParam(value = "collectionName", defaultValue = "null") String collectionName
            , @RequestParam(value = "filterFactors") String filterFactors) {
        logger.info(filterFactors);
        logger.info("get specialDocumentList by filterFactors " + filterFactors);
        JSONArray filters = JSONArray.parseArray(filterFactors);
        return this.documentService.getTestReport(filters, collectionName);
    }

    @GetMapping("/documents")
//    @AuthModel(targetModel = Document.class, actionType = {ActionType.QUERY})
    public List<Document> getDocuments(
            @RequestParam(value = "collectionName", defaultValue = "null") String collectionName
            , @RequestParam(value = "filterFactors", defaultValue = "[]") String filterFactors) {
        logger.info(filterFactors);
        if (filterFactors.equals("[]")) {
            logger.info("get all documents by collectionName " + collectionName);
            List<Document> list = this.documentService.getDocumentsInCollection(collectionName);
            return list;
        } else {
            logger.info("get specialDocumentList by filterFactors " + filterFactors);
            JSONArray filters = JSONArray.parseArray(filterFactors);
            return this.documentService.getDocumentsByCriteriaList(filters, collectionName);
        }
    }

    /**
     * 问题列表下拉选，已有对比结果的计划信息查询
     * @param collectionName
     * @return
     */
    @GetMapping("/questionplan")
    public List<ResultDocument> findQuestionCollection(
            @RequestParam(value = "collectionName", defaultValue = "null") String collectionName) {
            logger.info("get all documents by collectionName " + collectionName);
            List<ResultDocument> list = this.documentService.findQuestionCollection(collectionName);
            return list;
    }

    @GetMapping("/documents/{id}")
    public JSONObject getDocumentByIdAndCollectionName(@PathVariable String id
            , @RequestParam(value = "collectionName", defaultValue = "null") String collectionName
            , @RequestParam(value = "filterFactors", defaultValue = "null") String filterFactors
            , @RequestParam(value = "hierarchical", defaultValue = "null") String hierarchical) {
        logger.info("get document by id " + id + " and collectionName " + collectionName);

        if (!hierarchical.equals("null")) {
            logger.info("get document by hierarchical " + hierarchical + " and filter " + filterFactors);
            JSONArray filters = JSONArray.parseArray(filterFactors);
            return documentService.getDocumentByHierarchicalQueries(id, collectionName, 1, filters);
        }
        else if (filterFactors.equals("null")) {
            Document document = documentService.getDocumentByIdInCollection(id, collectionName);
            Gson g = new Gson();
            String jsonString = g.toJson(document);
            System.out.println(jsonString);
            return JSONObject.parseObject(jsonString);
        } else {
            logger.info("get document by filter: " + filterFactors);
            JSONArray filters = JSONArray.parseArray(filterFactors);
            return documentService.getDocumentContents(id, collectionName, filters);
        }
    }

    /**
     * 内嵌文档插入，删除
     * @param id
     * @param collectionName
     * @param params
     * @param embeddedDocument
     * @return
     */
    @PutMapping("/documents/{id}")
    public Document updateDocument(@PathVariable String id
            , @RequestParam(value = "collectionName", defaultValue = "null") String collectionName
            , @RequestBody JSONObject params
            ,  @RequestParam(value = "embeddedDocument",defaultValue = "false") boolean embeddedDocument) {
        if(!embeddedDocument){
            logger.info("update document by Id " + id + " and collectionName " + collectionName);
            return documentService.updateDocument(id, collectionName, params);
        }else{
            logger.info("update embedded document by params "+params);
            if(documentService.updateEmbeddedDocument(id,collectionName,params))
                return documentService.getDocumentByIdInCollection(id,collectionName);
            else return null;
        }
    }

    /**
     * 文档具体字段的更新（包括内嵌非内嵌）
     * @param id
     * @param collectionName
     * @param params
     * @param embeddedDocument
     * @return
     */
    @PutMapping("/documents2/{id}")
    public JSONObject updateDocument2(@PathVariable String id
            , @RequestParam(value = "collectionName", defaultValue = "null") String collectionName
            , @RequestBody JSONObject params
            ,  @RequestParam(value = "embeddedDocument",defaultValue = "true") boolean embeddedDocument) {
        JSONObject returnValue = new JSONObject();
        if(embeddedDocument) {//true更新内嵌文档具体字段
            if (documentService.updateEmbeddedDocument2(id, collectionName, params)){
                returnValue.put("statusFlag","true");
                return returnValue;
            } else {
                returnValue.put("statusFlag","false");
                return returnValue;
            }
        }else{//false更新非内嵌文档的具体字段
            if (documentService.updateEmbeddedDocument(id, collectionName, params)){
                returnValue.put("statusFlag","true");
                return returnValue;
            } else {
                returnValue.put("statusFlag","false");
                return returnValue;
            }
        }
    }

    @GetMapping("/documents/{id}/commits")
    public JSONArray getDocumentWithCommitId(@PathVariable String id
            , @RequestParam(value = "collectionName", defaultValue = "null") String collectionName
            , @RequestParam(value = "commitId", defaultValue = "null") String commitId) {
        if (commitId.equals("null")) {
            try {
                logger.info("Get Document commits with documentId " + id);
                return this.documentService.trackDocumentChangesWithJavers(id, collectionName);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document Not Found.", e);
            }
        } else {
            try {
                logger.info("Cet Document commit with commitId" + commitId);
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(this.documentService.getDocumentWithJaversCommitId(id, collectionName, commitId));
                return jsonArray;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document Not Found.", e);
            }
        }
    }
}
