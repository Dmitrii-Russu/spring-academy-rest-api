package message.integration.service;

import message.model.Message;
import message.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Integration tests for {@link MessageService}.
 * This class verifies the full interaction between {@link MessageService} and the database.
 * It tests the persistence layer using a real database instance, ensuring correct behavior of:
 * - Retrieving a message by ID and owner ({@link MessageService#findByIdAndOwner(Long, String)}),
 * - Retrieving all messages by owner with pagination and sorting ({@link MessageService#findAllByOwner(String, int, int, Sort)}),
 * - Creating a new message ({@link MessageService#createMessage(Message, String)}),
 * - Updating an existing message ({@link MessageService#updateMessage(Long, Message, String)}),
 * - Deleting a message ({@link MessageService#deleteMessage(Long, String)}).
 * Each operation is tested with actual database interactions, covering both successful scenarios
 * and edge cases (e.g., message not found, wrong owner). Transactions are rolled back after each test
 * to ensure database integrity.
 */
@Tag("integration")
@SpringBootTest
@Transactional  // Откат изменений после теста
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Используем реальную БД
class MessageServiceIT {

    @Autowired
    private MessageService service;

    /**
     * Tests for {@link MessageService#findByIdAndOwner(Long, String)}.
     * Verifies the behavior of retrieving a message by its ID and owner,
     * including correct retrieval, handling of non-existing messages,
     * and validation when the message does not belong to the user.
     */
    @Nested
    @DisplayName("findByIdAndOwner Tests")
    class FindByIdAndOwnerTests {

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that the correct message is returned when it exists.
         */
        @Test
        @Tag("findByIdAndOwner")
        void findByIdAndOwner_ShouldReturnMessage_WhenItExists() {

            Message retrieved = service.findByIdAndOwner(1L, "jack");

            assertEquals(new Message(1L, "testData1", "jack"), retrieved);
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not exist.
         */
        @Test
        @Tag("findByIdAndOwner")
        void findByIdAndOwner_ShouldReturn404_WhenMessageDoesNotExist() {

            assertThrows(EntityNotFoundException.class,
                () -> service.findByIdAndOwner(99L, "jack"));
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not belong to the user.
         */
        @Test
        @Tag("findByIdAndOwner")
        void findByIdAndOwner_ShouldReturn404_WhenMessageDoesNotBelongToUser() {

            assertThrows(EntityNotFoundException.class,
                () -> service.findByIdAndOwner(7L, "jack"));
        }
    }

    /**
     * Tests for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
     * Verifies the behavior of retrieving messages by owner with pagination and sorting.
     * Includes tests for default sorting, custom page size, descending order sorting,
     * and handling of out-of-bounds page requests.
     */
    @Nested
    @DisplayName("findAllByOwner Tests")
    class FindAllByOwnerTests {

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that messages are returned with the default sorting and custom page size.
         */
        @Test
        @Tag("findAllByOwner")
        void findAllByOwner_ShouldReturnMessagesWithDefaultSortingAndCustomPageSize() {

            List<Message> retrieved = service.findAllByOwner("jack", 0, 2, Sort.by("id").ascending());

            assertThat(retrieved)
                .hasSize(2)
                .extracting(Message::getId, Message::getTitle, Message::getOwner)
                .containsExactly(
                        tuple(1L, "testData1", "jack"),
                        tuple(2L, "testData2", "jack")
                );
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that messages are returned sorted by ID in descending order.
         */
        @Test
        @Tag("findAllByOwner")
        void findAllByOwner_ShouldReturnMessagesSortedByIdDescending_WhenRequested() {

            List<Message> retrieved = service.findAllByOwner("jack", 0, 2, Sort.by("id").descending());

            assertThat(retrieved)
                .hasSize(2)
                .extracting(Message::getId)
                .containsExactly(6L, 5L); // Теперь порядок 6 → 5
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that an empty list is returned when page size is out of bounds.
         */
        @Test
        @Tag("findAllByOwner")
        void findAllByOwner_ShouldReturnEmptyPageWhenOutOfBounds() {

            List<Message> retrieved = service.findAllByOwner("jack", 100, 2, Sort.by("id").ascending());

            assertThat(retrieved).isEmpty();
        }
    }

    /**
     * Tests for {@link MessageService#createMessage(Message, String)}.
     * Verifies the behavior of creating a new message and saving it to the repository.
     * Includes a test to ensure the message is correctly created and returned.
     */
    @Nested
    @DisplayName("createMessage Tests")
    class CreateMessageTests {

        /**
         * Test for {@link MessageService#createMessage(Message, String)}.
         * Verifies that a new message is created and returned.
         */
        @Test
        @Tag("createMessage")
        void createMessage_ShouldCreateAndRetrieveMessage() {

            Message message = new Message(null, "testData1", null);

            Message saved = service.createMessage(message, "jack");

            Message retrieved = service.findByIdAndOwner(saved.getId(), "jack");

            assertEquals(saved, retrieved);
        }
    }

    /**
     * Tests for {@link MessageService#updateMessage(Long, Message, String)}.
     * Verifies the behavior of updating an existing message in the repository.
     * Includes tests for successful updates, as well as cases where the message does not exist or does not belong to the user.
     */
    @Nested
    @DisplayName("updateMessage Tests")
    class UpdateMessageTests {

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that an existing message is updated correctly.
         */
        @Test
        @Tag("updateMessage")
        void updateMessage_ShouldUpdateMessage_WhenItExists() {

            Message message = new Message(null, "updatedMessage1", null);

            service.updateMessage(1L, message, "jack");

            Message updatedMessage = service.findByIdAndOwner(1L, "jack");

            assertEquals(updatedMessage.getTitle(), message.getTitle());
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not exist.
         */
        @Test
        @Tag("updateMessage")
        void updateMessage_ShouldReturn404_WhenMessageDoesNotExist() {

            Message message = new Message(null, "newTitle", null);

            assertThrows(EntityNotFoundException.class,
                () -> service.updateMessage(99L, message, "jack"));
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not belong to the user.
         */
        @Test
        @Tag("updateMessage")
        void updateMessage_ShouldReturn404_WhenMessageDoesNotBelongToUser() {

            Message message = new Message(null, "newTitle", null);

            assertThrows(EntityNotFoundException.class,
                () -> service.updateMessage(7L, message, "jack"));
        }
    }

    /**
     * Tests for {@link MessageService#deleteMessage(Long, String)}.
     * Verifies the behavior of deleting a message from the repository.
     * Includes tests for successful deletion, as well as cases where the message does not exist or does not belong to the user.
     */
    @Nested
    @DisplayName("deleteMessage Tests")
    class DeleteMessageTests {

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that a message is successfully removed when it exists.
         */
        @Test
        @Tag("deleteMessage")
        void deleteMessage_ShouldRemoveMessage_WhenItExists() {

            service.deleteMessage(1L, "jack");

            assertThrows(EntityNotFoundException.class,
                () -> service.findByIdAndOwner(1L, "jack"));
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not exist.
         */
        @Test
        @Tag("deleteMessage")
        void deleteMessage_ShouldReturn404_WhenMessageDoesNotExist() {

            assertThrows(EntityNotFoundException.class,
                () -> service.deleteMessage(99L, "jack"));
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not belong to the user.
         */
        @Test
        @Tag("deleteMessage")
        void deleteMessage_ShouldReturn404_WhenMessageDoesNotBelongToUser() {

            assertThrows(EntityNotFoundException.class,
                () -> service.deleteMessage(7L, "jack"));
        }
    }
}