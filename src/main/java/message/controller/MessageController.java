package message.controller;

import message.model.Message;
import message.service.MessageService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller class responsible for handling requests related to {@link Message} entities.
 * Provides endpoints for CRUD operations on messages, including finding, creating, updating, and deleting messages.
 */
@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    /**
     * Retrieves a {@link Message} by its id and owner.
     * Returns the message if found, or throws a {@link EntityNotFoundException} if not found.
     *
     * @param id the ID of the message
     * @param user the authenticated user requesting the message
     * @return the {@link Message} if found
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Message findByIdAndOwner(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal User user
    ) {
        return service.findByIdAndOwner(id, user.getUsername());
    }

    /**
     * Retrieves all {@link Message} entities for the authenticated user.
     * Supports pagination and sorting.
     *
     * @param user the authenticated user requesting the messages
     * @param page the page number (default is 0)
     * @param size the page size (default is 2)
     * @param sort the sort field (default is "id")
     * @param direction the sort direction (default is "asc")
     * @return a list of {@link Message} entities
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Message> findAllMessagesByOwner(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "2") @Positive int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        return service.findAllByOwner(
                user.getUsername(), page, size, sortOrder
        );
    }

    /**
     * Creates a new {@link Message} for the authenticated user.
     * The location of the newly created message is included in the response header.
     *
     * @param ucb the {@link UriComponentsBuilder} used to build the URI
     * @param response the {@link HttpServletResponse} to set the location header
     * @param user the authenticated user creating the message
     * @param message the {@link Message} to be created
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createMessage(
            UriComponentsBuilder ucb,
            HttpServletResponse response,
            @AuthenticationPrincipal User user,
            @RequestBody @Validated Message message
    ) {
        Message savedMessage
                = service.createMessage(message, user.getUsername());

        URI location
                = ucb.path("/messages/{id}")
                .buildAndExpand(savedMessage.getId()).toUri();

        response.setHeader(HttpHeaders.LOCATION, location.toString());
    }

    /**
     * Updates an existing {@link Message} by its id for the authenticated user.
     * If the message does not exist, an {@link EntityNotFoundException} is thrown.
     *
     * @param id the ID of the message to update
     * @param user the authenticated user updating the message
     * @param message the new {@link Message} data
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMessage(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal User user,
            @RequestBody @Validated Message message
    ) {
        service.updateMessage(id, message, user.getUsername());
    }

    /**
     * Deletes a {@link Message} by its id for the authenticated user.
     * If the message is not found, an {@link EntityNotFoundException} is thrown.
     *
     * @param id the ID of the message to delete
     * @param user the authenticated user requesting the deletion
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal User user
    ) {
        service.deleteMessage(id, user.getUsername());
    }
}

