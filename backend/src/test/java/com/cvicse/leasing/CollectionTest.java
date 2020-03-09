package com.cvicse.leasing;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.cvicse.leasing.service.DocumentService;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.constraints.NotNull;

import static com.mongodb.client.model.Filters.eq;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CollectionTest {
    @Autowired
    DocumentService documentService;

    @Before
    public void beforeTest(){
        JSONObject A = new JSONObject();
        A.put("name","tc");
        A.put("age",25);
        A.put("address","liyang");
        A.put("title","testDemo");
        documentService.createDocument("124",A);
    }

    @Test
    public void test(){
        MongoClient mongoClient = new MongoClient("localhost",27017);
        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> collections = mongoClient.getDatabase("test")
                .getCollection("testDemo");
        for (Document cur : collections.find()) {
            System.out.println(cur.toJson());
        }
        System.out.println(collections.find(eq("schemaId", "124"))
                .first().toJson());
    }
}
