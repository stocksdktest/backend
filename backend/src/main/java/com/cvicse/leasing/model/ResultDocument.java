package com.cvicse.leasing.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.annotation.Id;

//import com.cvicse.leasing.auth.framwork.model.BaseEntity;

@org.springframework.data.mongodb.core.mapping.Document(collection = "Document")
public class ResultDocument {
    @Id
    private String _id;

    private String jobID;

    private String dagID;

    private String planName;

    private String runnerID1;

    private String runnerID2;

    private JSONArray mismatch;

    private JSONObject result;

    private JSONArray error;

    private JSONArray empty;

    private String quoteDetail;

    private JSONArray detailType;

    private String planID;

    private String reportFlag;

    private String error_msg;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
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

    public String getQuoteDetail() {
        return quoteDetail;
    }

    public void setQuoteDetail(String quoteDetail) {
        this.quoteDetail = quoteDetail;
    }

    public JSONArray getDetailType() {
        return detailType;
    }

    public void setDetailType(JSONArray detailType) {
        this.detailType = detailType;
    }

    public String getPlanID() {
        return planID;
    }

    public void setPlanID(String planID) {
        this.planID = planID;
    }

    public String getReportFlag() {
        return reportFlag;
    }

    public void setReportFlag(String reportFlag) {
        this.reportFlag = reportFlag;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    @Override
    public String toString() {
//        System.out.println("bug" + JSON.toJSONString(data));
        return String.format(
                "{_id:'%s', jobID:'%s', dagID:'%s',planName:'%s',runnerID1:'%s',runnerID2:'%s', result:'%s',mismatch:'%s',error:'%s',empty:'%s',quoteDetail:'%s',detailType:'%s',planID:'%s',reportFlag:'%s',error_msg:'%s'}",
                _id, jobID, dagID,planName,runnerID1,runnerID2, JSON.toJSONString(result), JSON.toJSONString(mismatch), JSON.toJSONString(error), JSON.toJSONString(empty),quoteDetail,JSON.toJSONString(detailType),planID,reportFlag,error_msg);
    }
}
