package com.stocktest.gateway;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/testinformation")
public class TestInformationGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(TestInformationGatewayController.class);
    @Value("${server.rehost}")
    private String rehost;//在application.yml中配置的地址参数


    @PostMapping("/new")
    public JSONObject createTestInformation(@RequestBody JSONObject params){
        logger.info(params.toString());
        params.put("title","testInformation"); //title表示collectionName
        RestTemplate restTemplate = new RestTemplate();//新建一个restTemplate对象
        String uri = rehost + "/api/documents/new"; //这是backend的createNewDocument对应的uri，不用更改
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri,params,JSONObject.class); //在sdkVersion的collection中新建一个document

        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @GetMapping()
    public JSONArray getTestInformation(){
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/?collectionName={collectionName}";
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "testInformation");
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri, JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    /**
     * 前台传回两个版本名，根据版本名查询比较得出相同用例
     * @param twoVersion  两个版本信息
     * @return
     */
    @PostMapping("/sameTestcases")
    public JSONArray getSameTestcase(@RequestBody JSONArray twoVersion){
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/sameTestcases/?collectionName={collectionName}&twoVersion={twoVersion}";
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "testInformation");
        params.put("twoVersion",twoVersion.toString());
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri, JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @PostMapping()
    public JSONArray getTestInformation(
            @RequestBody(required = false) JSONArray
                    filterFactors){ RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "testInformation");
        String uri;
            if(filterFactors==null){
            uri = rehost + "/api/documents/?collectionName={collectionName}";
        }else{
            uri = rehost + "/api/documents/?collectionName={collectionName}&filterFactors={filterFactors}";
            params.put("filterFactors",filterFactors.toString());
        }
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri,JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @PostMapping("/testcases/{id}")
    public JSONObject getTestCases(@PathVariable String id,
            @RequestBody(required = false) JSONArray filterFactors){
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("id",id);
        params.put("collectionName", "testInformation");
        String uri;
        if(filterFactors==null){
            uri = rehost + "/api/documents/?collectionName={collectionName}";
        }else{
            uri = rehost + "/api/documents/{id}?collectionName={collectionName}&filterFactors={filterFactors}";
            params.put("filterFactors",filterFactors.toString());
        }
        ResponseEntity<JSONObject> response = restTemplate.getForEntity(uri,JSONObject.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }


    @GetMapping("/{id}")
    public JSONObject getTestInformationById(@PathVariable String id){
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/{id}?collectionName={collectionName}";

        Map<String, Object> params = new HashMap<>();
        params.put("id",id);
        params.put("collectionName", "testInformation");

        ResponseEntity<JSONObject> response = restTemplate.getForEntity(uri, JSONObject.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    /**
     * 内嵌文档数组的插入、删除
     * @param id
     * @param params
     * @param embeddedDocument
     * @return
     */
    @PutMapping("/{id}")
    public JSONObject updateDocumentById(@PathVariable String id
            , @RequestBody JSONObject params
            , @RequestParam(value = "embeddedDocument",defaultValue = "false") boolean embeddedDocument) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/{id}?collectionName={collectionName}&embeddedDocument={embeddedDocument}"; //update方法对应的uri，不用更改

        Map<String, String> pathParam = new HashMap<>();
        pathParam.put("id", id);
        pathParam.put("collectionName","testInformation"); //只需考虑这行，将collectionName更改即可
        if(embeddedDocument){
            pathParam.put("embeddedDocument","true");
        }else{
            pathParam.put("embeddedDocument","false");
        }
        //http头信息，不用更改
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> requestUpdate = new HttpEntity<>(params, headers);
        ResponseEntity<JSONObject> response = restTemplate.exchange(uri, HttpMethod.PUT, requestUpdate, JSONObject.class,pathParam);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    /**
     * 文档具体值的更新（包括内嵌文档值和非内嵌文档值）
     * @param id
     * @param params
     * @return
     */
    @PutMapping("/update/{id}")
    public JSONObject updateDocumentById2(@PathVariable String id
            , @RequestBody JSONObject params
            , @RequestParam(value = "embeddedDocument",defaultValue = "true") boolean embeddedDocument) {
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents2/{id}?collectionName={collectionName}"; //update方法对应的uri，不用更改
        Map<String, String> pathParam = new HashMap<>();
        pathParam.put("id", id);
        pathParam.put("collectionName","testInformation"); //只需考虑这行，将collectionName更改即可
        if(embeddedDocument){
            pathParam.put("embeddedDocument","true");
        }else{
            pathParam.put("embeddedDocument","false");
        }
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
        params.put("collectionName", "testInformation");


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestUpdate = new HttpEntity<>(id,headers);
        ResponseEntity<JSONArray> response = restTemplate.exchange(uri,HttpMethod.DELETE,requestUpdate,JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }




}
