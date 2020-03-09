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
@RequestMapping("/api/sdkversion")
public class SdkVersionGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(SdkVersionGatewayController.class);
    @Value("${server.rehost}")
    private String rehost;//在application.yml中配置的地址参数

    @PostMapping("/new")
    public JSONObject createSdkVersion(@RequestBody JSONObject params){
        logger.info(params.toString());
        params.put("title","sdkVersion"); //title表示collectionName
        RestTemplate restTemplate = new RestTemplate();//新建一个restTemplate对象
        String uri = rehost + "/api/documents/new"; //这是backend的createNewDocument对应的uri，不用更改
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri,params,JSONObject.class); //在sdkVersion的collection中新建一个document

        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @PostMapping()
    public JSONArray getSdkVersion(
            @RequestBody(required = false) JSONArray
                    filterFactors){ RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "sdkVersion");
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
    public JSONObject getSdkVersionById(@PathVariable String id
        ,@RequestBody(required = false) JSONArray filterFactors){
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("id",id);
        params.put("collectionName", "contract");
        String uri;
        if(filterFactors==null){
            uri = "http://localhost:8088/api/documents/{id}?collectionName={collectionName}";
        }else{
            uri = "http://localhost:8088/api/documents/{id}?collectionName={collectionName}&hierarchical={hierarchical}&filterFactors={filterFactors}";
            params.put("hierarchical","1");
            params.put("filterFactors",filterFactors.toString());
        }
        ResponseEntity<JSONObject> response = restTemplate.getForEntity(uri, JSONObject.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @PutMapping("/{id}")
    public JSONObject updateDocumentById(@PathVariable String id
            , @RequestBody JSONObject params
            , @RequestParam(value = "embeddedDocument",defaultValue = "false") boolean embeddedDocument) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = "http://localhost:8088/api/documents/{id}?collectionName={collectionName}&embeddedDocument={embeddedDocument}"; //update方法对应的uri，不用更改


        Map<String, String> pathParam = new HashMap<>();
        pathParam.put("id", id);
        pathParam.put("collectionName","i2ec"); //只需考虑这行，将collectionName更改即可
        if(embeddedDocument)
            pathParam.put("embeddedDocument","true");
        else pathParam.put("embeddedDocument","false");
        //http头信息，不用更改
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
        String uri = rehost + "/api/documents/{id}?collectionName={collectionName}";//delete对应的uri，不用更改

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
