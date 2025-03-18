package com.example.oauth_secured_rest_api.message;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Transactional
@Tag("integrationWebTestClient")
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageControllerWebTestClientIntegrationTest {

    private final Map<String, String> tokens = new HashMap<>();

    @Autowired
    private WebTestClient client;

    @BeforeAll
    void initTokens() throws Exception {
        tokens.put("jack", getTokenForUser("jack", "asd"));
        tokens.put("ann", getTokenForUser("ann", "zxc"));
        tokens.put("hank", getTokenForUser("hank", "qwe"));
    }

    private String getTokenForUser(String username, String password) throws Exception {

        return client.post().uri("/token")
                .headers(headers -> headers.setBasicAuth(username, password))
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();
    }

    @Nested
    @DisplayName("findByIdAndOwner_controller Tests")
    class FindByIdAndOwnerControllerTests {

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldGetMessage_WhenItExists() {
            
              client.get().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(1)
                    .jsonPath("$.title").isEqualTo("testData1")
                    .jsonPath("$.owner").isEqualTo("jack");
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn404_WhenMessageDoesNotExist() {

            client.get().uri("/messages/99")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn404_WhenMessageDoesNotBelongToUser() {

            client.get().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("ann")))
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn400_WhenIdIsNotANumber() {

            client.get().uri("/messages/abs")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn400_WhenIdIsInvalid() {

            client.get().uri("/messages/-1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn403_WhenUserHasNoGetRights() {

            client.get().uri("/messages/13")
                    .headers(headers -> headers.setBearerAuth(tokens.get("hank")))
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn401_WhenUserIsNotAuthenticated() {

            client.get().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("apple")))
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody(Void.class);
        }

    }

    @Nested
    @DisplayName("findAllMessagesByOwner_controller Tests")
    class FindAllMessagesByOwnerControllerTests {

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnPagedAndSortedMessagesByDefault() {

            client.get().uri("/messages?page=0&size=2&sort=id&direction=asc")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(2)
                    .jsonPath("$[0].id").isEqualTo(1)
                    .jsonPath("$[0].title").isEqualTo("testData1")
                    .jsonPath("$[0].owner").isEqualTo("jack")
                    .jsonPath("$[1].id").isEqualTo(2)
                    .jsonPath("$[1].title").isEqualTo("testData2")
                    .jsonPath("$[1].owner").isEqualTo("jack");
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnPagedMessages() {

            client.get().uri("/messages?page=1&size=1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(1)
                    .jsonPath("$[0].id").isEqualTo(2)
                    .jsonPath("$[0].title").isEqualTo("testData2")
                    .jsonPath("$[0].owner").isEqualTo("jack");
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnSortedMessages() {

            client.get().uri("/messages?page=0&size=2&sort=id&direction=desc")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(2)  // Проверяем, что получили 2 элемента
                    .jsonPath("$[0].id").isEqualTo(6) // Первый элемент на этой странице - ID 3
                    .jsonPath("$[1].id").isEqualTo(5); // Второй элемент на этой странице - ID 2
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnEmptyPageWhenOutOfBounds() {

            client.get().uri("/messages?page=100&size=10")  // Слишком большая страница
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(0);  // Ожидаем пустой список
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void getAll_ShouldReturn403_WhenUserHasNoGetRights() {

            client.get().uri("/messages")
                    .headers(headers -> headers.setBearerAuth(tokens.get("hank")))
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void getAll_ShouldReturn401_WhenUserIsNotAuthenticated() {

            client.get().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("apple")))
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturn400_WhenPaginationParamsAreNegative() throws Exception {

            client.get().uri("/messages?page=-1&size=-5&sort=id&direction=desc")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(Void.class);
        }

    }

    @Nested
    @DisplayName("createMessage_controller Tests")
    class CreateMessageControllerTests {

        @Test
        @Tag("createMessage_controller")
        void create_ShouldCreateNewMessage() {

            var response = client
                    .post().uri("/messages")
                    .headers(headers -> headers.setBearerAuth(tokens.get("ann")))
                    .bodyValue(new Message(null, "зеленое говно325", null))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .header(ACCEPT, APPLICATION_JSON_VALUE)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectHeader().exists(LOCATION)
                    .returnResult(Void.class);

            String location = response.getResponseHeaders().getFirst(LOCATION);
            assertNotNull(location, "Location header should not be null!");
            Long id = Long.parseLong(location.replaceAll(".*/", ""));

            client.get().uri(location)
                    .headers(headers -> headers.setBearerAuth(tokens.get("ann")))
                    .header(ACCEPT, APPLICATION_JSON_VALUE)
                    .exchange()
                    .expectBody(Message.class)
                    .isEqualTo(new Message(id, "зеленое говно325", "ann"));
        }

        @Test
        @Tag("createMessage_controller")
        void create_ShouldReturn400_WhenInvalidInput() {

            client.post().uri("/messages")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .bodyValue(new Message(null, "", null))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .header(ACCEPT, APPLICATION_JSON_VALUE)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @Tag("createMessage_controller")
        void create_ShouldReturn403_WhenUserHasNoCreateRights() {

            client.post().uri("/messages")
                    .headers(headers -> headers.setBearerAuth(tokens.get("hank")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new Message(null, "Test content", null))
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("createMessage_controller")
        void create_ShouldReturn401_WhenUserIsNotAuthenticated() {

            client.post().uri("/messages")
                    .bodyValue(new Message(null, "Test content", null))
                    .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .header(ACCEPT, APPLICATION_JSON_VALUE)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

    }

    @Nested
    @DisplayName("updateMessage_controller Tests")
    class UpdateMessageControllerTests {

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldUpdateMessage_WhenItExists() {

            client.put().uri("/messages/7")
                    .headers(headers -> headers.setBearerAuth(tokens.get("ann")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new Message(null, "updatedTitle", null)) // Тело запроса
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody(Void.class);

            client.get().uri("/messages/7")
                    .headers(headers -> headers.setBearerAuth(tokens.get("ann")))
                    .exchange()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(7)
                    .jsonPath("$.title").isEqualTo("updatedTitle")
                    .jsonPath("$.owner").isEqualTo("ann");
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn404_WhenMessageDoesNotExist() {

            client.put().uri("/messages/99")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                    .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn404_WhenMessageDoesNotBelongToUser() {

            client.put().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("ann")))
                    .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                    .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn400_WhenRequestBodyIsInvalid() {

            client.put().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                    .bodyValue("{}")   // Добавляем тело запроса
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn400_WhenIdIsNotANumber() {

            client.put().uri("/messages/abs")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                    .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn400_WhenIdIsInvalid() {

            client.put().uri("/messages/-1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                    .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn403_WhenUserHasNoUpdateRights() {

            client.put().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("hank")))
                    .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                    .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn401_WhenUserIsNotAuthenticated() {

            client.put().uri("/messages/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new Message(null, "Test content", null))
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody(Void.class);
        }

    }

    @Nested
    @DisplayName("deleteMessage_controller Tests")
    class DeleteMessageControllerTests {

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldDeleteMessage_WhenItExists() {

            client.delete().uri("/messages/{id}", 12)
                    .headers(headers -> headers.setBearerAuth(tokens.get("ann")))
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody(Void.class);

            client.get().uri("/messages/12")
                    .headers(headers -> headers.setBearerAuth(tokens.get("ann")))
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn404_WhenMessageDoesNotExist() {

            client.delete().uri("/messages/99")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn404_WhenMessageDoesNotBelongToUser() {

            client.delete().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("ann")))
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn400_WhenIdIsNotANumber() {

            client.delete().uri("/messages/abs")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn400_WhenIdIsInvalid() {

            client.delete().uri("/messages/-1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("jack")))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody(Void.class);
        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn403_WhenUserHasNoDeleteRights() {

            client.delete().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("hank")))
                    .exchange()
                    .expectStatus().isForbidden()
                    .expectBody(Void.class);

        }

        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn401_WhenUserIsNotAuthenticated() {

            client.delete().uri("/messages/1")
                    .headers(headers -> headers.setBearerAuth(tokens.get("apple")))
                    .exchange()
                    .expectStatus().isUnauthorized()
                    .expectBody(Void.class);
        }

    }

}
