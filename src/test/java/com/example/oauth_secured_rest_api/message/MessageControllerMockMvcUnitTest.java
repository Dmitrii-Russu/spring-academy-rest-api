package com.example.oauth_secured_rest_api.message;

import com.example.oauth_secured_rest_api.security.config.SecurityConfig;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@Import(SecurityConfig.class)
@WebMvcTest(MessageController.class)
class MessageControllerMockMvcUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService service;

    @Nested
    @DisplayName("findByIdAndOwner_controller Tests")
    class FindByIdAndOwnerControllerTests {

        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void get_ShouldGetMessage_WhenItExists() throws Exception {

            when(service.findByIdAndOwner(1L, "jack"))
                    .thenReturn(new Message(1L, "testData1", "jack"));

            mockMvc.perform(get("/messages/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("testData1"))
                    .andExpect(jsonPath("$.owner").value("jack"));

            verify(service, times(1)).findByIdAndOwner(1L, "jack");
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void get_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {

            when(service.findByIdAndOwner(99L, "jack")).thenThrow(EntityNotFoundException.class);

            mockMvc.perform(get("/messages/99")).andExpect(status().isNotFound());

            verify(service, times(1)).findByIdAndOwner(99L, "jack");
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "ann", authorities = "SCOPE_USER")
        void get_ShouldReturn404_WhenMessageDoesNotBelongToUser() throws Exception {

            when(service.findByIdAndOwner(1L, "ann")).thenThrow(EntityNotFoundException.class);

            mockMvc.perform(get("/messages/1")).andExpect(status().isNotFound());

            verify(service, times(1)).findByIdAndOwner(1L, "ann");
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void get_ShouldReturn400_WhenIdIsNotANumber() throws Exception {

            mockMvc.perform(get("/messages/abc")).andExpect(status().isBadRequest());

            verify(service, Mockito.never()).findByIdAndOwner(anyLong(), anyString());
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void get_ShouldReturn400_WhenIdIsInvalid() throws Exception {

            mockMvc.perform(get("/messages/-1"))
                    .andExpect(status().isBadRequest());

            verify(service, never()).findByIdAndOwner(anyLong(), anyString());
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        @WithMockUser(username = "hank", authorities = "SCOPE_NON-USER")
        void get_ShouldReturn403_WhenUserHasNoGetRights() throws Exception {

            mockMvc.perform(get("/messages/13"))
                    .andExpect(status().isForbidden());

            verify(service, never()).findByIdAndOwner(anyLong(), anyString());
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(get("/messages/1"))
                    .andExpect(status().isUnauthorized());

            verify(service, never()).findByIdAndOwner(anyLong(), anyString());
        }

    }

    @Nested
    @DisplayName("findAllMessagesByOwner_controller Tests")
    class FindAllMessagesByOwnerControllerTests {

        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void get_shouldReturnPagedAndSortedMessagesByDefault() throws Exception {

            List<Message> messages = List.of(
                    new Message(1L, "First Message", "jack"),
                    new Message(2L, "Second Message", "jack")
            );

            when(service.findAllByOwner(
                    "jack", 0, 2, Sort.by(Sort.Direction.ASC, "id"))
                ).thenReturn(messages);

            mockMvc.perform(get("/messages")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("First Message"))
                    .andExpect(jsonPath("$[1].title").value("Second Message"));

            verify(service, times(1)).findAllByOwner("jack", 0, 2, Sort.by(Sort.Direction.ASC, "id"));
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void get_shouldReturnPagedMessages() throws Exception {

            List<Message> messages = List.of(
                    new Message(2L, "Second Message", "jack")
            );

            when(service.findAllByOwner(
                    "jack", 1, 1, Sort.by(Sort.Direction.ASC, "id"))
                ).thenReturn(messages);

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

        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void get_shouldReturnSortedMessages() throws Exception {

            List<Message> messages = List.of(
                    new Message(6L, "6 - Message", "jack"),
                    new Message(5L, "5 - Message", "jack")
            );

            when(service.findAllByOwner(
                    "jack", 0, 2, Sort.by(Sort.Direction.DESC, "id"))
                ).thenReturn(messages);

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

        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void get_shouldReturnEmptyPageWhenOutOfBounds() throws Exception {

            when(service.findAllByOwner("jack", 100, 2, Sort.by(Sort.Direction.ASC, "id")))
                    .thenReturn(Collections.emptyList());

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

        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "hank", authorities = "SCOPE_NON-USER")
        void getAll_ShouldReturn403_WhenUserHasNoGetRights() throws Exception {

            mockMvc.perform(get("/messages")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(service, never()).findAllByOwner(anyString(), anyInt(), anyInt(), any(Sort.class));
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void getAll_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(get("/messages")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());

            verify(service, never()).findAllByOwner(anyString(), anyInt(), anyInt(), any(Sort.class));
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void get_shouldReturn400_WhenPaginationParamsAreNegative() throws Exception {

            mockMvc.perform(get("/messages")
                            .param("page", "-1")
                            .param("size", "-5")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).findAllByOwner(anyString(), anyInt(), anyInt(), any(Sort.class));
        }

    }

    @Nested
    @DisplayName("createMessage_controller Tests")
    class CreateMessageControllerTests {

        @Test
        @Tag("createMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
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

        @Test
        @Tag("createMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void create_ShouldReturn400_WhenInvalidInput() throws Exception {

            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}") // Пустое тело
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }

        @Test
        @Tag("createMessage_controller")
        @WithMockUser(username = "hank", authorities = "SCOPE_NON-USER")
        void create_ShouldReturn403_WhenUserHasNoCreateRights() throws Exception {

            mockMvc.perform(post("/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"hank\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(service, never()).createMessage(any(Message.class), eq("hank"));
        }

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

    @Nested
    @DisplayName("updateMessage_controller Tests")
    class UpdateMessageControllerTests {

        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void update_ShouldUpdateMessage_WhenItExists() throws Exception {

            mockMvc.perform(put("/messages/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).updateMessage(eq(1L), any(Message.class), eq("jack"));
        }

        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
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

        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "ann", authorities = "SCOPE_USER")
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

        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void update_ShouldReturn400_WhenRequestBodyIsInvalid() throws Exception {

            mockMvc.perform(put("/messages/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}") // Пустое тело
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }

        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void update_ShouldReturn400_WhenIdIsNotANumber() throws Exception {

            mockMvc.perform(put("/messages/abs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }

        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void update_ShouldReturn400_WhenIdIsInvalid() throws Exception {

            mockMvc.perform(put("/messages/-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Updated Title\",\"owner\":\"jack\"}")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(service, never()).updateMessage(anyLong(), any(Message.class), anyString());
        }

        @Test
        @Tag("updateMessage_controller")
        @WithMockUser(username = "hank", authorities = "SCOPE_NON-USER")
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

    @Nested
    @DisplayName("deleteMessage_controller Tests")
    class DeleteMessageControllerTests {

        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void delete_ShouldDeleteMessage_WhenItExists() throws Exception {

            mockMvc.perform(delete("/messages/1"))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).deleteMessage(1L, "jack");
        }

        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void delete_ShouldReturn404_WhenMessageDoesNotExist() throws Exception {

            doThrow(new EntityNotFoundException()).when(service).deleteMessage(99L, "jack");

            mockMvc.perform(delete("/messages/99")).andExpect(status().isNotFound());

            verify(service, times(1)).deleteMessage(99L, "jack");
        }

        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void delete_ShouldReturn404_WhenMessageDoesNotBelongToUser() throws Exception {

            doThrow(new EntityNotFoundException()).when(service).deleteMessage(10L, "jack");

            mockMvc.perform(delete("/messages/10")).andExpect(status().isNotFound());

            verify(service, times(1)).deleteMessage(10L, "jack");
        }

        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void delete_ShouldReturn400_WhenIdIsNotANumber() throws Exception {

            mockMvc.perform(delete("/messages/abc")).andExpect(status().isBadRequest());

            verify(service, never()).deleteMessage(any(), anyString());
        }

        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "jack", authorities = "SCOPE_USER")
        void delete_ShouldReturn400_WhenIdIsInvalid() throws Exception {

            mockMvc.perform(delete("/messages/-1")).andExpect(status().isBadRequest());

            verify(service, never()).deleteMessage(anyLong(), anyString());
        }


        @Test
        @Tag("deleteMessage_controller")
        @WithMockUser(username = "hank", authorities = "SCOPE_NON-USER")
        void delete_ShouldReturn403_WhenUserHasNoDeleteRights() throws Exception {

            mockMvc.perform(delete("/messages/1")).andExpect(status().isForbidden());

            verify(service, never()).deleteMessage(anyLong(), anyString());
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {

            mockMvc.perform(delete("/messages/1"))
                    .andExpect(status().isUnauthorized());

            verify(service, never()).deleteMessage(anyLong(), anyString());
        }

    }

}
