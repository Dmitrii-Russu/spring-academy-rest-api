package com.example.oauth_secured_rest_api.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByIdAndOwner(Long id, String owner);

    Page<Message> findAllByOwner(String owner, Pageable pageable);
}
