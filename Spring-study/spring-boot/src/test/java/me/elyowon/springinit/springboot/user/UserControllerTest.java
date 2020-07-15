package me.elyowon.springinit.springboot.user;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;


    @Test
    public void hello() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("hello"));
    }

    @Test
    public void createUser_JSON() throws Exception {
        String userJson = "{\"username\":\"elyo\", \"password\":\"123\"}";
        //post() << 요청하는부분
        mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username",
                    is(equalTo("elyo"))))
            .andExpect(jsonPath("$.password",
                    is(equalTo("123"))));
    }

    // ContentNegotiatingViewResolver 등록이 되있다고 가정한다.
    // 요청은 json으로 보내고 응답은 xml로 받아보쟈

    @Test
    public void createUser_XML() throws Exception {
        String userJson = "{\"username\":\"elyo\", \"password\":\"123\"}";
        //post() << 요청하는부분
        mockMvc.perform(post("/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_XML)
                .content(userJson))
                .andExpect(status().isOk())
                .andExpect(xpath("/User/username")
                        .string("elyo"))
                .andExpect(xpath("/User/password")
                        .string("123"));
    }
}