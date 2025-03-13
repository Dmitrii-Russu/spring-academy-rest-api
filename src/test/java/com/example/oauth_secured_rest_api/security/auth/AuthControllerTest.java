package com.example.oauth_secured_rest_api.security.auth;

import com.example.oauth_secured_rest_api.security.config.SecurityConfig;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    TokenService tokenService;

    @Test
    @Tag("token")
    void tokenWhenAnonymousThenStatusIsUnauthorized() throws Exception {
        this.mvc.perform(post("/token")).andExpect(status().isUnauthorized());
    }

    @Test
    @Tag("token")
    void tokenWithBasicThenGetToken() throws Exception {

        MvcResult result = this.mvc.perform(post("/token")
                .with(httpBasic("jack", "asd")))
                .andExpect(status().isOk()).andReturn();

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }

}
