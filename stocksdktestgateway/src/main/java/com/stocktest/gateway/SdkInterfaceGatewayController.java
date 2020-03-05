package com.stocktest.gateway;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/sdkinterface")
public class SdkInterfaceGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(SdkInterfaceGatewayController.class);
    @Value("${server.rehost}")
    private String rehost;//在application.yml中配置的地址参数
    @PostMapping("/new")
    public JSONObject createSdkInterface(@RequestBody JSONObject params){
        logger.info(params.toString());
        params.put("title","sdkInterface");
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/new";
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri,params,JSONObject.class);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @GetMapping()
    public JSONArray getSdkVersion(){
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/?collectionName={collectionName}";
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "sdkInterface");
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri, JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @PostMapping()
    public JSONArray getSdkInterface(
            @RequestBody(required = false) JSONArray
                    filterFactors){ RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "sdkInterface");
        String uri;
        if(filterFactors==null){
            uri = rehost + "/api/documents/?collectionName={collectionName}";
        }else{
            uri = rehost + "/api/documents/?collectionName={collectionName}&filterFactors={filterFactors}";
            params.put("filterFactors",filterFactors.toString());
        }
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri,
                JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @GetMapping("/{id}")
    public JSONObject getSdkVersionById(@PathVariable String id){
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/{id}?collectionName={collectionName}";
        Map<String, Object> params = new HashMap<>();
        params.put("id",id);
        params.put("collectionName", "sdkInterface");
        ResponseEntity<JSONObject> response = restTemplate.getForEntity(uri, JSONObject.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @PutMapping("/{id}")
    public JSONObject updateDocumentById(@PathVariable String id
            , @RequestBody JSONObject params) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/{id}?collectionName={collectionName}";
        Map<String, String> pathParam = new HashMap<>();
        pathParam.put("id", id);
        pathParam.put("collectionName","sdkInterface");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> requestUpdate = new HttpEntity<>(params, headers);
        ResponseEntity<JSONObject> response = restTemplate.exchange(uri, HttpMethod.PUT, requestUpdate, JSONObject.class,pathParam);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @DeleteMapping("/{id}")
    public JSONArray deleteDocumentById(@PathVariable String id){
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/{id}?collectionName={collectionName}";
        Map<String, Object> params = new HashMap<>();
        params.put("id",id);
        params.put("collectionName", "sdkInterface");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestUpdate = new HttpEntity<>(id,headers);
        ResponseEntity<JSONArray> response = restTemplate.exchange(uri,HttpMethod.DELETE,requestUpdate,JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

}
