package com.example.simple_rest_api;

import com.example.simple_rest_api.model.User;
import com.example.simple_rest_api.repositories.UserRepository;
import com.example.simple_rest_api.securiry.Authority;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
public class SimpleRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleRestApiApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(UserRepository repository, PasswordEncoder encoder, ObjectMapper mapper) {
        return args -> {
            User user = new User();
            user.setAuthorities(List.of(Authority.READ, Authority.WRITE));

            System.err.println(mapper.writeValueAsString(user));
        };
    }

}
