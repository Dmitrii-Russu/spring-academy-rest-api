package com.example.oauth_secured_rest_api;

import com.example.oauth_secured_rest_api.security.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
public class OauthSecuredRestApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(OauthSecuredRestApiApplication.class, args);
	}

}
