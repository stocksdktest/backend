package com.cvicse.leasing;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cvicse.leasing.service.DocumentService;
//import com.cvicse.leasingauthmanage.model.Action;
//import com.cvicse.leasingauthmanage.model.Role;
//import com.cvicse.leasingauthmanage.model.User;
//import com.cvicse.leasingauthmanage.repository.RoleRepository;
//import com.cvicse.leasingauthmanage.repository.UserRepository;
//import com.cvicse.leasingauthmanage.service.RoleService;
//import com.cvicse.leasingauthmanage.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebMvc
@AutoConfigureMockMvc
public class CtlTest {
//    private static final Logger logger = LoggerFactory.getLogger(CtlTest.class);
//
//    @Autowired
//    private MockMvc mockMvc;
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private RoleService roleService;
//
//    @Autowired
//    private DocumentService documentService;
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    private List<User> users = new LinkedList<>();
//    private List<Role> roles = new LinkedList<>();
//
//    @Before
//    public void init() {
//        //新建用户 A B C, 账号密码均相同
//        User user = userService.newUser(new User("A", "A"));
//        user.setPassword("A");
//        users.add(user);
//        user = userService.newUser(new User("B", "B"));
//        user.setPassword("B");
//        users.add(user);
//        user = userService.newUser(new User("C", "C"));
//        user.setPassword("C");
//        users.add(user);
//        //新建角色, one two three. 并且给予相对应的权限
//        //one
//        Role one = new Role("one");
//        //action
//        Action actOne = new Action(
//                "Document",
//                "ADD.DELETE.UPDATE.QUERY",
//                "{\n" +
//                        "  \"collectionName\": \"\"\n" +
//                        "}"
//        );
//        one.setActions(Collections.singletonList(actOne));
//        //two
//        Role two = new Role("two");
//        //action
//        Action actTwo = new Action(
//                "Document",
//                "ADD.DELETE.UPDATE.QUERY",
//                "{}"
//        );
//        two.setActions(Collections.singletonList(actTwo));
//        //Three
//        Role three = new Role("three");
//        //Action
//        Action actThree = new Action(
//                "Document",
//                "ADD.DELETE.UPDATE.QUERY",
//                "{}"
//        );
//        three.setActions(Collections.singletonList(actThree));
//        //添加到list
//        roles.add(roleService.newRole(one));
//        roles.add(roleService.newRole(two));
//        roles.add(roleService.newRole(three));
//        //把角色赋予给指定用户
//        for (int i = 0; i < users.size(); ++i) {
//            User u = userService.updateRole2User(
//                    users.get(i).getId(),
//                    roles.subList(i, i + 1)
//            );
//            logger.info(String.format("The User [%s] is in Role %s. And the Actions are %s",
//                    u.getUsername(),
//                    JSON.toJSONString(u.getAuthorities()),
//                    JSON.toJSONString(u.getActions())
//                    )
//            );
//        }
//    }
//
//
//    @Test
//    public void run() throws Exception {
//        MockHttpSession session = mockUserLogin(users.get(0));      //A登录
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("a", "qqqq");
//        jsonObject.put("title", "cashFlow");
//        mockMvc.perform(post("/api/documents/new?schemaId=000&collectionName=cashFlow")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(jsonObject.toJSONString())
//                .session(session));
//        //获取所有
//        mockMvc.perform(get("/api/documents?collectionName=cashFlow")
//                .contentType(MediaType.APPLICATION_JSON)
//                .session(session)
//        ).andExpect(status().isOk());
//        //修改B登录
//        session = mockUserLogin(users.get(1));
//        jsonObject = new JSONObject();
//        jsonObject.put("b", "this is b's content");
//        jsonObject.put("title", "cashFlow");
//        //B
//        mockMvc.perform(post("/api/documents/new?schemaId=111&collectionName=cashFlow")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(jsonObject.toJSONString())
//                .session(session));
//        //获取所有
//        String response = mockMvc.perform(get("/api/documents?collectionName=cashFlow")
//                .contentType(MediaType.APPLICATION_JSON)
//                .session(session)
//        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
//
//        JSONArray arr = (JSONArray) JSON.parse(response);
//    }
//
//
//    //用户登录
//    private MockHttpSession mockUserLogin(User user) throws Exception {
//        //用户登录
//        MvcResult result = mockMvc.perform(post("/api/auth/login")
//                .content(JSON.toJSONString(user))
//                .contentType(MediaType.APPLICATION_JSON)
//        ).andExpect(status().isOk()).andReturn();
//        //session作为token凭证
//        return (MockHttpSession) result.getRequest().getSession();
//    }

}
