//package com.cvicse.leasing.config;
//
//import com.mongodb.MongoClient;
//import com.mongodb.MongoCredential;
//import com.mongodb.ServerAddress;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.MongoDbFactory;
//import org.springframework.data.mongodb.MongoTransactionManager;
//import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
//
//import java.util.Arrays;
//
//@Configuration
//@EnableMongoRepositories(basePackages = "com.cvicse.leasing.repository")
//public class MongoTransactionConfig extends AbstractMongoConfiguration {
//
//    @Bean
//    MongoTransactionManager transactionManager(MongoDbFactory dbFactory) {
//        return new MongoTransactionManager(dbFactory);
//    }
//
//    @Override
//    protected String getDatabaseName() {
//        return "admin";
//    }
//
//    @Override
//    public MongoClient mongoClient() {
//        MongoCredential credential = MongoCredential.createCredential("root", "admin", "password123".toCharArray());
//        MongoClient mongoClient = new MongoClient(new ServerAddress("localhost", 27017), Arrays.asList(credential));
//        return mongoClient;
//    }
//}