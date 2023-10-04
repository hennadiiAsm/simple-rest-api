package com.example.simple_rest_api.controllers;

import com.example.simple_rest_api.exceptions.FieldException;
import com.example.simple_rest_api.model.User;
import com.example.simple_rest_api.repositories.RepositorySim;
import com.example.simple_rest_api.util.SimpleDTO;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/users", produces = "application/json")
public class UserController {

    @Value("${user.minAge}")
    private int minAge;

    private volatile LocalDate lastValidBirthDate;

    private final RepositorySim repo;

    @Autowired
    public UserController(RepositorySim repo) {
        this.repo = repo;
    }

    @PostConstruct
    private void init() {
        /*
        One could force clients to provide info about timezone in order to properly calculate
        lastValidBirthDate, but for the sake of simplicity LocalDate class is used here
         */
        lastValidBirthDate = LocalDate.now().minusYears(minAge);

        var exeService = Executors.newSingleThreadScheduledExecutor();

        long millisPerDay = ChronoUnit.DAYS.getDuration().toMillis();
        long millisOfDay = LocalTime.now().toNanoOfDay() / 1_000_000L;
        long initialDelay = millisPerDay - millisOfDay; // millis to the end of the current day

        exeService.scheduleAtFixedRate(() -> lastValidBirthDate = lastValidBirthDate.plusDays(1),
                initialDelay,
                millisPerDay,
                TimeUnit.MILLISECONDS);
    }

    @PostMapping
    private ResponseEntity<Void> createUser(@Valid @RequestBody User user, UriComponentsBuilder ucb) {
        checkBirthDate(user.getBirthDate());

        user.setId(null); // to avoid inconsistency
        repo.save(user);

        URI locationOfNewUser = ucb
                .path("users/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(locationOfNewUser).build();
    }

    private void checkBirthDate(LocalDate birthDate) {
        if (birthDate.isAfter(lastValidBirthDate)) {
            throw new FieldException(
                    "Only users who are more than " + minAge + " years are allowed to use resource. " +
                            "Provided birth date: " + birthDate);
        }
    }

    @PatchMapping("/{id}")
    private ResponseEntity<Void> update(@PathVariable Long id, @RequestBody User user) {
        User userFromRepository = repo.findById(id);
        if (userFromRepository == null) {
            return ResponseEntity.notFound().build();
        }

        if (user.getEmail() != null) {
            if (user.getEmail().matches(User.EMAIL_PATTERN)) {
                userFromRepository.setEmail(user.getEmail());
            } else {
                throw new FieldException("Invalid email. Email should match pattern " + User.EMAIL_PATTERN);
            }
        }

        if (user.getFirstName() != null) {
            if (!user.getFirstName().isBlank()) {
                userFromRepository.setFirstName(user.getFirstName());
            } else {
                throw new FieldException("First name should not be blank");
            }
        }

        if (user.getLastName() != null) {
            if (!user.getLastName().isBlank()) {
                userFromRepository.setLastName(user.getLastName());
            } else {
                throw new FieldException("Last name should not be blank");
            }
        }

        if (user.getBirthDate() != null) {
            checkBirthDate(user.getBirthDate());
            userFromRepository.setBirthDate(user.getBirthDate());
        }

        if (user.getAddress() != null) userFromRepository.setAddress(user.getAddress());
        if (user.getPhoneNumber() != null) userFromRepository.setPhoneNumber(user.getPhoneNumber());

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    private ResponseEntity<Void> replace(@PathVariable Long id, @Valid @RequestBody User user) {
        User userFromRepository = repo.findById(id);
        if (userFromRepository == null) {
            return ResponseEntity.notFound().build();
        }
        checkBirthDate(user.getBirthDate());

        user.setId(id); // to avoid inconsistency
        repo.save(user);
        return ResponseEntity.ok().build();
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}")
    private void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }

    @GetMapping
    public ResponseEntity<List<User>> getByBirthDateRange(@RequestParam LocalDate from,
                                                          @RequestParam LocalDate to) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        } else {
            return ResponseEntity.ok(repo.findByBirthRange(from, to));
        }
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    private ResponseEntity<SimpleDTO> handle(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(SimpleDTO.of("Required request parameter " + ex.getParameterName() + " in format yyyy-MM-dd"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<SimpleDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest()
                .body(SimpleDTO.of(errors));
    }

    @ExceptionHandler({FieldException.class})
    private ResponseEntity<SimpleDTO> handleCustomExceptions(Exception ex) {
        return ResponseEntity.badRequest()
                .body(SimpleDTO.of(ex.getMessage()));
    }
}
