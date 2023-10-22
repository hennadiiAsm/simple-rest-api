package com.example.simple_rest_api;


import com.example.simple_rest_api.controllers.UserController;
import com.example.simple_rest_api.model.User;
import com.example.simple_rest_api.repositories.UserRepository;
import com.example.simple_rest_api.securiry.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@ContextConfiguration(classes = {UserController.class, SecurityConfig.class})
@TestPropertySource(locations = "classpath:application.yml")
class SimpleRestApiApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository repo;

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_PASSWORD = "password";
    private static final String VALID_FIRST_NAME = "Nick";
    private static final String VALID_LAST_NAME = "Freak";
    private static LocalDate VALID_BIRTH_DATE;

    @BeforeAll
    static void init(@Value("${user.minAge}") int minAge) {
        VALID_BIRTH_DATE = LocalDate.now().minusYears(minAge);
    }

    private User getValidUser() {
        return new User(VALID_EMAIL, VALID_PASSWORD, null, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_BIRTH_DATE);
    }

    @Test
    @WithMockUser(roles = "BASIC")
    void whenPostWithRoleBASIC_returnsStatus403() throws Exception {
        mvc.perform(post("/users"))
                .andExpect(authenticated())
                .andExpect(status().isForbidden());
        Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
    }

    @Nested
    @WithMockUser(roles = "ADMIN")
    class AuthorizedPostRequests {
        @Test
        void whenPostAndUserEmailIsInvalid_returnsStatus400() throws Exception {
            User user = getValidUser();
            user.setEmail("user@");
            badPostRequest(user);
            Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
        }

        private void badPostRequest(User user) throws Exception {
            String body = objectMapper.writeValueAsString(user);

            mvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(authenticated())
                    .andExpect(status().isBadRequest());
        }

        @Test
        void whenPostAndUserPasswordIsBlank_returnsStatus400() throws Exception {
            User user = getValidUser();
            user.setPassword(" ");
            badPostRequest(user);
            Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
        }

        @Test
        void whenPostAndUserFirstNameIsBlank_returnsStatus400() throws Exception {
            User user = getValidUser();
            user.setFirstName(" ");
            badPostRequest(user);
            Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
        }

        @Test
        void whenPostAndUserLastNameIsBlank_returnsStatus400() throws Exception {
            User user = getValidUser();
            user.setLastName(" ");
            badPostRequest(user);
            Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
        }

        @Test
        void whenPostAndUserIsTooYoung_returnsStatus400() throws Exception {
            User user = getValidUser();
            user.setBirthDate(VALID_BIRTH_DATE.plusDays(1));
            badPostRequest(user);
            Mockito.verify(repo, Mockito.never()).save(Mockito.any(User.class));
        }

        @Test
        void whenPostAndUserIsValid_returnsStatus201AndProperLocation() throws Exception {
            User user = getValidUser();
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
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenDeleteWithRoleADMIN_returnsStatus200() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        Mockito.verify(repo).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "BASIC")
    void whenDeleteWithRoleBASIC_returnsStatus403() throws Exception {
        mvc.perform(delete("/users/1"))
                .andExpect(status().isForbidden());

        Mockito.verify(repo, Mockito.never()).deleteById(1L);
    }

    @Test
    @WithMockUser
    void whenPatchAndSamePrincipalAndUserBirthDateIsInvalid_returnsStatus400() throws Exception {
        initMockRepositoryWithSamePrincipal();

        User user = getValidUser();
        user.setBirthDate(VALID_BIRTH_DATE.plusDays(1));
        String body = objectMapper.writeValueAsString(user);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    private void initMockRepositoryWithSamePrincipal() {
        User user = new User();
        user.setId(1L);
        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(user));

        User principalUser = new User();
        principalUser.setId(1L);
        Mockito.when(repo.findByEmail("user")).thenReturn(Optional.of(principalUser));
    }

    @Test
    @WithMockUser
    void whenPatchAndSamePrincipalAndUserIsValid_returnsStatus200() throws Exception {
        initMockRepositoryWithSamePrincipal();

        User user = getValidUser();
        String body = objectMapper.writeValueAsString(user);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void whenPatchAndAnotherPrincipalNotADMIN_returnsStatus403() throws Exception {
        initMockRepositoryWithAnotherPrincipal();

        User user = getValidUser();
        String body = objectMapper.writeValueAsString(user);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    private void initMockRepositoryWithAnotherPrincipal() {
        User user = new User();
        user.setId(1L);
        Mockito.when(repo.findById(1L)).thenReturn(Optional.of(user));

        User principalUser = new User();
        principalUser.setId(2L);
        Mockito.when(repo.findByEmail("user")).thenReturn(Optional.of(principalUser));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenPatchAndAnotherPrincipalADMINAndUserIsValid_returnsStatus200() throws Exception {
        initMockRepositoryWithAnotherPrincipal();

        User user = getValidUser();
        String body = objectMapper.writeValueAsString(user);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenGetAndFromLaterThanTo_returnsStatus400() throws Exception {
        LocalDate from = VALID_BIRTH_DATE.plusDays(1);
        LocalDate to = VALID_BIRTH_DATE.minusDays(1);

        mvc.perform(get("/users")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                )
                .andExpect(status().isBadRequest());

        Mockito.verify(repo, Mockito.never()).findByBirthDateBetween(from, to);
    }

    @Test
    void whenGetAndFromEarlierThanTo_returnsStatus200() throws Exception {
        LocalDate from = VALID_BIRTH_DATE.minusDays(1);
        LocalDate to = VALID_BIRTH_DATE.plusDays(1);

        User user = getValidUser();
        user.setId(1L);

        Mockito.when(repo.findByBirthDateBetween(from, to)).thenReturn(List.of(user));

        String jsonContent = objectMapper.writeValueAsString(List.of(user));

        mvc.perform(get("/users")
                        .queryParam("from", from.toString())
                        .queryParam("to", to.toString())
                )
                .andExpect(status().isOk())
                .andExpect(content().json(jsonContent));

        Mockito.verify(repo).findByBirthDateBetween(from, to);
    }
}
