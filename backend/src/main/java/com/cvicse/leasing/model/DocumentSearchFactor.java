package com.cvicse.leasing.model;

import com.alibaba.fastjson.JSONObject;

/**
 * 文档全局搜索时的参数
 */
public class DocumentSearchFactor {
    private String matchKey;
    private String matchValue;
    private String matchProject;

    public DocumentSearchFactor(JSONObject params){
        matchKey = params.getString("key");
        matchValue = params.getString("value");
    }
    public DocumentSearchFactor(){}

    public void setMatchProject(String matchProject) {
        this.matchProject = matchProject;
    }

    public String getMatchProject() {
        return matchProject;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }

    public void setMatchValue(String matchValue) {
        this.matchValue = matchValue;
    }

    public String getMatchKey() {
        return matchKey;
    }

    public String getMatchValue() {
        return matchValue;
    }
}
