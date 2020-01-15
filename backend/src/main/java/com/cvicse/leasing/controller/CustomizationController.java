package com.cvicse.leasing.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cvicse.leasing.model.Customization;
import com.cvicse.leasing.service.CustomizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/customizations")
public class CustomizationController {
    @Autowired
    CustomizationService schemaService;

    private static final Logger logger = LoggerFactory.getLogger(CustomizationController.class);

    @PostMapping("/new")
    public Customization createCustomization(@RequestBody JSONObject params){
        logger.info("create new Customization. ");
        return schemaService.createCustomization(params);
    }

    @DeleteMapping("/{id}")
    public List<Customization> deleteCustomization(@PathVariable String id){
        logger.info("delete schema "+id);
        schemaService.deleteCustomization(id);
        return schemaService.getAllCustomizations();
    }

    @GetMapping
    public List<Customization> getCustomizations(){
        logger.info("get all schemas");
        List<Customization> schemaList = schemaService.getAllCustomizations();
        for (int i = 0; i < schemaList.size(); i++) {
            Customization s = schemaList.get(i);
            System.out.println(s.getCustomizationContent());
        }
        return schemaService.getAllCustomizations();
    }

    @GetMapping("/{id}")
    public Customization getCustomizationById(@PathVariable String id){
        logger.info("get schema by id "+id);
        return schemaService.getCustomizationById(id);
    }

    @PutMapping("/{id}")
    public Customization updateCustomization(@PathVariable String id
            ,@RequestBody JSONObject params){
        logger.info("update schema by Id "+ id);
        return schemaService.updateCustomization(id,params);
    }

    @GetMapping("/{id}/commits")
    public JSONArray getCustomizationWithCommitId(@PathVariable String id
            , @RequestParam(value = "commitId", defaultValue = "null") String commitId) {
        if (commitId.equals("null")) {
            try {
                logger.info("Get Customization commits with schemaId "+ id);
                return this.schemaService.trackCustomizationChangesWithJavers(id);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customization Not Found.", e);
            }
        } else {
            try {
                logger.info("Cet Customization commit with schemaId and commitId" + commitId);
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(this.schemaService.getCustomizationWithJaversCommitId(id, commitId));
                return jsonArray;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customization Not Found.", e);
            }
        }
    }
}
