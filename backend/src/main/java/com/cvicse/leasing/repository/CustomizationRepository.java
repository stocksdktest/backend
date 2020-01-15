package com.cvicse.leasing.repository;

import com.cvicse.leasing.model.Customization;
import com.cvicse.leasing.model.Status;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@JaversSpringDataAuditable
public interface CustomizationRepository extends MongoRepository<Customization,String> {
    List<Customization> findAllByStatus(Status status);
}

