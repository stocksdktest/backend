package com.stocktest.gateway;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/testresult")
public class TestResultGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(TestResultGatewayController.class);
    @Value("${server.rehost}")
    private String rehost;//在application.yml中配置的地址参数
    @Value("${server.sehost}")
    private String sehost;//在application.yml中配置的地址参数


    @PostMapping("/new")
    public JSONObject createTestResult(@RequestBody JSONObject params){
        logger.info(params.toString());
        params.put("title","testResult"); //title表示collectionName
        RestTemplate restTemplate = new RestTemplate();//新建一个restTemplate对象
        String uri = rehost + "/api/documents/new"; //这是backend的createNewDocument对应的uri，不用更改
        ResponseEntity<JSONObject> response = restTemplate.postForEntity(uri,params,JSONObject.class); //在sdkVersion的collection中新建一个document

        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @GetMapping()
    public JSONArray getTestResult(){
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/?collectionName={collectionName}";
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "testResult");
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri, JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    /**
     * 问题列表页面已有对比结果的计划信息查询(直接查询所有对比结果集合的信息)
     * @return
     */
    @PostMapping("/questionPlan")
    public JSONArray getQuestionPlanInformation(){ RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "testResult");
        String uri;
        uri = rehost + "/api/questionplan/?collectionName={collectionName}";
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri,JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    /**
     * 问题列表查询
     * @param filterFactors
     * @return
     */
    @PostMapping("/questionList")
    public JSONArray getQuestionList(
            @RequestBody(required = false) JSONArray filterFactors){
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "testResult");
        String uri = rehost + "/api/questionList/?collectionName={collectionName}&filterFactors={filterFactors}";
        params.put("filterFactors",filterFactors.toString());
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri,JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    /**
     *测试报告页面查询（包含从计划集合，对比结果集合）
     * @param filterFactors
     * @return
     */
    @PostMapping("/testReport")
    public JSONArray getTestReport(
            @RequestBody(required = false) JSONArray filterFactors){
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "testResult");
        String uri = rehost + "/api/testReport/?collectionName={collectionName}&filterFactors={filterFactors}";
        params.put("filterFactors",filterFactors.toString());
        ResponseEntity<JSONArray> response = restTemplate.getForEntity(uri,JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }

    @PostMapping()
    public JSONArray getTestResult(
            @RequestBody(required = false) JSONArray
                    filterFactors){ RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        params.put("collectionName", "testResult");
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


    /**
     * 1.更新执行状态
     * 2.带集合名，时间戳访问airflow查询对比结果接口
     * 3.将查询的对比结果插入平台数据库
     * @param filterFactors  时间戳jobID
     * @param collectionName 对比结果表名
     * @return
     */
    @PutMapping("/airflowTestResult/{id}")
    public JSONObject getTestResultFromAirflow(@PathVariable String id,
            @RequestBody JSONArray filterFactors,
            @RequestParam(value = "collectionName",defaultValue = "") String collectionName,
            @RequestParam(value = "planName",defaultValue = "") String planName,
            @RequestParam(value = "quoteDetail",defaultValue = "") String quoteDetail){//0基准，1行情的标志
        RestTemplate restTemplate = new RestTemplate();
        RestTemplate restTemplate2 = new RestTemplate();
        Map<String, Object> params = new HashMap<>();
        String uri;
        //1.更新执行状态
        JSONObject executeStatus = filterFactors.getJSONObject(0);//更新执行状态的条件
        String state = executeStatus.getJSONArray("content").getJSONObject(0).getString("value");
        params.put("collectionName", "testPlan");
        uri = rehost + "/api/documents2/{id}?collectionName={collectionName}";
        params.put("filterFactors",executeStatus.toString());
        params.put("id",id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> requestUpdate = new HttpEntity<>(executeStatus, headers);
        ResponseEntity<JSONObject> response = restTemplate.exchange(uri, HttpMethod.PUT, requestUpdate, JSONObject.class,params);
        if("执行失败".equals(state)){//如果执行状态为failed，无对比结果，则不需要查询和插入结果，直接结束方法
            return executeStatus;
        }
        //从airflow查询对比结果，同时将结果插入本地数据库
        //2.带集合名，时间戳jobID访问airflow查询对比结果接口
        JSONObject jobID = filterFactors.getJSONObject(1);//获取jobID
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder()
                .url("http://221.228.66.83:30618/"+collectionName+"?where="+jobID)
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        try {
            Response res = client.newCall(request).execute();
            //3.将查询的对比结果插入平台数据库
            JSONObject items = JSONObject.parseObject(res.body().string());
            JSONObject result = items.getJSONArray("_items").getJSONObject(0);
            result.put("planName",planName);
            result.put("quoteDetail",quoteDetail);
            result.remove("_created");//去除okhttp请求带的多余参数
            result.remove("_updated");
            result.remove("_etag");
            result.remove("_links");
            uri = rehost + "/api/result/new";
            JSONArray testResult = new JSONArray();
            testResult.add(result);//放入JSONArray,复用后台的新增方法
            ResponseEntity<JSONArray> resultResponse = restTemplate2.postForEntity(uri,testResult,JSONArray.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.getBody();
    }



    @GetMapping("/{id}")
    public JSONObject getTestResultById(@PathVariable String id){
        RestTemplate restTemplate = new RestTemplate();
        String uri = rehost + "/api/documents/{id}?collectionName={collectionName}";

        Map<String, Object> params = new HashMap<>();
        params.put("id",id);
        params.put("collectionName", "testResult");

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
        pathParam.put("collectionName","testResult"); //只需考虑这行，将collectionName更改即可
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
        pathParam.put("collectionName","testResult"); //只需考虑这行，将collectionName更改即可
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
        params.put("collectionName", "testResult");


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestUpdate = new HttpEntity<>(id,headers);
        ResponseEntity<JSONArray> response = restTemplate.exchange(uri,HttpMethod.DELETE,requestUpdate,JSONArray.class,params);
        logger.info(response.getBody().toString());
        return response.getBody();
    }




}
