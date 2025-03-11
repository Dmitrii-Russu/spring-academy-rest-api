package com.example.oauth_secured_rest_api.security.userdetails;

import com.example.oauth_secured_rest_api.user.UserEntityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SecurityUserService implements UserDetailsService {
    private final UserEntityRepository repository;

    public SecurityUserService(UserEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws EntityNotFoundException {

        return repository
                .findByUsername(username)
                .map(SecurityUser::new)
                .orElseThrow(EntityNotFoundException::new);
    }

}
