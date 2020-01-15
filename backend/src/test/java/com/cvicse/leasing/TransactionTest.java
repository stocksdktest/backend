package com.cvicse.leasing;


import com.alibaba.fastjson.JSONObject;
import com.cvicse.leasing.model.Document;
import com.cvicse.leasing.repository.DocumentRepository;
import com.cvicse.leasing.repository.SchemaRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
//@ContextConfiguration(classes = MongoTransactionConfig.class)
public class TransactionTest {
    @Autowired
    MongoTemplate mongoTemplate;

//    @Before
//    public void beforeTest(){
//        mongoTemplate.createCollection("cashFlow");
//    }


    @Test
    @Transactional
    public void givenTransactionTemplate_whenPerformTransaction_thenSuccess() {
        String schemaId = "111111";
        JSONObject schemaDocument = new JSONObject();
        schemaDocument.put("name","tang");
        schemaDocument.put("title","cashFlow");
        Document doc1 = new Document();
        doc1.setData(schemaDocument);
        doc1.setSchemaId(schemaId);
        doc1.setCollectionName(schemaDocument.getString("title"));
        String schemaId1 = "22222";
        JSONObject schemaDocument1 = new JSONObject();
        schemaDocument.put("name","cong");
        schemaDocument.put("title","cashFlow");
        Document doc2 = new Document();
        doc2.setData(schemaDocument1);
        doc2.setSchemaId(schemaId1);
        doc2.setCollectionName(schemaDocument1.getString("title"));

        //mongoTemplate.setSessionSynchronization(SessionSynchronization.ALWAYS);
        //TransactionTemplate transactionTemplate = new TransactionTemplate(mongoTransactionManager);
        //transactionTemplate.execute(new TransactionCallbackWithoutResult() {
        //    @Override
        //    protected void doInTransactionWithoutResult(TransactionStatus status) {
        mongoTemplate.save(doc1);
        mongoTemplate.save(doc2);
        //    };
        //});

        Query query = new Query().addCriteria(Criteria.where("collectionName").is("cashFlow"));
        List<Document> users = mongoTemplate.find(query, Document.class);
        for(Document document:users){
            System.out.println(document.toString());
        }
        System.out.println(users.toArray().toString());
    }
}
