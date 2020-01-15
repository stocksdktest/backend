package com.cvicse.leasing.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cvicse.leasing.model.Customization;
import com.cvicse.leasing.model.Status;
import com.cvicse.leasing.repository.CustomizationRepository;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomizationService {
    @Autowired
    CustomizationRepository schemaRepository;

    @Autowired
    Javers javers;

    private static final Logger logger = LoggerFactory.getLogger(CustomizationService.class);

    /**
     * 获取所有状态为"Created"的schema
     * @return
     */
    public List<Customization> getAllCustomizations(){
        logger.info("get all schemas");
        return schemaRepository.findAllByStatus(Status.Created);
    }

    /**
     * 根据schemaId获取对应的schema
     * @param id
     * @return
     */
    public Customization getCustomizationById(String id){
        logger.info("get schema by id");
        Customization schema = schemaRepository.findById(id).get();
        if(schema.getStatus().equals(Status.Created)){
        return schemaRepository.findById(id).get();
        }else return null;
    }

    /**
     * 创建schema
     * @param content
     * @return
     */
    public Customization createCustomization(JSONObject content){
        logger.info("create schema.");
        Customization schema = new Customization(content);
        schema.setStatus(Status.Created);
        return this.schemaRepository.save(schema);
    }

    /**
     * 更新schema
     * @param id
     * @param jsonObject
     * @return
     */
    public Customization updateCustomization(String id,JSONObject jsonObject) {
        logger.info("update schema by id.");
        this.schemaRepository.findById(id).ifPresent(schema -> {
            schema.setCustomization(jsonObject);
            this.schemaRepository.save(schema);
        });
        return this.schemaRepository.findById(id).get();
    }

    /**
     * 利用Status标记状态为"已删除"
     * @param id
     */
    public void deleteCustomization(String id){
        this.schemaRepository.findById(id).ifPresent(schema -> {
            schema.setStatus(Status.Deleted);
            this.schemaRepository.save(schema);
        });
    }

    public JSONArray trackCustomizationChangesWithJavers(String schemaId) {
        Customization schema = this.schemaRepository.findById(schemaId).get();
        JSONArray jsonArray = new JSONArray();
        JqlQuery jqlQuery = QueryBuilder.byInstance(schema).build();
        List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery);
        for (CdoSnapshot snapshot : snapshots) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("commitId", snapshot.getCommitId().getMajorId());
            jsonObject.put("commitDate", snapshot.getCommitMetadata().getCommitDate());
            jsonObject.put("data", JSON.parseObject(javers.getJsonConverter().toJson(snapshot.getState()), Customization.class));
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    public JSONObject getCustomizationWithJaversCommitId(String schemaId, String commitId){
        Customization schema= this.schemaRepository.findById(schemaId).get();
        JqlQuery jqlQuery = QueryBuilder.byInstance(schema).build();
        List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery);
        for (CdoSnapshot snapshot : snapshots) {
            System.out.println("content  "+snapshot.getState());
            System.out.println("commitId   "+snapshot.getCommitId());
            System.out.println(JSON.parseObject(javers.getJsonConverter().toJson(snapshot.getState())));
            if (snapshot.getCommitId().getMajorId() == Integer.parseInt(commitId))
                return JSON.parseObject(javers.getJsonConverter().toJson(snapshot.getState()));
        }
        return null;
    }
}
