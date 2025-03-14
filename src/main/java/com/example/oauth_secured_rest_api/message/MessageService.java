package com.example.oauth_secured_rest_api.message;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    public Message findByIdAndOwner(Long id, String owner) {
        return repository
                .findByIdAndOwner(id, owner)
                .orElseThrow(EntityNotFoundException::new);
    }

    /**
     * Finds all {@link Message} entities by the specified owner with pagination and sorting.
     *
     * @param owner the owner of the messages
     * @param page the page number
     * @param size the page size
     * @param sort the sorting order
     * @return a list of {@link Message} entities
     */
    public List<Message> findAllByOwner(
            String owner, int page, int size, Sort sort
    ) {
        //Sort sort = Sort.by("id").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAllByOwner(owner, pageable).getContent();
    }

    /**
     * Creates a new {@link Message} and sets the owner before saving it.
     *
     * @param message the {@link Message} to create
     * @param owner the owner of the message
     * @return the saved {@link Message}
     */
    public Message createMessage(
            @RequestBody Message message,
            String owner
    ) {
        message.setOwner(owner);
        return repository.save(message);
    }

    /**
     * Updates an existing {@link Message} with the provided data.
     * The message is found by its id and owner, then updated with the new title.
     *
     * @param id the ID of the message to update
     * @param message the new {@link Message} data to update
     * @param owner the owner of the message
     * @return the updated {@link Message}
     * @throws EntityNotFoundException if the message is not found
     */
    public Message updateMessage(Long id, Message message, String owner) {
        Message messageToUpdate = findByIdAndOwner(id, owner);
        messageToUpdate.setTitle(message.getTitle());
        return repository.save(messageToUpdate);
    }

    public void deleteMessage(Long id, String owner) {
        Message message = findByIdAndOwner(id, owner);
        repository.delete(message);
    }
}