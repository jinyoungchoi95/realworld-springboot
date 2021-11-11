package com.study.realworld.user.controller;

import static com.study.realworld.user.controller.ApiDocumentUtils.getDocumentRequest;
import static com.study.realworld.user.controller.ApiDocumentUtils.getDocumentResponse;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.realworld.global.security.JwtAuthentication;
import com.study.realworld.global.security.JwtService;
import com.study.realworld.user.domain.Bio;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Image;
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
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private User user;

    @BeforeEach
    void beforeEach(RestDocumentationContextProvider restDocumentationContextProvider) {
        SecurityContextHolder.clearContext();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
            .apply(documentationConfiguration(restDocumentationContextProvider))
            .alwaysExpect(status().isOk())
            .build();

        user = User.builder()
            .id(1L)
            .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jake@jake.jake"))
            .password(Password.of("jakejake"))
            .build();
    }

    @Test
    void joinTest() throws Exception {

        // setup
        when(userService.join(any())).thenReturn(user);
        when(jwtService.createToken(user)).thenReturn("token");

        // given
        final String URL = "/api/users";
        final String content = "{\"user\":{\"username\":\"" + "jake"
            + "\",\"email\":\"" + "jake@jake.jake"
            + "\",\"password\":\"" + "jakejake"
            + "\",\"bio\":\"" + "I work at statefarm"
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

            .andExpect(jsonPath("$.user.username", is(user.username().value())))
            .andExpect(jsonPath("$.user.email", is(user.email().value())))
            .andExpect(jsonPath("$.user.bio", is(user.bio().value())))
            .andExpect(jsonPath("$.user.image", is(nullValue())))
            .andExpect(jsonPath("$.user.token", is("token")))
            .andDo(document("user-join",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("user.username").type(JsonFieldType.STRING).description("user's username"),
                    fieldWithPath("user.email").type(JsonFieldType.STRING).description("user's email"),
                    fieldWithPath("user.password").type(JsonFieldType.STRING).description("user's password"),
                    fieldWithPath("user.bio").type(JsonFieldType.STRING).description("user's bio").optional(),
                    fieldWithPath("user.image").type(JsonFieldType.STRING).description("users' image").optional()
                ),
                responseFields(
                    fieldWithPath("user.email").type(JsonFieldType.STRING).description("user's email"),
                    fieldWithPath("user.token").type(JsonFieldType.STRING).description("user's login token"),
                    fieldWithPath("user.username").type(JsonFieldType.STRING).description("user's username"),
                    fieldWithPath("user.bio").type(JsonFieldType.STRING).description("user's bio").optional(),
                    fieldWithPath("user.image").type(JsonFieldType.STRING).description("user's image").optional()
                )
            ))
        ;
    }


    @Test
    void loginTest() throws Exception {

        // setup
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

            .andExpect(jsonPath("$.user.username", is(user.username().value())))
            .andExpect(jsonPath("$.user.email", is(user.email().value())))
            .andExpect(jsonPath("$.user.bio", is(user.bio().value())))
            .andExpect(jsonPath("$.user.image", is(nullValue())))
            .andExpect(jsonPath("$.user.token", is("token")))
            .andDo(document("user-login",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("user.email").type(JsonFieldType.STRING).description("user's email"),
                    fieldWithPath("user.password").type(JsonFieldType.STRING).description("user's password")
                ),
                responseFields(
                    fieldWithPath("user.email").type(JsonFieldType.STRING).description("user's email"),
                    fieldWithPath("user.token").type(JsonFieldType.STRING).description("user's login token"),
                    fieldWithPath("user.username").type(JsonFieldType.STRING).description("user's username"),
                    fieldWithPath("user.bio").type(JsonFieldType.STRING).description("user's bio").optional(),
                    fieldWithPath("user.image").type(JsonFieldType.STRING).description("user's image").optional()
                )
            ))
        ;
    }

    @Test
    void getCurrentUserTest() throws Exception {

        // setup
        when(userService.findById(any())).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthentication(1L, "token"));

        // given
        final String URL = "/api/user";

        // when
        AbstractAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(1L, "token");
        ResultActions resultActions = mockMvc.perform(get(URL)
            .principal(authenticationToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print());

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.user.username", is(user.username().value())))
            .andExpect(jsonPath("$.user.email", is(user.email().value())))
            .andExpect(jsonPath("$.user.bio", is(user.bio().value())))
            .andExpect(jsonPath("$.user.image", is(nullValue())))
            .andExpect(jsonPath("$.user.token", is("token")))
            .andDo(document("user-get-current",
                getDocumentRequest(),
                getDocumentResponse(),
                responseFields(
                    fieldWithPath("user.email").type(JsonFieldType.STRING).description("user's email"),
                    fieldWithPath("user.token").type(JsonFieldType.STRING).description("user's login token"),
                    fieldWithPath("user.username").type(JsonFieldType.STRING).description("user's username"),
                    fieldWithPath("user.bio").type(JsonFieldType.STRING).description("user's bio").optional(),
                    fieldWithPath("user.image").type(JsonFieldType.STRING).description("user's image").optional()
                )
            ))
        ;
    }

    @Test
    void updateTest() throws Exception {

        // setup
        User changedUser = User.builder()
            .id(1L)
            .profile(Username.of("jakefriend"), Bio.of("I like to skateboard"), Image.of("https://i.stack.imgur.com/xHWG8.jpg"))
            .email(Email.of("jakefriend@jake.jake"))
            .password(Password.of("passwordChange"))
            .build();
        when(userService.update(any(), any())).thenReturn(changedUser);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthentication(1L, "token"));

        // given
        final String URL = "/api/user";
        final String content = "{\"user\":{\"username\":\"" + "jakefriend"
            + "\",\"email\":\"" + "jakefriend@jake.jake"
            + "\",\"password\":\"" + "passwordChange"
            + "\",\"bio\":\"" + "I like to skateboard"
            + "\",\"image\":\"" + "https://i.stack.imgur.com/xHWG8.jpg"
            + "\"}}";

        // when
        ResultActions resultActions = mockMvc.perform(put(URL)
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print());

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.user.username", is(changedUser.username().value())))
            .andExpect(jsonPath("$.user.email", is(changedUser.email().value())))
            .andExpect(jsonPath("$.user.bio", is(changedUser.bio().value())))
            .andExpect(jsonPath("$.user.image", is(changedUser.image().value())))
            .andExpect(jsonPath("$.user.token", is("token")))
            .andDo(document("user-update",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("user.username").type(JsonFieldType.STRING).description("user's username for update"),
                    fieldWithPath("user.email").type(JsonFieldType.STRING).description("user's email for update"),
                    fieldWithPath("user.password").type(JsonFieldType.STRING).description("user's password for update"),
                    fieldWithPath("user.bio").type(JsonFieldType.STRING).description("user's bio for update").optional(),
                    fieldWithPath("user.image").type(JsonFieldType.STRING).description("user's image for update").optional()
                ),
                responseFields(
                    fieldWithPath("user.email").type(JsonFieldType.STRING).description("user's email"),
                    fieldWithPath("user.token").type(JsonFieldType.STRING).description("user's login token"),
                    fieldWithPath("user.username").type(JsonFieldType.STRING).description("user's username"),
                    fieldWithPath("user.bio").type(JsonFieldType.STRING).description("user's bio").optional(),
                    fieldWithPath("user.image").type(JsonFieldType.STRING).description("user's image").optional()
                )
            ))
        ;
    }

}