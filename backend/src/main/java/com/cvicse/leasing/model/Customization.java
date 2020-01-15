package com.cvicse.leasing.model;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.lang.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Customization")
public class Customization {
    @Id
    private String id;

    private JSONObject schemaContent;

    private Status status;

    public Customization(JSONObject schemaContent){
        this.schemaContent = schemaContent;
    }

    public Customization(){}

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
    public JSONObject getCustomizationContent() {
        return schemaContent;
    }

    public void setCustomization(@NonNull JSONObject schemaContent) {
        this.schemaContent = schemaContent;
    }

    @Override
    public String toString() {
        return String.format(
                "Customization[id=%s,schemaContent='%s',schemaStatus='%s']",
                id, schemaContent,status);
    }
}
