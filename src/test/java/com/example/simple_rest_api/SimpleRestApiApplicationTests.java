package com.example.simple_rest_api;


import com.example.simple_rest_api.controllers.UserController;
import com.example.simple_rest_api.model.User;
import com.example.simple_rest_api.repositories.RepositorySim;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = UserController.class)
@TestPropertySource(locations = "classpath:application.properties")
class ClearSolutionsApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RepositorySim repo;

    private static final String VALID_EMAIL = "a@a";
    private static final String VALID_FIRST_NAME = "A";
    private static final String VALID_LAST_NAME = "B";
    private static LocalDate VALID_BIRTH_DATE;

    @BeforeAll
    static void init(@Value("${user.minAge}") int minAge) {
        VALID_BIRTH_DATE = LocalDate.now().minusYears(minAge);
    }

    @Test
    void whenPostAndUserEmailIsBlank_returnsStatus400() throws Exception {
        User user = new User(" ", VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        badPostRequest(user);
        Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
    }

    private void badPostRequest(User user) throws Exception {
        String body = objectMapper.writeValueAsString(user);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPostAndUserEmailIsInvalid_returnsStatus400() throws Exception {
        User user = new User("a@", VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        badPostRequest(user);
        Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void whenPostAndUserFirstNameIsBlank_returnsStatus400() throws Exception {
        User user = new User(VALID_EMAIL, " ", VALID_LAST_NAME, VALID_BIRTH_DATE);
        badPostRequest(user);
        Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void whenPostAndUserLastNameIsBlank_returnsStatus400() throws Exception {
        User user = new User(VALID_EMAIL, VALID_FIRST_NAME, " ", VALID_BIRTH_DATE);
        badPostRequest(user);
        Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void whenPostAndUserIsTooYoung_returnsStatus400() throws Exception {
        User user = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME,
                VALID_BIRTH_DATE.plusDays(1));
        badPostRequest(user);
        Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void whenPostAndUserIsValid_returnsStatus201AndProperLocation() throws Exception {
        User user = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        String body = objectMapper.writeValueAsString(user);

        Mockito.doAnswer(invocationOnMock -> {
            User userToSave = invocationOnMock.getArgument(0, User.class);
            userToSave.setId(1L);
            return null;
        }).when(repo).save(Mockito.any(User.class));

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("location", "http://localhost/users/1"));

        Mockito.verify(repo, Mockito.times(1)).save(Mockito.any(User.class));
    }

    @Test
    void whenPutAndUserIsNotCreatedYet_returnsStatus404() throws Exception {
        User user = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        String body = objectMapper.writeValueAsString(user);

        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());

        Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void whenPutAndUserIsValid_returnsStatus200() throws Exception {
        initMockRepository();

        User user = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        String body = objectMapper.writeValueAsString(user);

        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        Mockito.verify(repo, Mockito.times(1)).save(Mockito.any(User.class));
    }

    private void initMockRepository() {
        User user = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        user.setId(1L);

        Mockito.when(repo.findById(1L)).thenReturn(user);
    }

    @Test
    void whenPutAndUserEmailIsNotValid_returnsStatus400() throws Exception {
        initMockRepository();

        User invalidUser = new User("a@", VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        String invalidBody = objectMapper.writeValueAsString(invalidUser);

        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());

        Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void whenPutAndUserBirthDateIsNotValid_returnsStatus400() throws Exception {
        initMockRepository();

        User invalidUser = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, LocalDate.now());
        String invalidBody = objectMapper.writeValueAsString(invalidUser);

        mvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());

        Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void whenDelete(@Autowired RepositorySim repo) throws Exception {
        User user = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        Mockito.when(repo.findByBirthRange(LocalDate.MIN, LocalDate.MAX)).thenReturn(List.of(user));

        assertEquals(1, repo.findByBirthRange(LocalDate.MIN, LocalDate.MAX).size());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        Mockito.verify(repo).deleteById(1L);
    }

    @Test
    void whenPatchAndUserEmailIsInvalid_returnsStatus400() throws Exception {
        initMockRepository();

        User invalidUser = new User("a@", VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        String invalidBody = objectMapper.writeValueAsString(invalidUser);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPatchAndUserBirthDateIsInvalid_returnsStatus400() throws Exception {
        initMockRepository();

        User invalidUser = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE.plusDays(1));
        String invalidBody = objectMapper.writeValueAsString(invalidUser);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPatchAndUserIsValid_returnsStatus200() throws Exception {
        initMockRepository();

        User user = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        String body = objectMapper.writeValueAsString(user);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void whenGetAndFromLaterThanTo_returnsStatus400() throws Exception {
        LocalDate from = VALID_BIRTH_DATE.plusDays(1);
        LocalDate to = VALID_BIRTH_DATE.minusDays(1);

        mvc.perform(get("/users?from=" + from + "&to=" + to))
                .andExpect(status().isBadRequest());

        Mockito.verify(repo, Mockito.never()).findByBirthRange(from, to);
    }

    @Test
    void whenGetAndFromEarlierThanTo_returnsStatus200() throws Exception {
        LocalDate from = VALID_BIRTH_DATE.minusDays(1);
        LocalDate to = VALID_BIRTH_DATE.plusDays(1);

        User user = new User(VALID_EMAIL, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
        user.setId(1L);

        Mockito.when(repo.findByBirthRange(from, to)).thenReturn(List.of(user));

        String jsonContent = objectMapper.writeValueAsString(List.of(user));

        mvc.perform(get("/users?from=" + from + "&to=" + to))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonContent));

        Mockito.verify(repo).findByBirthRange(from, to);
    }
}