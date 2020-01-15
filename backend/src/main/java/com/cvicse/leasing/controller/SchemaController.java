package com.cvicse.leasing.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cvicse.leasing.model.Schema;
import com.cvicse.leasing.service.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/schemas")
public class SchemaController {
    @Autowired
    SchemaService schemaService;

    private static final Logger logger = LoggerFactory.getLogger(SchemaController.class);

    @PostMapping("/new")
    public Schema createSchema(@RequestBody JSONObject params){
        logger.info("create new Schema. ");
        return schemaService.createSchema(params);
    }

    @DeleteMapping("/{id}")
    public List<Schema> deleteSchema(@PathVariable String id){
        logger.info("delete schema "+id);
        schemaService.deleteSchema(id);
        return schemaService.getAllSchemas();
    }

    @GetMapping
    public List<Schema> getSchemas(){
        logger.info("get all schemas");
        List<Schema> schemaList = schemaService.getAllSchemas();
        for (int i = 0; i < schemaList.size(); i++) {
            Schema s = schemaList.get(i);
            System.out.println(s.getSchemaContent());
        }
        return schemaService.getAllSchemas();
    }

    @GetMapping("/{id}")
    public Schema getSchemaById(@PathVariable String id){
        logger.info("get schema by id "+id);
        return schemaService.getSchemaById(id);
    }

    @PutMapping("/{id}")
    public Schema updateSchema(@PathVariable String id
            ,@RequestBody JSONObject params){
        logger.info("update schema by Id "+ id);
        return schemaService.updateSchema(id,params);
    }

    @GetMapping("/{id}/commits")
    public JSONArray getSchemaWithCommitId(@PathVariable String id
            , @RequestParam(value = "commitId", defaultValue = "null") String commitId) {
        if (commitId.equals("null")) {
            try {
                logger.info("Get Schema commits with schemaId "+ id);
                return this.schemaService.trackSchemaChangesWithJavers(id);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schema Not Found.", e);
            }
        } else {
            try {
                logger.info("Cet Schema commit with schemaId and commitId" + commitId);
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(this.schemaService.getSchemaWithJaversCommitId(id, commitId));
                return jsonArray;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schema Not Found.", e);
            }
        }
    }
}
