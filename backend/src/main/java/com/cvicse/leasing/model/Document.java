package com.cvicse.leasing.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
//import com.cvicse.leasing.auth.framwork.model.BaseEntity;
import org.springframework.data.annotation.Id;

@org.springframework.data.mongodb.core.mapping.Document(collection = "Document")
public class Document  {
    @Id
    private String id;

    private String schemaId;

    private String collectionName;

    private JSONObject data;

    private Status status;

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public String getSchemaId() {
        return schemaId;
    }

    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    @Override
    public String toString() {
//        System.out.println("bug" + JSON.toJSONString(data));
        return String.format(
                "{id:'%s', schemaId:'%s',collectionName:'%s', data:'%s',status:'%s'}",
                id, schemaId, collectionName, JSON.toJSONString(data), status);
    }
}
