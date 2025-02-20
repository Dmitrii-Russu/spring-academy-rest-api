package message.unit.service;

import message.model.Message;
import message.repository.MessageRepository;
import message.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MessageService}.
 * This class contains tests for the core CRUD operations of the MessageService:
 * - Retrieving a message by ID and owner ({@link MessageService#findByIdAndOwner(Long, String)}),
 * - Retrieving all messages by owner with pagination and sorting ({@link MessageService#findAllByOwner(String, int, int, Sort)}),
 * - Creating a new message ({@link MessageService#createMessage(Message, String)}),
 * - Updating an existing message ({@link MessageService#updateMessage(Long, Message, String)}),
 * - Deleting a message ({@link MessageService#deleteMessage(Long, String)}).
 * Each operation is tested for both successful scenarios and edge cases (e.g., message not found, wrong owner).
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class MessageServiceUnitTest {

    @Mock
    private MessageRepository repository;

    @InjectMocks
    private MessageService service;

    /**
     * Cleans up mock interactions after each test to verify no unused interactions remain.
     */
    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(repository);
    }


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
            Message expected = new Message(1L, "testData1", "jack");

            when(repository.findByIdAndOwner(1L, "jack"))
                    .thenReturn(Optional.of(expected));

            Message actual = service.findByIdAndOwner(1L, "jack");

            assertEquals(actual, expected);

            assertThat(actual)
                    .extracting(Message::getId, Message::getTitle, Message::getOwner)
                    .containsExactly(1L, "testData1", "jack");

            verify(repository).findByIdAndOwner(1L, "jack");
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not exist.
         */
        @Test
        @Tag("findByIdAndOwner")
        void findByIdAndOwner_ShouldReturn404_WhenMessageDoesNotExist() {
            when(repository.findByIdAndOwner(99L, "jack"))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.findByIdAndOwner(99L, "jack"));

            verify(repository).findByIdAndOwner(99L, "jack");
        }

        /**
         * Test for {@link MessageService#findByIdAndOwner(Long, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not belong to the user.
         */
        @Test
        @Tag("findByIdAndOwner")
        void findByIdAndOwner_ShouldReturn404_WhenMessageDoesNotBelongToUser() {
            when(repository.findByIdAndOwner(1L, "ann"))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.findByIdAndOwner(1L, "ann"));

            verify(repository).findByIdAndOwner(1L, "ann");
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
            Page<Message> messages = new PageImpl<>(
                    List.of(
                            new Message(1L, "testData1", "jack"),
                            new Message(2L, "testData2", "jack")
                    ),
                    PageRequest.of(0, 2),
                    2
            );

            when(repository.findAllByOwner(eq("jack"), any(Pageable.class)))
                    .thenReturn(messages);

            List<Message> retrieved
                    = service.findAllByOwner("jack", 0, 2, Sort.by("id").ascending());

            assertThat(retrieved)
                    .hasSize(2)
                    .extracting(Message::getId, Message::getTitle, Message::getOwner)
                    .containsExactly(
                            tuple(1L, "testData1", "jack"),
                            tuple(2L, "testData2", "jack")
                    );

            verify(repository).findAllByOwner(eq("jack"), any(Pageable.class));
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that messages are returned sorted by ID in descending order.
         */
        @Test
        @Tag("findAllByOwner")
        void findAllByOwner_ShouldReturnMessagesSortedByIdDescending_WhenRequested() {
            List<Message> messages = List.of(
                    new Message(6L, "testData6", "jack"),
                    new Message(5L, "testData5", "jack")
            );

            Page<Message> messagePage = new PageImpl<>(messages);

            when(repository.findAllByOwner(eq("jack"), any(Pageable.class)))
                    .thenReturn(messagePage);

            List<Message> retrieved = service.findAllByOwner("jack", 0, 2, Sort.by("id").descending());

            assertThat(retrieved)
                    .hasSize(2)
                    .extracting(Message::getId)
                    .containsExactly(6L, 5L);

            verify(repository).findAllByOwner(eq("jack"), any(Pageable.class));
        }

        /**
         * Test for {@link MessageService#findAllByOwner(String, int, int, Sort)}.
         * Verifies that an empty list is returned when page size is out of bounds.
         */
        @Test
        @Tag("findAllByOwner")
        void findAllByOwner_ShouldReturnEmptyPageWhenOutOfBounds() {
            Page<Message> emptyPage = Page.empty();

            when(repository.findAllByOwner(eq("jack"), any(Pageable.class)))
                    .thenReturn(emptyPage);

            List<Message> retrieved = service.findAllByOwner("jack", 100, 2, Sort.by("id").ascending());

            assertThat(retrieved).isEmpty();

            verify(repository).findAllByOwner(eq("jack"), any(Pageable.class));
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
        void createMessage_ShouldCreateMessage() {
            Message message = new Message(null, "testData1", "jack");
            Message savedMessage = new Message(1L, "testData1", "jack");

            when(repository.save(any(Message.class))).thenReturn(savedMessage);

            Message saved = service.createMessage(message, "jack");

            assertEquals(saved, savedMessage);

            assertThat(saved)
                    .extracting(Message::getId, Message::getTitle, Message::getOwner)
                    .containsExactly(1L, "testData1", "jack");

            verify(repository).save(any(Message.class));
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
        void updateMessage_ShouldReturnUpdatedMessage_WhenItExists() {
            Message existingMessage = new Message(1L, "old message", "jack");
            Message newMessage = new Message(null, "updated message", null);
            Message savedMessage = new Message(1L, "updated message", "jack");

            when(repository.findByIdAndOwner(1L, "jack"))
                    .thenReturn(Optional.of(existingMessage));

            when(repository.save(any(Message.class)))
                    .thenReturn(savedMessage);

            Message result = service.updateMessage(1L, newMessage, "jack");

            assertEquals(result, savedMessage);

            assertThat(result)
                    .extracting(Message::getId, Message::getTitle, Message::getOwner)
                    .containsExactly(1L, "updated message", "jack");

            verify(repository).findByIdAndOwner(1L, "jack");
            verify(repository).save(any(Message.class));
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not exist.
         */
        @Test
        @Tag("updateMessage")
        void updateMessage_ShouldReturn404_WhenMessageDoesNotExist() {
            Message message = new Message(null, "newTitle", null);

            when(repository.findByIdAndOwner(99L, "jack"))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateMessage(99L, message, "jack"));

            verify(repository).findByIdAndOwner(99L, "jack");
            verify(repository, never()).save(any());
        }

        /**
         * Test for {@link MessageService#updateMessage(Long, Message, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not belong to the user.
         */
        @Test
        @Tag("updateMessage")
        void updateMessage_ShouldReturn404_WhenMessageDoesNotBelongToUser() {
            Message message = new Message(null, "newTitle", null);

            when(repository.findByIdAndOwner(7L, "jack"))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.updateMessage(7L, message, "jack"));

            verify(repository).findByIdAndOwner(7L, "jack");
            verify(repository, never()).save(any());
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
            Message message = new Message(1L, "title1", "jack");

            when(repository.findByIdAndOwner(1L, "jack"))
                    .thenReturn(Optional.of(message));

            service.deleteMessage(1L, "jack");

            verify(repository).findByIdAndOwner(1L, "jack");
            verify(repository).delete(message);
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not exist.
         */
        @Test
        @Tag("deleteMessage")
        void deleteMessage_ShouldReturn404_WhenMessageDoesNotExist() {
            when(repository.findByIdAndOwner(99L, "jack"))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.deleteMessage(99L, "jack"));

            verify(repository).findByIdAndOwner(99L, "jack");
            verify(repository, never()).delete(any());
        }

        /**
         * Test for {@link MessageService#deleteMessage(Long, String)}.
         * Verifies that an {@link EntityNotFoundException} is thrown when the message does not belong to the user.
         */
        @Test
        @Tag("deleteMessage")
        void deleteMessage_ShouldReturn404_WhenMessageDoesNotBelongToUser() {
            when(repository.findByIdAndOwner(1L, "ann"))
                    .thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class,
                    () -> service.deleteMessage(1L, "ann"));

            verify(repository).findByIdAndOwner(1L, "ann");
            verify(repository, never()).delete(any());
        }
    }
}
