package com.example.simple_rest_api.repositories;

import com.example.simple_rest_api.model.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class RepositorySim {

    private final HashMap<Long, User> usersById = new HashMap<>();

    private final AtomicLong idCounter = new AtomicLong();

    public void save(User user) {
        if (user.getId() == null) {
            user.setId(idCounter.incrementAndGet());
        }
        usersById.put(user.getId(), user);
    }

    public void deleteById(Long id) {
        usersById.remove(id);
    }

    public User findById(Long id) {
        return usersById.get(id);
    }

    public List<User> findByBirthRange(LocalDate from, LocalDate to) {
        return usersById.values().parallelStream()
                .filter(user -> !(user.getBirthDate().isBefore(from) || user.getBirthDate().isAfter(to)))
                .sorted(Comparator.comparing(User::getBirthDate))
                .collect(Collectors.toList());
    }
}
