package com.example.oauth_secured_rest_api.message;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Message findByIdAndOwner(@PathVariable @Positive Long id, Principal principal) {
        return service.findByIdAndOwner(id, principal.getName());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Message> findAllMessagesByOwner(
            Principal principal,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "2") @Positive int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        return service.findAllByOwner(
                principal.getName(), page, size, sortOrder
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createMessage(
            Principal principal,
            UriComponentsBuilder ucb,
            HttpServletResponse response,
            @RequestBody @Validated Message message
    ) {
        Message savedMessage
                = service.createMessage(message, principal.getName());

        URI location
                = ucb.path("/messages/{id}")
                .buildAndExpand(savedMessage.getId()).toUri();

        response.setHeader(HttpHeaders.LOCATION, location.toString());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMessage(
            Principal principal,
            @PathVariable @Positive Long id,
            @RequestBody @Validated Message message
    ) {
        service.updateMessage(id, message, principal.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            Principal principal,
            @PathVariable @Positive Long id
    ) {
        service.deleteMessage(id, principal.getName());
    }
}
