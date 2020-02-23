package com.stocktest.gateway;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/sdkversion")
public class SdkVersionGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(SdkVersionGatewayController.class);
    @PostMapping("/new")
    public JSONObject createSdkVersion(@RequestBody JSONObject params){
        logger.info(params.toString());
        params.put("title","sdkVersion");
        RestTemplate restTemplate = new RestTemplate();

//        docker 部署的每个服务运行在不同的容器下，主机的IP都不同
//        通讯方式：1、通过宿主机的IP和端口通讯
//        通讯方式2、设置自定义网络 通过容器名访问

        String uri = "http://sddm-backend:8088/api/documents/new";
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri,params,JSONObject.class);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @GetMapping()
    public JSONArray getSdkVersion(){
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://sddm-backend:8088/api/documents/?collectionName={collectionName}";
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "sdkVersion");
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri, JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @GetMapping("/{id}")
    public JSONObject getSdkVersionById(@PathVariable String id){
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8088/api/documents/{id}?collectionName={collectionName}";
        Map<String, Object> params = new HashMap<>();
        params.put("id",id);
        params.put("collectionName", "sdkVersion");
        ResponseEntity<JSONObject> response = restTemplate.getForEntity(uri, JSONObject.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @PutMapping("/{id}")
    public JSONObject updateDocumentById(@PathVariable String id
            , @RequestBody JSONObject params) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8088/api/documents/{id}?collectionName={collectionName}";
        Map<String, String> pathParam = new HashMap<>();
        pathParam.put("id", id);
        pathParam.put("collectionName","sdkVersion");
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
        String uri = "http://localhost:8088/api/documents/{id}?collectionName={collectionName}";
        Map<String, Object> params = new HashMap<>();
        params.put("id",id);
        params.put("collectionName", "sdkVersion");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestUpdate = new HttpEntity<>(id,headers);
        ResponseEntity<JSONArray> response = restTemplate.exchange(uri,HttpMethod.DELETE,requestUpdate,JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

}
