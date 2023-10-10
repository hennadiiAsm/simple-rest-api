package com.example.simple_rest_api.repositories;

import com.example.simple_rest_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByBirthDateBetween(LocalDate from, LocalDate to);

}
