package com.cvicse.leasing.repository;

import com.cvicse.leasing.model.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
//@JaversSpringDataAuditable
public interface DocumentRepository extends CustomDocumentRepository,
        MongoRepository<Document, String> {
}
