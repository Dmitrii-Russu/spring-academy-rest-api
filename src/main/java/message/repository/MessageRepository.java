package message.repository;

import message.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

/**
 * Repository interface for {@link Message} entity, providing CRUD operations and custom queries.
 * Extends {@link JpaRepository} to leverage default JPA functionality.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Finds a {@link Message} by its id and owner.
     *
     * @param id the ID of the message
     * @param owner the owner of the message
     * @return an {@link Optional} containing the found message, or {@link Optional#empty()} if not found
     */
    Optional<Message> findByIdAndOwner(Long id, String owner);

    /**
     * Finds all {@link Message} entities by the specified owner, with pagination support.
     *
     * @param owner the owner of the messages
     * @param pageable the pagination information
     * @return a {@link Page} containing the messages for the given owner
     */
    Page<Message> findAllByOwner(String owner, Pageable pageable);
}
