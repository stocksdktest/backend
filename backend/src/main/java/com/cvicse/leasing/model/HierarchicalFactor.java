package com.cvicse.leasing.model;

import com.alibaba.fastjson.JSONObject;

/**
 * 级联查询时的参数
 */
public class HierarchicalFactor {
    private String collectionName;
    private String localParam;
    private String foreignParam;
    private String as;

    public HierarchicalFactor(JSONObject params){
        collectionName = params.getString("collectionName");
        localParam = params.getString("localParam");
        foreignParam = params.getString("foreignParam");
        as = params.getString("as");
    }
    public String getAs() {
        return as;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getForeignParam() {
        return foreignParam;
    }

    public String getLocalParam() {
        return localParam;
    }
    @Override
    public String toString() {
        return String.format(
                "HierarchicalFactor[collectionName=%s, localParam='%s',foreignParam='%s', as='%s']",
                collectionName, localParam,foreignParam, as);
    }
}
