package com.cvicse.leasing.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.annotation.Id;

//import com.cvicse.leasing.auth.framwork.model.BaseEntity;

@org.springframework.data.mongodb.core.mapping.Document(collection = "Document")
public class ResultDocument {
    @Id
    private String id;

    private String jobID;

    private String dagID;

    private String title;

    private String runnerID1;

    private String runnerID2;

    private JSONArray mismatch;

    private JSONObject result;

    private JSONArray error;

    private JSONArray empty;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getDagID() {
        return dagID;
    }

    public void setDagID(String dagID) {
        this.dagID = dagID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRunnerID1() {
        return runnerID1;
    }

    public void setRunnerID1(String runnerID1) {
        this.runnerID1 = runnerID1;
    }

    public String getRunnerID2() {
        return runnerID2;
    }

    public void setRunnerID2(String runnerID2) {
        this.runnerID2 = runnerID2;
    }

    public JSONArray getMismatch() {
        return mismatch;
    }

    public void setMismatch(JSONArray mismatch) {
        this.mismatch = mismatch;
    }

    public JSONObject getResult() {
        return result;
    }

    public void setResult(JSONObject result) {
        this.result = result;
    }

    public JSONArray getError() {
        return error;
    }

    public void setError(JSONArray error) {
        this.error = error;
    }

    public JSONArray getEmpty() {
        return empty;
    }

    public void setEmpty(JSONArray empty) {
        this.empty = empty;
    }

    @Override
    public String toString() {
//        System.out.println("bug" + JSON.toJSONString(data));
        return String.format(
                "{id:'%s', jobID:'%s', dagID:'%s',title:'%s',runnerID1:'%s',runnerID2:'%s', result:'%s',mismatch:'%s',error:'%s',empty:'%s'}",
                id, jobID, dagID,title,runnerID1,runnerID2, JSON.toJSONString(result), JSON.toJSONString(mismatch), JSON.toJSONString(error), JSON.toJSONString(empty));
    }
}
