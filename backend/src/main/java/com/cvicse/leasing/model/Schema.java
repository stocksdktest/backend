package com.cvicse.leasing.model;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.lang.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Schema")
public class Schema {
    @Id
    private String id;

    private JSONObject schemaContent;

    private Status status;

    public Schema(JSONObject schemaContent){
        this.schemaContent = schemaContent;
    }

    public Schema(){}

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    @NonNull
    public JSONObject getSchemaContent() {
        return schemaContent;
    }

    public void setSchema(@NonNull JSONObject schemaContent) {
        this.schemaContent = schemaContent;
    }

    @Override
    public String toString() {
        return String.format(
                "Schema[id=%s,schemaContent='%s',schemaStatus='%s']",
                id, schemaContent,status);
    }
}
