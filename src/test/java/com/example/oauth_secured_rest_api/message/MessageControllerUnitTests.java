package com.example.oauth_secured_rest_api.message;

import com.example.oauth_secured_rest_api.security.config.SecurityConfig;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Tag("unit")
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@WebMvcTest(MessageController.class)
class MessageControllerUnitTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService service;

    /**
     * Tests for the {@link MessageController} related to finding a message by its ID and owner.
     * Includes tests for successful retrieval, handling of non-existent messages,
     * permission checks, invalid ID formats, and unauthorized access.
     */
    @Nested
    @DisplayName("findByIdAndOwner_controller Tests")
    class FindByIdAndOwnerControllerTests {

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that the correct message is returned when it exists.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void get_ShouldGetMessage_WhenItExists() throws Exception {

            given(service.findByIdAndOwner(1L, "jack"))
                    .willReturn(new Message(1L, "testData1", "jack"));

            mockMvc.perform(get("/messages/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("testData1"))
                    .andExpect(jsonPath("$.owner").value("jack"));

            verify(service, times(1)).findByIdAndOwner(1L, "jack");
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 404 status is returned when the message does not exist.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void get_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {

            given(service.findByIdAndOwner(99L, "jack"))
                    .willThrow(new EntityNotFoundException("Message not found"));

            mockMvc.perform(get("/messages/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Message not found"));

            verify(service, times(1)).findByIdAndOwner(99L, "jack");
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 404 status is returned when the message belongs to another user.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "ann", roles = "USER")
        void get_ShouldReturn404_WhenMessageDoesNotBelongToUser() throws Exception {

            given(service.findByIdAndOwner(1L, "ann"))
                    .willThrow(new EntityNotFoundException("Message not found"));

            mockMvc.perform(get("/messages/1"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Message not found"));

            verify(service, times(1)).findByIdAndOwner(1L, "ann");
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 400 status is returned when the ID is not a valid number.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void get_ShouldReturn400_WhenIdIsNotANumber() throws Exception {

            mockMvc.perform(get("/messages/abc"))
                    .andExpect(status().isBadRequest());

            verify(service, never()).findByIdAndOwner(anyLong(), anyString());
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 400 status is returned when the ID is negative.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void get_ShouldReturn400_WhenIdIsInvalid() throws Exception {

            mockMvc.perform(get("/messages/-1"))
                    .andExpect(status().isBadRequest());

            verify(service, never()).findByIdAndOwner(anyLong(), anyString());
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 403 status is returned when the user does not have the "USER" role.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "hank", roles = "NON-USER")
        void get_ShouldReturn403_WhenUserHasNoGetRights() throws Exception {

            mockMvc.perform(get("/messages/1"))
                    .andExpect(status().isForbidden());

            verify(service, never()).findByIdAndOwner(anyLong(), anyString());
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 401 status is returned when the user is not authenticated.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        @WithAnonymousUser
        void get_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(get("/messages/1"))
                    .andExpect(status().isUnauthorized());

            verify(service, never()).findByIdAndOwner(anyLong(), anyString());
        }
    }

    /**
     * Tests for {@link MessageController#findAllMessagesByOwner(String, int, int, String, String)}.
     * This nested class verifies different behaviors of retrieving messages by owner,
     * including pagination, sorting, error handling for invalid parameters, and access control.
     */
    @Nested
    @DisplayName("findAllMessagesByOwner_controller Tests")
    class FindAllMessagesByOwnerControllerTests {

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that messages are returned in a paged and sorted manner by default.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void get_shouldReturnPagedAndSortedMessagesByDefault() throws Exception {
            List<Message> messages = List.of(
                    new Message(1L, "First Message", "jack"),
                    new Message(2L, "Second Message", "jack")
            );

            given(service.findAllByOwner(
                    "jack", 0, 2, Sort.by(Sort.Direction.ASC, "id"))
            ).willReturn(messages);

            mockMvc.perform(get("/messages")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("First Message"))
                    .andExpect(jsonPath("$[1].title").value("Second Message"));

            verify(service, times(1)).findAllByOwner("jack", 0, 2, Sort.by(Sort.Direction.ASC, "id"));
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that pagination works correctly when requesting a specific page and size.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void get_shouldReturnPagedMessages() throws Exception {
            List<Message> messages = List.of(
                    new Message(2L, "Second Message", "jack")
            );

            given(service.findAllByOwner(
                    "jack", 1, 1, Sort.by(Sort.Direction.ASC, "id"))
            ).willReturn(messages);

            mockMvc.perform(get("/messages")
                            .param("page", "1")
                            .param("size", "1")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(2))
                    .andExpect(jsonPath("$[0].title").value("Second Message"))
                    .andExpect(jsonPath("$[0].owner").value("jack"));

            verify(service, times(1)).findAllByOwner("jack", 1, 1, Sort.by(Sort.Direction.ASC, "id"));
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that sorting messages in descending order works correctly.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void get_shouldReturnSortedMessages() throws Exception {
            List<Message> messages = List.of(
                    new Message(6L, "6 - Message", "jack"),
                    new Message(5L, "5 - Message", "jack")
            );

            given(service.findAllByOwner(
                    "jack", 0, 2, Sort.by(Sort.Direction.DESC, "id"))
            ).willReturn(messages);

            mockMvc.perform(get("/messages")
                            .param("page", "0")
                            .param("size", "2")
                            .param("direction", "desc")
                            .param("sort", "id")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(6))
                    .andExpect(jsonPath("$[0].title").value("6 - Message"))
                    .andExpect(jsonPath("$[0].owner").value("jack"))
                    .andExpect(jsonPath("$[1].id").value(5))
                    .andExpect(jsonPath("$[1].title").value("5 - Message"))
                    .andExpect(jsonPath("$[1].owner").value("jack"));

            verify(service, times(1)).findAllByOwner("jack", 0, 2, Sort.by(Sort.Direction.DESC, "id"));
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that an empty page is returned when the page is out of bounds.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void get_shouldReturnEmptyPageWhenOutOfBounds() throws Exception {

            given(service.findAllByOwner("jack", 100, 2, Sort.by(Sort.Direction.ASC, "id")))
                    .willReturn(Collections.emptyList());

            mockMvc.perform(get("/messages")
                            .param("size", "100")
                            .param("page", "2")
                            .param("direction", "asc")
                            .param("sort", "id")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(service, times(1)).findAllByOwner(anyString(), anyInt(), anyInt(), any(Sort.class));
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that a 403 Forbidden status is returned when the user does not have GET rights.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "hank", roles = "NON-USER")
        void getAll_ShouldReturn403_WhenUserHasNoGetRights() throws Exception {

            mockMvc.perform(get("/messages")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(service, never()).findAllByOwner(anyString(), anyInt(), anyInt(), any(Sort.class));
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that a 401 Unauthorized status is returned when the user is not authenticated.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        void getAll_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(get("/messages")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());

            verify(service, never()).findAllByOwner(anyString(), anyInt(), anyInt(), any(Sort.class));
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that a 400 Bad Request status is returned when pagination parameters are negative.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void get_shouldReturn400_WhenPaginationParamsAreNegative() throws Exception {

            mockMvc.perform(get("/messages")
                            .param("page", "-1")
                            .param("size", "-5")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).findAllByOwner(anyString(), anyInt(), anyInt(), any(Sort.class));
        }
    }

    /**
     * Tests for the {@link MessageController} related to message creation.
     * Includes tests for successful creation, invalid input, unauthorized access,
     * and insufficient permissions.
     */
    @Nested
    @DisplayName("createMessage_controller Tests")
    class CreateMessageControllerTests {

        /**
         * Test for {@link MessageService#createMessage(Message, String)}.
         * Verifies that a new message is created successfully.
         */
        @Test
        @Tag("createMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void create_ShouldCreateNewMessage() throws Exception {

            when(service.createMessage(any(Message.class), eq("jack")))
                    .thenReturn(new Message(1L, "Title", "jack"));

            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/messages/1"));

            verify(service, times(1)).createMessage(any(Message.class), eq("jack"));
        }

        /**
         * Test for {@link MessageService#createMessage(Message, String)}.
         * Verifies that a 400 Bad Request status is returned when invalid input is provided.
         */
        @Test
        @Tag("createMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void create_ShouldReturn400_WhenInvalidInput() throws Exception {

            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}") // Пустое тело
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }

        /**
         * Test for {@link MessageService#createMessage(Message, String)}.
         * Verifies that a 403 Forbidden status is returned when the user has no create rights.
         */
        @Test
        @Tag("createMessage_controller")
        @WithMockUser(username = "hank", roles = "NON-USER")
        void create_ShouldReturn403_WhenUserHasNoCreateRights() throws Exception {

            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"hank\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(service, never()).createMessage(any(Message.class), eq("hank"));
        }

        /**
         * Test for {@link MessageService#createMessage(Message, String)}.
         * Verifies that a 401 Unauthorized status is returned when the user is not authenticated.
         */
        @Test
        @Tag("createMessage_controller")
        void create_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"elvis\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }
    }

    /**
     * Tests for the {@link MessageController} related to message updates.
     * Includes tests for updating existing messages, handling non-existent messages,
     * permissions checks, invalid inputs, and unauthorized access.
     */
    @Nested
    @DisplayName("updateMessage_controller Tests")
    class UpdateMessageControllerTests {

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a message is updated when it exists.
         */
        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void update_ShouldUpdateMessage_WhenItExists() throws Exception {

            mockMvc.perform(put("/messages/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).updateMessage(eq(1L), any(Message.class), eq("jack"));
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 404 Not Found status is returned when the message does not exist.
         */
        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void update_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {

            doThrow(new EntityNotFoundException())
                    .when(service).updateMessage(
                            eq(1L), any(Message.class), eq("jack")
                    );

            mockMvc.perform(put("/messages/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(service, times(1)).updateMessage(eq(1L), any(Message.class), eq("jack"));
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 404 Not Found status is returned when the message does not belong to the user.
         */
        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "ann", roles = "USER")
        void update_ShouldReturn404_WhenMessageDoesNotBelongToUser() throws Exception {

            doThrow(new EntityNotFoundException())
                    .when(service).updateMessage(
                            eq(1L), any(Message.class), eq("ann")
                    );

            mockMvc.perform(put("/messages/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"ann\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(service, times(1)).updateMessage(eq(1L), any(Message.class), eq("ann"));
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 400 Bad Request status is returned when the request body is invalid.
         */
        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void update_ShouldReturn400_WhenRequestBodyIsInvalid() throws Exception {

            mockMvc.perform(put("/messages/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}") // Пустое тело
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 400 Bad Request status is returned when the ID is not a number.
         */
        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void update_ShouldReturn400_WhenIdIsNotANumber() throws Exception {

            mockMvc.perform(put("/messages/abs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 400 Bad Request status is returned when the ID is invalid.
         */
        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void update_ShouldReturn400_WhenIdIsInvalid() throws Exception {

            mockMvc.perform(put("/messages/-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 403 Forbidden status is returned when the user has no update rights.
         */
        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "hank", roles = "NON-USER")
        void update_ShouldReturn403_WhenUserHasNoUpdateRights() throws Exception {

            doThrow(new AccessDeniedException("Not the owner"))
                    .when(service).updateMessage(
                            eq(1L), any(Message.class), eq("hank")
                    );

            mockMvc.perform(put("/messages/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"hank\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(service, never()).updateMessage(eq(1L), any(Message.class), eq("hank"));
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 401 Unauthorized status is returned when the user is not authenticated.
         */
        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(put("/messages/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }
    }

    /**
     * Tests for the {@link MessageController} related to message deletion.
     * Includes tests for successful deletion, non-existent messages, permissions checks,
     * invalid IDs, and unauthorized access.
     */
    @Nested
    @DisplayName("deleteMessage_controller Tests")
    class DeleteMessageControllerTests {

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a message is deleted when it exists.
         */
        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void delete_ShouldDeleteMessage_WhenItExists() throws Exception {

            mockMvc.perform(delete("/messages/1"))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).deleteMessage(1L, "jack");
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 404 Not Found status is returned when the message does not exist.
         */
        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void delete_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {

            doThrow(new EntityNotFoundException())
                    .when(service).deleteMessage(99L, "jack");

            mockMvc.perform(delete("/messages/99"))
                    .andExpect(status().isNotFound());

            verify(service, times(1)).deleteMessage(99L, "jack");
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 404 Not Found status is returned when the message does not belong to the user.
         */
        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "ann", roles = "USER")
        void delete_ShouldReturn404_WhenMessageDoesNotBelongToUser() throws Exception {

            doThrow(new EntityNotFoundException())
                    .when(service).deleteMessage(1L, "ann");

            mockMvc.perform(delete("/messages/1"))
                    .andExpect(status().isNotFound());

            verify(service, times(1)).deleteMessage(1L, "ann");
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 400 Bad Request status is returned when the ID is not a number.
         */
        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void delete_ShouldReturn400_WhenIdIsNotANumber() throws Exception {

            mockMvc.perform(delete("/messages/abc")) // Некорректный ID
                    .andExpect(status().isBadRequest());

            verify(service, never()).deleteMessage(any(), anyString());
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 400 Bad Request status is returned when the ID is invalid.
         */
        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "jack", roles = "USER")
        void delete_ShouldReturn400_WhenIdIsInvalid() throws Exception {

            mockMvc.perform(delete("/messages/-1")) // Отрицательный ID
                    .andExpect(status().isBadRequest());

            verify(service, never()).deleteMessage(anyLong(), anyString());
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 403 Forbidden status is returned when the user has no delete rights.
         */
        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "hank", roles = "NON-USER")
        void delete_ShouldReturn403_WhenUserHasNoDeleteRights() throws Exception {

            doThrow(new AccessDeniedException("Not the owner"))
                    .when(service).deleteMessage(1L, "hank");

            mockMvc.perform(delete("/messages/1"))
                    .andExpect(status().isForbidden());

            verify(service, never()).deleteMessage(anyLong(), anyString());
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 401 Unauthorized status is returned when the user is not authenticated.
         */
        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(delete("/messages/1"))
                    .andExpect(status().isUnauthorized());

            verify(service, never()).deleteMessage(anyLong(), anyString());
        }
    }
}
