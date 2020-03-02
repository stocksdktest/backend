package com.stocktest.gateway;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GatewayApplicationTests {

    @Autowired
    SdkVersionGatewayController sdkVersionGatewayController;

    @Test
    public void contextLoads() {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("key","name");
        jsonObject.put("value","tc");
        jsonArray.add(jsonObject);

    }

}
