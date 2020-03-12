package com.cvicse.leasing;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cvicse.leasing.model.Document;
import com.cvicse.leasing.model.DocumentSearchFactor;
import com.cvicse.leasing.model.Schema;
import com.cvicse.leasing.model.Status;
import com.cvicse.leasing.repository.DocumentRepository;
import com.cvicse.leasing.repository.SchemaRepository;
import com.cvicse.leasing.service.DocumentService;
import com.cvicse.leasing.service.SchemaService;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit4.SpringRunner;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LeasingSchemaApplicationTests {
    private static final Logger logger = LoggerFactory.getLogger(LeasingSchemaApplicationTests.class);
    @Autowired
    DocumentService documentService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    SchemaRepository schemaRepository;

    @Autowired
    SchemaService schemaService;

    @Test
    public void beforeTest(){
        JSONObject A = new JSONObject();
        JSONArray B = new JSONArray();
        JSONObject D = new JSONObject();
        JSONArray C = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","wdy");
        jsonObject.put("age","25");
        C.add(jsonObject);
//        D.put("content",C);
//        B.add(D);
        jsonObject = new JSONObject();
        jsonObject.put("name","wg");
        jsonObject.put("age","23");
        C.add(jsonObject);
        D.put("stu",C);
        D.put("type","1997");
        B.add(D);
        jsonObject = new JSONObject();
        jsonObject.put("name","czff");
        jsonObject.put("age","210");
        C = new JSONArray();
        C.add(jsonObject);
        D = new JSONObject();
        D.put("stu",C);
        D.put("type","1990");
        B.add(D);
        A.put("i2ec",B);
        A.put("sdkVersion","1");
        A.put("title","i2ec");
        documentService.createDocument("124",A);
    }

    @Test
    public void updateEmbeddedDocument(){
        JSONArray jsonArray = new JSONArray();
        JSONObject i = new JSONObject();
        i.put("key","first.type");
        i.put("value","1997");
        jsonArray.add(i);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key","name");
        jsonObject.put("value","wdy");
        JSONObject params = new JSONObject();
        params.put("type","delete");
        params.put("location","data.i2ec.$[first].stu");
        params.put("content",jsonObject);
        params.put("filterFactors",jsonArray);
        String  id = "5e69d19e721a941419527cfc";
        documentService.updateEmbeddedDocument(id,"i2ec",params);
//        Query query = new Query();
//        query.addCriteria(Criteria.where("schemaId").is("125"));
//        Update update = new Update();
//        update.push("data.i2ec", jsonObject);
//        update.pull("data.i2ec.$[first].stu",Query.query(Criteria.where("name").is("scc")).getQueryObject());
//        update.filterArray("first.type","1990");
//        update.filterArray("second.name","tc");
//        mongoTemplate.upsert(query,update,"i2ec");
    }


    @Test
    public void contextLoads() {
        String schemaId0 = "000";
        String schemaId1 = "111";
        String schemaId2 = "222";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a","qqqq");
        jsonObject.put("title","cashFlow");
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("a","bbbb");
        jsonObject1.put("title","cashFlow");
        Document document = documentService.createDocument(schemaId0,jsonObject);
        Document document1 = documentService.createDocument(schemaId1,jsonObject);
        documentService.createDocument(schemaId2,jsonObject);
        documentService.updateDocument(document.getId(),"cashFlow",jsonObject1);
        documentService.deleteDocument(document.getId(),"cashFlow");
        JSONArray jsonArray1 = new JSONArray();
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("key","data.a");
        jsonObject2.put("value","qqqq");
        jsonArray1.add(jsonObject2);
        List<Document> d = documentService.getDocumentsByCriteriaList(jsonArray1,"cashFlow");
        for(Document doc: d){
            System.out.println("ccccc    "+doc.toString());
        }
        Document document0 = documentService.getDocumentByIdInCollection(document1.getId(),"cashFlow");
        System.out.println(document0.toString());
        System.out.println("============getAllDocuments========");
        List<Document> documents = documentService.getDocumentsInCollection("cashFlow");
        System.out.println(documents.size());
        for(Document document2:documents){
            System.out.println(document2.toString());
        }
        System.out.println("==============版本控制================");
        JSONArray jsonArray = documentService.trackDocumentChangesWithJavers(document.getId(),"cashFlow");
        for(int i=0;i<jsonArray.size();i++){
            JSONObject jsonObject0 = jsonArray.getJSONObject(i);
            System.out.println("commitId: "+ jsonObject0.getString("commitId")+" commitDate: "+jsonObject0.getString("commitDate")
            +" data: "+jsonObject0.getString("data"));
        }
        System.out.println("=============根据commitId获取===========");
        //Document document2 = documentService.getDocumentWithJaversCommitId(document.getId(),jsonArray.getJSONObject(0).getString("commitId"));
        //System.out.println(document2.toString());

        mongoTemplate.dropCollection("cashFlow");
        mongoTemplate.dropCollection("Contract");
        documentRepository.deleteAll();
    }

    @Test
    public void testRepositoryAndTemplate(){
        List<Document> documents = documentRepository.findAll();
        List<Document> documentList = documentService.getDocumentsInCollection("cashFlow");
        List<Document> documents1 = documentService.getDocumentsInCollection("Contract");
        System.out.println(documents.size()+"    "+documentList.size());
        System.out.println("=======all from repository=======");
        for(Document document:documents){
            System.out.println(document.toString());
        }
        System.out.println("=======cashFlow from template=======");
        for(Document document:documentList){
            System.out.println(document.toString());
        }
        System.out.println("=======contract from template=======");
        for(Document document:documents1){
            System.out.println(document.toString());
        }
    }

    @Test
    public void testSchema(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("a","qqqq");
        Schema schema = new Schema(jsonObject);
        schema.setStatus(Status.Created);
        schemaRepository.save(schema);
        Schema newSchema = schemaRepository.findAllByStatus(Status.Created).get(0);
       // System.out.println(newSchema.toString());
        newSchema.setStatus(Status.Deleted);
        schemaRepository.save(newSchema);
        Schema newSchema1 = schemaRepository.findById(newSchema.getId()).get();
        System.out.println(newSchema1.toString());
        JSONArray jsonArray = schemaService.trackSchemaChangesWithJavers(newSchema.getId());
        for(int i=0;i<jsonArray.size();i++){
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            //System.out.println(jsonObject1.getString("commitId")+"    "+jsonObject1.getString("data"));
            String commitId = jsonObject1.getString("commitId");
            JSONObject schema1 = schemaService.getSchemaWithJaversCommitId(newSchema.getId(),commitId);
            System.out.println("schema=========="+schema1.toString());
        }
        schemaRepository.deleteAll();
    }

    @Test
    public void testAggregation(){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("collectionName","Contract");
        jsonObject.put("localParam","data.contractId");
        jsonObject.put("foreignParam","contractId");
        jsonArray.add(jsonObject);
    }

    @Test
    public void testProjectAggregation(){
        String schemaId = "11111";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title","cashFlow");
        jsonObject.put("firstName","han");
        jsonObject.put("lastName","yuanxi");
        jsonObject.put("age","82");
        jsonObject.put("city","jilin");
        logger.info("=============测试create========");
        Document document = documentService.createDocument(schemaId,jsonObject);
        logger.info(document.toString());

        List<Document> documents = documentService.getDocumentsInCollection("cashFlow");
        System.out.println(documents.size());
        for(Document document2:documents){
            System.out.println(document2.toString());
        }

        JSONObject jsonObject0 = new JSONObject();
        jsonObject0.put("title","cashFlow");
        jsonObject0.put("firstName","han");
        jsonObject0.put("lastName","yuanxi");
        jsonObject0.put("age","80");
        jsonObject0.put("city","jilin");
        logger.info("=============测试update=========");
        document = documentService.updateDocument(document.getId(),"cashFlow",jsonObject0);
        logger.info(document.toString());


        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("content","data.title");
        jsonArray.add(jsonObject1);
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("content","schemaId");
        jsonArray.add(jsonObject2);
        JSONObject newdocument = documentService.getDocumentContents(document.getId(),"cashFlow",jsonArray);
        logger.info("==============测试查询========");
        logger.info(newdocument.toJSONString());
        //documentService.deleteDocument(document.getId(),"cashFlow");
    }

    @Test
    public void testMatchAggregation(){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key","data.sdkInterface.id");
        jsonObject.put("value","1997");
        jsonObject.put("project","data");
        jsonArray.add(jsonObject);
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("key","data.sdkInterface.tcc.name");
        jsonObject1.put("value","wdy");
        jsonObject1.put("project","data.Bvalue");
        jsonArray.add(jsonObject1);
        List<Criteria> criteriaList = new ArrayList<>();
        List<DocumentSearchFactor> documentSearchFactors = new ArrayList<>();
        for(int i=0;i<jsonArray.size();i++){
            JSONObject filterFactor = jsonArray.getJSONObject(i);
            DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor(filterFactor);
            documentSearchFactor.setMatchProject(filterFactor.getString("project"));
            documentSearchFactors.add(documentSearchFactor);
            Criteria criteria = Criteria.where(documentSearchFactor.getMatchKey())
                    .is(documentSearchFactor.getMatchValue());
            criteriaList.add(criteria);
        }
        List<AggregationOperation> operations = Lists.newArrayList();
        for(Criteria criteria: criteriaList){
            operations.add(Aggregation.match(criteria));
        }
        operations.add(Aggregation.match(Criteria.where("status").is(Status.Created)));
        //获取到一个完整的sdk
//        for(DocumentSearchFactor documentSearchFactor :documentSearchFactors){
//            ProjectionOperation projectionOperation = Aggregation.project(documentSearchFactor.getMatchProject());
//            operations.add(projectionOperation);
//        }
        ProjectionOperation projectionOperation = Aggregation.project("data");
        operations.add(projectionOperation); //取出其中的sdkVersion部分
        ProjectionOperation projectionOperation1 = Aggregation.project("data.sdkInterface");
        operations.add(projectionOperation1); //取出其中的sdkInterface
        operations.add(Aggregation.unwind("sdkInterface")); //因为是数组 所以把每一个拆分为一个单独的文档
        Criteria criteria = Criteria.where("sdkInterface.id").is("1997");
        operations.add(Aggregation.match(criteria)); //查找id=1997的文档
        ProjectionOperation projectionOperation2 = Aggregation.project("sdkInterface.tcc");
        operations.add(projectionOperation2);  //往下一层 获取tcc
        operations.add(Aggregation.unwind("tcc"));  //将每一个tcc拆分为一个单独的文档
        Criteria criteria1 = Criteria.where("tcc.name").is("wdy");  //查找name = wdy 的文档
        operations.add(Aggregation.match(criteria1));
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<JSONObject> contractAggregationResults= mongoTemplate.aggregate(aggregation,"testDemo", JSONObject.class);
        for(JSONObject jsonObject2:contractAggregationResults){
            logger.info(jsonObject2.toString());
        }
    }

    @Test
    public void testHierarchicalQueries(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("collectionName","cashFlow");
        jsonObject.put("localParam","data.contractId");
        jsonObject.put("foreignParam","schemaId");
        jsonObject.put("as","contract");
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(jsonObject);

        String schemaId = "111122";
        JSONObject schemaDocument = new JSONObject();
        schemaDocument.put("name","tang");
        schemaDocument.put("title","cashFlow");
        Document document = documentService.createDocument(schemaId,schemaDocument);
        logger.info(document.toString());

        String schemaId1 = "222222";
        JSONObject contractDocument = new JSONObject();
        contractDocument.put("contractId","111122");
        contractDocument.put("title","contract");
        logger.info(documentService.createDocument(schemaId1,contractDocument).toString());

        JSONObject jsonObject1 = documentService.getDocumentByHierarchicalQueries(document.getId(),"contract",1,jsonArray);
        logger.info(jsonObject1.toJSONString());
    }

    @Test
    public void testGetEmbeddedDocuments(){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("key","data.sdkVersion");
        jsonObject2.put("value","1");
        jsonArray.add(jsonObject2);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key","data.sdkInterface.id");
        jsonObject.put("value","1997");
        jsonArray.add(jsonObject);
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("key","data.sdkInterface.tcc.name");
        jsonObject1.put("value","wdy");
        jsonArray.add(jsonObject1);
        List<JSONObject> documents = documentService.getEmbeddedDocuments("testDemo",jsonArray,"data(O).sdkInterface(A).tcc(A)");
        for(JSONObject jsonObject3:documents)
            logger.info(jsonObject3.toString());
    }
    @Test
    public void testGetDocumentsInCollection(){

    }
}
