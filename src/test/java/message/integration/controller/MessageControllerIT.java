package message.integration.controller;

import message.controller.MessageController;
import message.model.Message;
import message.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Integration test for {@link MessageController}.
 * This class uses {@link WebTestClient} to test the HTTP endpoints of the {@link MessageController}.
 * It verifies the full integration with the application context, including security configurations,
 * HTTP status codes, and response validation.
 *
 * The tests cover:
 * - Retrieving messages by ID and owner ({@link MessageService#findByIdAndOwner(Long, String)}),
 * - Retrieving paginated and sorted messages ({@link MessageService#findAllByOwner(String, int, int, Sort)}),
 * - Creating new messages ({@link MessageService#createMessage(Message, String)}),
 * - Updating existing messages ({@link MessageService#updateMessage(Long, Message, String)}),
 * - Deleting messages ({@link MessageService#deleteMessage(Long, String)}).
 *
 * Each operation is tested for both successful scenarios and edge cases, including
 * permission checks, invalid input, and authentication failures.
 */

@Tag("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageControllerIT {

    @Autowired
    private WebTestClient client;

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
        void get_ShouldGetMessage_WhenItExists() {

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("testData1")
                .jsonPath("$.owner").isEqualTo("jack");
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 404 status is returned when the message does not exist.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn404_WhenMessageDoesNotExist() {

            client.get().uri("/messages/99")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 404 status is returned when the message belongs to another user.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn404_WhenMessageDoesNotBelongToUser() {

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("ann", "zxc"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 400 status is returned when the ID is not a valid number.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn400_WhenIdIsNotANumber() {

            client.get().uri("/messages/abs")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 400 status is returned when the ID is negative.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn400_WhenIdIsInvalid() {

            client.get().uri("/messages/-1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 403 status is returned when the user does not have the "USER" role.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn403_WhenUserHasNoGetRights() {

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("hank", "qwe"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that a 401 status is returned when the user is not authenticated.
         */
        @Test
        @Tag("findByIdAndOwner_controller")
        void get_ShouldReturn401_WhenUserIsNotAuthenticated() {

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("john", "yui"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(Void.class);
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
        void get_shouldReturnPagedAndSortedMessagesByDefault() {

            client.get().uri("/messages?page=0&size=2&sort=id&direction=asc")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
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

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that pagination works correctly when requesting a specific page and size.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnPagedMessages() {

            client.get().uri("/messages?page=1&size=1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].id").isEqualTo(2)
                .jsonPath("$[0].title").isEqualTo("testData2")
                .jsonPath("$[0].owner").isEqualTo("jack");
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that sorting messages in descending order works correctly.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnSortedMessages() {

            client.get().uri("/messages?page=0&size=2&sort=id&direction=desc")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)  // Проверяем, что получили 2 элемента
                .jsonPath("$[0].id").isEqualTo(6) // Первый элемент на этой странице - ID 3
                .jsonPath("$[1].id").isEqualTo(5); // Второй элемент на этой странице - ID 2
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that an empty page is returned when the page is out of bounds.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturnEmptyPageWhenOutOfBounds() {

            client.get().uri("/messages?page=100&size=10")  // Слишком большая страница
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);  // Ожидаем пустой список
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that a 403 Forbidden status is returned when the user does not have GET rights.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        void getAll_ShouldReturn403_WhenUserHasNoGetRights() {

            client.get().uri("/messages")
                .headers(headers -> headers.setBasicAuth("hank", "qwe"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that a 401 Unauthorized status is returned when the user is not authenticated.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        void getAll_ShouldReturn401_WhenUserIsNotAuthenticated() {

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("john", "yui"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that a 400 Bad Request status is returned when pagination parameters are negative.
         */
        @Test
        @Tag("findAllMessagesByOwner_controller")
        void get_shouldReturn400_WhenPaginationParamsAreNegative() throws Exception {

            client.get().uri("/messages?page=-1&size=-5&sort=id&direction=desc")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
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
        //@DirtiesContext
        @Tag("createMessage_controller")
        void create_ShouldCreateNewMessage() {

            var response = client
                .post().uri("/messages")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
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
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .exchange()
                .expectBody(Message.class)
                .isEqualTo(new Message(id, "зеленое говно325", "jack"));
        }

        /**
         * Test for {@link MessageService#createMessage(Message, String)}.
         * Verifies that a 400 Bad Request status is returned when invalid input is provided.
         */
        @Test
        @Tag("createMessage_controller")
        void create_ShouldReturn400_WhenInvalidInput() {

            client.post().uri("/messages")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .bodyValue(new Message(null, "", null))
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .header(ACCEPT, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest();
        }

        /**
         * Test for {@link MessageService#createMessage(Message, String)}.
         * Verifies that a 403 Forbidden status is returned when the user has no create rights.
         */
        @Test
        @Tag("createMessage_controller")
        void create_ShouldReturn403_WhenUserHasNoCreateRights() {

            client.post().uri("/messages")
                .headers(headers -> headers.setBasicAuth("hank", "qwe"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new Message(null, "Test content", null))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#createMessage(Message, String)}.
         * Verifies that a 401 Unauthorized status is returned when the user is not authenticated.
         */
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
        void update_ShouldUpdateMessage_WhenItExists() {

            client.put().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new Message(null, "updatedTitle", null)) // Тело запроса
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("updatedTitle")
                .jsonPath("$.owner").isEqualTo("jack");
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 404 Not Found status is returned when the message does not exist.
         */
        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn404_WhenMessageDoesNotExist() {

            client.put().uri("/messages/99")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 404 Not Found status is returned when the message does not belong to the user.
         */
        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn404_WhenMessageDoesNotBelongToUser() {

            client.put().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("ann", "zxc"))
                .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 400 Bad Request status is returned when the request body is invalid.
         */
        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn400_WhenRequestBodyIsInvalid() {

            client.put().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                .bodyValue("{}")   // Добавляем тело запроса
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 400 Bad Request status is returned when the ID is not a number.
         */
        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn400_WhenIdIsNotANumber() {

            client.put().uri("/messages/abs")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 400 Bad Request status is returned when the ID is invalid.
         */
        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn400_WhenIdIsInvalid() {

            client.put().uri("/messages/-1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 403 Forbidden status is returned when the user has no update rights.
         */
        @Test
        @Tag("updateMessage_controller")
        void update_ShouldReturn403_WhenUserHasNoUpdateRights() {

            client.put().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("hank", "qwe"))
                .contentType(MediaType.APPLICATION_JSON)  // Указываем JSON-формат
                .bodyValue(new Message(null, "Test content", null))   // Добавляем тело запроса
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that a 401 Unauthorized status is returned when the user is not authenticated.
         */
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
        @DirtiesContext
        @Tag("deleteMessage_controller")
        void delete_ShouldDeleteMessage_WhenItExists() {

            client.delete().uri("/messages/{id}", 1)
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 404 Not Found status is returned when the message does not exist.
         */
        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn404_WhenMessageDoesNotExist() {

            client.delete().uri("/messages/99")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 404 Not Found status is returned when the message does not belong to the user.
         */
        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn404_WhenMessageDoesNotBelongToUser() {

            client.delete().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("ann", "zxc"))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Void.class);

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isOk();
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 400 Bad Request status is returned when the ID is not a number.
         */
        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn400_WhenIdIsNotANumber() {

            client.delete().uri("/messages/abs")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 400 Bad Request status is returned when the ID is invalid.
         */
        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn400_WhenIdIsInvalid() {

            client.delete().uri("/messages/-1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Void.class);
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 403 Forbidden status is returned when the user has no delete rights.
         */
        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn403_WhenUserHasNoDeleteRights() {

            client.delete().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("hank", "qwe"))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody(Void.class);

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isOk();

        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a 401 Unauthorized status is returned when the user is not authenticated.
         */
        @Test
        @Tag("deleteMessage_controller")
        void delete_ShouldReturn401_WhenUserIsNotAuthenticated() {

            client.delete().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("john", "yui"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(Void.class);

            client.get().uri("/messages/1")
                .headers(headers -> headers.setBasicAuth("jack", "asd"))
                .exchange()
                .expectStatus().isOk();
        }
    }
}
