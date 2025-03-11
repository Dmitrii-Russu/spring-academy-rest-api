package com.example.oauth_secured_rest_api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class HomeController {

    @GetMapping
    public String home(Principal principal) {
        return "Hello, " + principal.getName();
    }

    @GetMapping("/jack")
    //@PreAuthorize("hasAuthority('SCOPE_USER')")
    public String home1() {
        return "Вот така хуйня малятки!";
    }

}