package com.example.oauth_secured_rest_api.message;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integrationMockMvc")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageControllerMockMvcIntegrationTest {

    private final Map<String, String> tokens = new HashMap<>();

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void initTokens() throws Exception {
        tokens.put("jack", getTokenForUser("jack", "asd"));
        tokens.put("ann", getTokenForUser("ann", "zxc"));
        tokens.put("hank", getTokenForUser("hank", "qwe"));
    }

    private String getTokenForUser(String username, String password) throws Exception {

        return mockMvc.perform(post("/token")
                        .with(httpBasic(username, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Nested
    @DisplayName("findByIdAndOwner_controller Tests")
    class FindByIdAndOwnerControllerTests {

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldGetMessage_WhenItExists() throws Exception {

            mockMvc.perform(get("/messages/5")
                    .header("Authorization", "Bearer " + tokens.get("jack")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(5))
                    .andExpect(jsonPath("$.title").value("testData5"))
                    .andExpect(jsonPath("$.owner").value("jack"));
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {

            mockMvc.perform(get("/messages/99")
                    .header("Authorization", "Bearer " + tokens.get("jack")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn404_WhenMessageDoesNotBelongToUser() throws Exception {

            mockMvc.perform(get("/messages/1")
                    .header("Authorization", "Bearer " + tokens.get("ann")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn400_WhenIdIsNotANumber() throws Exception {

            mockMvc.perform(get("/messages/abc")
                    .header("Authorization", "Bearer " + tokens.get("jack")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn400_WhenIdIsInvalid() throws Exception {

            mockMvc.perform(get("/messages/-1")
                    .header("Authorization", "Bearer " + tokens.get("jack")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn403_WhenUserHasNoGetRights() throws Exception {

            mockMvc.perform(get("/messages/13")
                    .header("Authorization", "Bearer " + tokens.get("hank")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(get("/messages/1")).andExpect(status().isUnauthorized());
        }

    }

    @Nested
    @DisplayName("findAllMessagesByOwner_controller Tests")
    class FindAllMessagesByOwnerControllerTests {

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnPagedAndSortedMessagesByDefault() throws Exception {

            mockMvc.perform(get("/messages")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("testData1"))
                    .andExpect(jsonPath("$[1].title").value("testData2"));
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnPagedMessages() throws Exception {

            mockMvc.perform(get("/messages")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .param("page", "1")
                    .param("size", "1")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[0].title").value("testData2"))
                    .andExpect(jsonPath("$[0].owner").value("jack"));
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnSortedMessages() throws Exception {

            mockMvc.perform(get("/messages")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .param("page", "0")
                    .param("size", "2")
                    .param("direction", "desc")
                    .param("sort", "id")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(6))
                    .andExpect(jsonPath("$[0].title").value("testData6"))
                    .andExpect(jsonPath("$[0].owner").value("jack"))
                    .andExpect(jsonPath("$[1].id").value(5))
                    .andExpect(jsonPath("$[1].title").value("testData5"))
                    .andExpect(jsonPath("$[1].owner").value("jack"));
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnEmptyPageWhenOutOfBounds() throws Exception {

            mockMvc.perform(get("/messages")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .param("size", "100")
                    .param("page", "2")
                    .param("direction", "asc")
                    .param("sort", "id")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void getAll_ShouldReturn403_WhenUserHasNoGetRights() throws Exception {

            mockMvc.perform(
                    get("/messages")
                    .header("Authorization", "Bearer " + tokens.get("hank"))
                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isForbidden());
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void getAll_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(
                    get("/messages").accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturn400_WhenPaginationParamsAreNegative() throws Exception {

            mockMvc.perform(get("/messages")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .param("page", "-1")
                    .param("size", "-5")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    @DisplayName("createMessage_controller Tests")
    class CreateMessageControllerTests {

        @Test
        @Tag("createMessage_controller")
        void create_ShouldCreateNewMessage() throws Exception {

            mockMvc.perform(
                    post("/messages")
                    .header("Authorization", "Bearer " + tokens.get("ann"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Title\",\"owner\":\"ann\"}")
                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isCreated())
                    .andExpect(header().string(
                            HttpHeaders.LOCATION, "http://localhost/messages/14")
                    );
        }

        @Test
        @Tag("createMessage_controller")
        void create_ShouldReturn400_WhenInvalidInput() throws Exception {

            mockMvc.perform(
                    post("/messages")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}") // Пустое тело
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Tag("createMessage_controller")
        void create_ShouldReturn403_WhenUserHasNoCreateRights() throws Exception {

            mockMvc.perform(
                    post("/messages")
                    .header("Authorization", "Bearer " + tokens.get("hank"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Updated Title\",\"owner\":\"hank\"}")
                    .accept(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isForbidden());
        }

        @Test
        @Tag("createMessage_controller")
        void create_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"elvis\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

    }

    @Nested
    @DisplayName("updateMessage_controller Tests")
    class UpdateMessageControllerTests {

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldUpdateMessage_WhenItExists() throws Exception {

            mockMvc.perform(put("/messages/11")
                    .header("Authorization", "Bearer " + tokens.get("ann"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Updated Title\",\"owner\":\"ann\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {

            mockMvc.perform(put("/messages/99")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn404_WhenMessageDoesNotBelongToUser() throws Exception {

            mockMvc.perform(put("/messages/1")
                    .header("Authorization", "Bearer " + tokens.get("ann"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Updated Title\",\"owner\":\"ann\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn400_WhenRequestBodyIsInvalid() throws Exception {

            mockMvc.perform(put("/messages/1")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}") // Пустое тело
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn400_WhenIdIsNotANumber() throws Exception {

            mockMvc.perform(put("/messages/abs")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn400_WhenIdIsInvalid() throws Exception {

            mockMvc.perform(put("/messages/-1")
                    .header("Authorization", "Bearer " + tokens.get("jack"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn403_WhenUserHasNoUpdateRights() throws Exception {

            mockMvc.perform(put("/messages/1")
                    .header("Authorization", "Bearer " + tokens.get("hank"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Updated Title\",\"owner\":\"hank\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(put("/messages/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

    }

    @Nested
    @DisplayName("deleteMessage_controller Tests")
    class DeleteMessageControllerTests {

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldDeleteMessage_WhenItExists() throws Exception {

            mockMvc.perform(delete("/messages/12")
                    .header("Authorization", "Bearer " + tokens.get("ann")))
                    .andExpect(status().isNoContent());
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {

            mockMvc.perform(delete("/messages/99")
                    .header("Authorization", "Bearer " + tokens.get("jack")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn404_WhenMessageDoesNotBelongToUser() throws Exception {

            mockMvc.perform(delete("/messages/10")
                    .header("Authorization", "Bearer " + tokens.get("jack")))
                    .andExpect(status().isNotFound());
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn400_WhenIdIsNotANumber() throws Exception {

            mockMvc.perform(delete("/messages/abc")
                    .header("Authorization", "Bearer " + tokens.get("jack")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn400_WhenIdIsInvalid() throws Exception {

            mockMvc.perform(delete("/messages/-1")
                    .header("Authorization", "Bearer " + tokens.get("jack")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn403_WhenUserHasNoDeleteRights() throws Exception {

            mockMvc.perform(delete("/messages/1")
                    .header("Authorization", "Bearer " + tokens.get("hank")))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(delete("/messages/1")).andExpect(status().isUnauthorized());
        }

    }

}
