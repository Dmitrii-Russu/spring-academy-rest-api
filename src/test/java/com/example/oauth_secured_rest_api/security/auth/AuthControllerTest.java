package com.example.oauth_secured_rest_api.security.auth;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.core.Authentication;

@WebMvcTest(AuthController.class)
class AuthControllerMockMvcUnitTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    TokenService tokenService;

    @Test
    @Tag("token")
    void tokenWhenAnonymousThenStatusIsUnauthorized() throws Exception {

        this.mvc.perform(post("/token")).andExpect(status().isUnauthorized());

        Mockito.verify(tokenService, Mockito.never())
                .generateToken(Mockito.any(Authentication.class));
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
