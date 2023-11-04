package com.example.simple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimpleRestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleRestApiApplication.class, args);
    }

//    @Bean
//    ApplicationRunner applicationRunner(UserRepository repository, PasswordEncoder encoder, ObjectMapper mapper) {
//        return args -> {
//            User user = new User();
//            System.err.println(mapper.writeValueAsString(user));
//        };
//    }

}
