package com.example.simple_rest_api.securiry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var manager = new InMemoryUserDetailsManager();

        UserDetails user1 = User.withUsername("a")
                .password(encoder.encode("a"))
                .roles(Role.ADMIN.name(), Role.BASIC.name())
                .build();
        manager.createUser(user1);

        UserDetails user2 = User.withUsername("b")
                .password(encoder.encode("b"))
                .roles(Role.BASIC.name())
                .build();
        manager.createUser(user2);

        return manager;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, HandlerMappingIntrospector introspector) throws Exception {
        var mvcMatcher = new MvcRequestMatcher.Builder(introspector);
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .passwordManagement(c -> c
                        .changePasswordPage("/profile/change-password")
                )
                .authorizeHttpRequests(c -> c
//                        .requestMatchers(HttpMethod.POST, "/users").hasRole(Role.ADMIN.name())
                        .requestMatchers(mvcMatcher.pattern(HttpMethod.POST, "/users")).hasRole(Role.ADMIN.name())
                        .requestMatchers(mvcMatcher.pattern(HttpMethod.GET, "/users")).permitAll()
                        .requestMatchers(mvcMatcher.pattern(HttpMethod.DELETE, "/users/{id}")).hasRole(Role.ADMIN.name())
                        .requestMatchers(mvcMatcher.pattern(HttpMethod.PATCH, "/users/{id}")).authenticated()
                )
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // bcrypt is used by default
    }

}
