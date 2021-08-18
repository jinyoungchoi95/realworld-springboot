package com.study.realworld.user.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.realworld.security.JwtService;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Password;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.Username;
import com.study.realworld.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .alwaysExpect(status().isOk())
            .build();
    }

    @Test
    void joinTest() throws Exception {

        // setup
        User user = User.Builder()
            .username(new Username("username"))
            .email(new Email("test@test.com"))
            .password(new Password("password"))
            .build();

        when(userService.join(any())).thenReturn(user);
        when(jwtService.createToken(user)).thenReturn("token");

        // given
        final String URL = "/api/users";
        final String content = "{\"user\":{\"username\":\"" + "username"
            + "\",\"email\":\"" + "test@test.com"
            + "\",\"password\":\"" + "password"
            + "\"}}";

        // when
        ResultActions resultActions = mockMvc.perform(post(URL)
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print());

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.user.username", is("username")))
            .andExpect(jsonPath("$.user.email", is("test@test.com")))
            .andExpect(jsonPath("$.user.bio", is("null")))
            .andExpect(jsonPath("$.user.image", is("null")))
            .andExpect(jsonPath("$.user.token", is("token")))
        ;
    }

    @Test
    void loginTest() throws Exception {

        // setup
        User user = User.Builder()
            .username(new Username("username"))
            .email(new Email("test@test.com"))
            .password(new Password("password"))
            .build();

        when(userService.login(any(), any())).thenReturn(user);
        when(jwtService.createToken(user)).thenReturn("token");

        // given
        final String URL = "/api/users/login";
        final String content = "{\"user\":{\"email\":\"" + "test@test.com"
            + "\",\"password\":\"" + "password"
            + "\"}}";

        // when
        ResultActions resultActions = mockMvc.perform(post(URL)
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print());

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.user.username", is("username")))
            .andExpect(jsonPath("$.user.email", is("test@test.com")))
            .andExpect(jsonPath("$.user.bio", is("null")))
            .andExpect(jsonPath("$.user.image", is("null")))
            .andExpect(jsonPath("$.user.token", is("token")))
        ;
    }

}