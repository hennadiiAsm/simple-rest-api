package com.example.simple_rest_api.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name = "users")
public class User {

    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    @Email(regexp = EMAIL_PATTERN)
    private String email;

    @NotBlank
    private String password;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable
    private Collection<GrantedAuthority> authorities;

    private boolean enabled = true;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private LocalDate birthDate;

    private String address;

    private String phoneNumber;

    public User(String email, String firstName, String lastName, LocalDate birthDate) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public User(
            String email, String password, Collection<GrantedAuthority> authorities,
            String firstName, String lastName, LocalDate birthDate) {
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;

        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
