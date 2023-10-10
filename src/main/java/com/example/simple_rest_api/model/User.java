package com.example.simple_rest_api.model;


import com.example.simple_rest_api.securiry.Authority;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

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

    @NotBlank
    private String password;

    @Column(unique = true)
    @Email(regexp = EMAIL_PATTERN, message = "Invalid email. Email should match pattern " + User.EMAIL_PATTERN)
    private String email;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable
    private List<Authority> authorities;

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
