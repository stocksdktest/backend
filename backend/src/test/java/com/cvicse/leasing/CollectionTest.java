package com.cvicse.leasing;


import com.alibaba.fastjson.JSONObject;
import com.cvicse.leasing.service.DocumentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CollectionTest {
    @Autowired
    DocumentService documentService;

    @Test
    public void test(){
        JSONObject info = new JSONObject();
        info.put("legalRepresentativeId","511423199002110018");
        info.put("fiel","yingwenmingc");
        info.put("modifier","ghl");
        info.put("busiCustUniqueId","5e42130d3ab64f1fb875a208");
        info.put("custClassification","工程机械类");
        info.put("taxIdentNum","91440101MA59TLXY82");
        info.put("registeredAddress","注册地址录入");
        info.put("custNature","企业法人");
        info.put("taxBank","建行江苏分行");
        info.put("whetherVATGeneralTax","yes");
        info.put("nationTaxRegistCertify","yes");
        info.put("registraDate","2020-01-28");
        info.put("taxMobileNum","18611520001");
        info.put("chooseBusinessCust","苏州和祥贸易有限责任公司");
        info.put("licenseNum","91440101MA59TLXY82");
        info.put("primaryMG","162abe6a23d74b0e9b7b7be3d28ee0d4");
        info.put("taxLandline","0542-1234567");
        info.put("governFinanciPlatformCust","no");
        info.put("numberEmployees","2000");
        info.put("loanCardCodeNum","5114234259512905");
        info.put("assistMG","66807dac470744138a95d24434e3e3d0");
        info.put("registCapCurrency","RMB");
        info.put("registDate","2020-01-28");
        info.put("payableCurrency","RMB");
        info.put("establishmentDate","2020-02-11");
        info.put("unifiedCreditCode","91440101MA59TLXY82");
        info.put("whetherRevoke","no");
        info.put("licenseDate","2029-02-22");
        info.put("legalRepresentative","吕良伟");
        info.put("custSource","电话营销");
        info.put("registeredCapital","20000");
        info.put("customerSize","中型");
        info.put("custId","ZL00000217");
        info.put("mainBusinessScope","主营婴幼儿用品批发");
        info.put("taxAccBankAcc","6225112542251002");
        info.put("chiefDept","e5cda7d2e2a145598ca2a40283fb77d0");
        info.put("businessIncome","22000");
        info.put("groupCust","no");
        info.put("lastModifiedDate","2020-02-11");
        info.put("taxRegistNum","91440101MA59TLXY82");
        info.put("custName", "苏州和祥贸易有限责任公司");
        info.put("validityCodeDocuments","9999-12-31");
        info.put("nationTaxRegistCertifyNum","91440101MA59TLXY82");
        info.put("localTaxRegistCertifyNum","91440101MA59TLXY82");
        info.put("assetSize","100");
        info.put("registraNumType","工商注册号");
        info.put("custIndustry","51");
        info.put("taxAdd","纳税人地址录入");
        info.put("taxAccOpenName", "苏州和祥贸易有限责任公司");
        info.put("location","苏州市");
        ArrayList<String> list = new ArrayList<>();
        list.add("股票");
        list.add("股份");
        info.put("investmentType",list);
        info.put("businessBackground","（私营）有限责任公司");
        info.put("businessType","股份有限公司（非上市");
        info.put("custRole",new ArrayList<String>().add("承租人"));
        info.put("paid-inCapital","16400");
        info.put("custIndustryOurs","modernLogistics");
        JSONObject custBasicInfo = new JSONObject();
        custBasicInfo.put("formData",info);
        custBasicInfo.put("title","custBasicInfo");
        documentService.createDocument("1",custBasicInfo);
    }
}
