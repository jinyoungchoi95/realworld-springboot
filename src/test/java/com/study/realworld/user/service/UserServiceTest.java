package com.study.realworld.user.service;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.study.realworld.global.error.ErrorCode;
import com.study.realworld.global.error.exception.BusinessException;
import com.study.realworld.user.domain.Bio;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Image;
import com.study.realworld.user.domain.Password;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.UserRepository;
import com.study.realworld.user.domain.Username;
import com.study.realworld.user.service.model.UserUpdateModel;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("이미 존재하는 유저네임이 들어왔을 때 Exception이 발생되어야 한다.")
    void existUsernameJoinTest() {

        // setup & given
        User user = User.Builder().build();
        when(userRepository.findByUsername(any()))
            .thenReturn(Optional.of(user));

        // when & then
        assertThatExceptionOfType(BusinessException.class)
            .isThrownBy(() -> userService.join(user))
            .withMessageMatching(ErrorCode.USERNAME_DUPLICATION.getMessage());
    }

    @Test
    @DisplayName("이미 존재하는 이메일이 들어왔을 때 Exception이 발생되어야 한다.")
    void existEmailJoinTest() {

        // setup & given
        User user = User.Builder().build();
        when(userRepository.findByUsername(any())).thenReturn(empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        // when & then
        assertThatExceptionOfType(BusinessException.class)
            .isThrownBy(() -> userService.join(user))
            .withMessageMatching(ErrorCode.EMAIL_DUPLICATION.getMessage());
    }

    @Test
    @DisplayName("새로운 유저가 왔을 때 정상적으로 해당 유저가 저장되고 유저 유저정보가 반환되어야 한다.")
    void successJoinTest() {

        // setup & given
        when(userRepository.findByUsername(any())).thenReturn(empty());
        when(userRepository.findByEmail(any())).thenReturn(empty());
        Password password = new Password("password");
        when(passwordEncoder.encode(password.password())).thenReturn("encoded_password");
        User input = User.Builder()
            .username(new Username("username"))
            .email(new Email("test@test.com"))
            .password(password)
            .bio(new Bio("bio"))
            .image(new Image("image"))
            .build();
        when(userRepository.save(any())).thenReturn(
            User.Builder()
                .id(1L)
                .username(input.username())
                .email(input.email())
                .password(new Password("encoded_password"))
                .bio(input.bio().orElse(null))
                .image(input.image().orElse(null))
                .build()
        );

        // when
        User user = userService.join(input);

        // then
        assertThat(user.id()).isEqualTo(1L);
        assertThat(user.username()).isEqualTo(new Username("username"));
        assertThat(user.email()).isEqualTo(new Email("test@test.com"));
        assertThat(user.password().password())
            .isEqualTo("encoded_password");
        assertThat(user.bio().get()).isEqualTo(new Bio("bio"));
        assertThat(user.image().get()).isEqualTo(new Image("image"));
    }

    @Test
    @DisplayName("로그인 요청한 이메일이 존재하지 않는 이메일이면 Exception을 반환해야 한다.")
    void loginFailByEmailTest() {

        // setup & given
        Email email = new Email("test@test.com");
        Password password = new Password("password");
        when(userRepository.findByEmail(email)).thenReturn(empty());

        // when && then
        assertThatExceptionOfType(BusinessException.class)
            .isThrownBy(() -> userService.login(email, password))
            .withMessageMatching(ErrorCode.USER_NOTFOUND.getMessage());
    }

    // TODO
    @Test
    @DisplayName("로그인 요청한 이메일에 매칭되지 않는 패스워드이면 Exception을 반환해야 한다.")
    void loginFailByPasswordTest() {

        // setup & given
        Email email = new Email("test@test.com");
        Password password = new Password("password");
        User user = User.Builder().password(new Password("encoded_password")).build();
        when(userRepository.findByEmail(email)).thenReturn(ofNullable(user));
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(false);

        // when & then
        assertThatExceptionOfType(Exception.class)
            .isThrownBy(() -> userService.login(email, password));
    }

    @Test
    @DisplayName("로그인 요청한 이메일 페스워드가 실제 유저일 때 유저를 반환한다.")
    void loginSuccessTest() {

        // setup & given
        Email email = new Email("test@test.com");
        Password password = new Password("password");
        User input = User.Builder().email(email).password(new Password("encoded_password")).build();
        when(userRepository.findByEmail(email)).thenReturn(ofNullable(input));
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);

        // when
        User user = userService.login(email, password);

        // then
        assertThat(user).isNotNull();
        assertThat(user.email().toString()).isEqualTo("test@test.com");
        assertThat(user.password().password()).isEqualTo("encoded_password");
    }

    @Test
    @DisplayName("id를 가지고 유저를 조회할 때 해당 유저가 존재하면 해당 유저를 반환한다.")
    void findByIdSuccessTest() {

        // setup & given
        User user = User.Builder().email(new Email("test@test.com")).build();
        Long userId = 2L;
        when(userRepository.findById(userId)).thenReturn(ofNullable(user));

        // when
        User result = userService.findById(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(user.email());
    }

    @Test
    @DisplayName("null인 id를 가지고 유저를 조회할 때  IllegalArgumentException이 발생되어야 한다.")
    void findByNullIdFailTest() {

        // setup & given
        Long userId = null;
        when(userRepository.findById(userId)).thenThrow(IllegalArgumentException.class);

        // when & then
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> userService.findById(userId));
    }

    @Test
    @DisplayName("없는 유저의 id를 가지고 유저를 조회할 때 Exception이 반환되어야 한다.")
    void findByNoUserIdFailTest() {

        // setup & given
        Long userId = 2L;
        when(userRepository.findById(userId)).thenReturn(empty());

        // when & then
        assertThatExceptionOfType(BusinessException.class)
            .isThrownBy(() -> userService.findById(userId))
            .withMessageMatching(ErrorCode.USER_NOTFOUND.getMessage());
    }

    @Test
    @DisplayName("없는 유저의 id를 가지고 update 요청 시 exception이 발생되어야 한다.")
    void updateFailByNotFoundUserTest() {

        // setup & given
        Long userId = 2L;
        UserUpdateModel userUpdateModel = new UserUpdateModel(
            new Username("username"),
            new Email("test@test.com"),
            new Password("password"),
            new Bio("bio"),
            new Image("image")
        );
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // when & then
        assertThatExceptionOfType(BusinessException.class)
            .isThrownBy(() -> userService.update(userUpdateModel, userId))
            .withMessageMatching(ErrorCode.USER_NOTFOUND.getMessage());
    }

    @Nested
    @DisplayName("User를 update할 때")
    class UserUpdateTest {

        private User user;
        private User originUser;
        private Long userId = 1L;

        @BeforeEach
        void beforeEach() {
            user = User.Builder()
                .username(new Username("username"))
                .email(new Email("test@test.com"))
                .password(new Password("encoded_password"))
                .bio(new Bio("bio"))
                .image(new Image("image"))
                .build();
            originUser = User.Builder()
                .username(new Username("username"))
                .email(new Email("test@test.com"))
                .password(new Password("encoded_password"))
                .bio(new Bio("bio"))
                .image(new Image("image"))
                .build();
            when(userRepository.findById(userId)).thenReturn(Optional.ofNullable(user));
        }

        @Nested
        @DisplayName("[username]")
        class UsernameTest {

            @Test
            @DisplayName("현재 user와 동일한 username이 들이어오면 변경되지 않아야 한다.")
            void updateFailByEqualWithNowUsernameTest() {

                // given
                Username username = new Username("username");
                UserUpdateModel userUpdateModel = new UserUpdateModel(username, null,
                    null, null, null);

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.username()).isEqualTo(originUser.username());
            }

            @Test
            @DisplayName("기본에 있는 username이 업데이트 요청이 들어오면 Exception이 발생되어야한다.")
            void updateFailByDuplicatedUsernameTest() {

                // setup & given
                Username username = new Username("usernameChange");
                UserUpdateModel userUpdateModel = new UserUpdateModel(username, null,
                    null, null, null);
                when(userRepository.findByUsername(username))
                    .thenReturn(Optional.ofNullable(User.Builder().build()));

                // when & then
                assertThatExceptionOfType(BusinessException.class)
                    .isThrownBy(() -> userService.update(userUpdateModel, userId))
                    .withMessageMatching(ErrorCode.USERNAME_DUPLICATION.getMessage());
            }

            @Test
            @DisplayName("username이 변경가능 할 경우 변경되어야 한다.")
            void updateSuccessByUsernameTest() {

                // setup & given
                Username username = new Username("usernameChange");
                UserUpdateModel userUpdateModel = new UserUpdateModel(username, null,
                    null, null, null);
                when(userRepository.findByUsername(username))
                    .thenReturn(Optional.empty());

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.username()).isEqualTo(username);
                assertThat(result.username()).isNotEqualTo(originUser.username());

                assertThat(result.email()).isEqualTo(originUser.email());
                assertThat(result.password().password())
                    .isEqualTo(originUser.password().password());
                assertThat(result.bio()).isEqualTo(originUser.bio());
                assertThat(result.image()).isEqualTo(originUser.image());
            }
        }

        @Nested
        @DisplayName("[email]")
        class EmailTest {

            @Test
            @DisplayName("현재 user와 동일한 email이 들이어오면 변경되지 않아야 한다.")
            void updateFailByEqualWithNowEmailTest() {

                // given
                Email email = new Email("test@test.com");
                UserUpdateModel userUpdateModel = new UserUpdateModel(null, email,
                    null, null, null);

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.email()).isEqualTo(originUser.email());
            }

            @Test
            @DisplayName("기본에 있는 email이 업데이트 요청이 들어오면 Exception이 발생되어야한다.")
            void updateFailByDuplicatedEmailTest() {

                // setup & given
                Email email = new Email("change@change.com");
                UserUpdateModel userUpdateModel = new UserUpdateModel(null, email,
                    null, null, null);
                when(userRepository.findByEmail(email))
                    .thenReturn(Optional.ofNullable(User.Builder().build()));

                // when & then
                assertThatExceptionOfType(BusinessException.class)
                    .isThrownBy(() -> userService.update(userUpdateModel, userId))
                    .withMessageMatching(ErrorCode.EMAIL_DUPLICATION.getMessage());
            }

            @Test
            @DisplayName("email이 변경가능 할 경우 변경되어야 한다.")
            void updateSuccessByEmailTest() {

                // setup & given
                Email email = new Email("change@change.com");
                UserUpdateModel userUpdateModel = new UserUpdateModel(null, email,
                    null, null, null);
                when(userRepository.findByEmail(email))
                    .thenReturn(Optional.empty());

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.username()).isEqualTo(originUser.username());

                assertThat(result.email()).isEqualTo(email);
                assertThat(result.email()).isNotEqualTo(originUser.email());

                assertThat(result.password().password())
                    .isEqualTo(originUser.password().password());
                assertThat(result.bio()).isEqualTo(originUser.bio());
                assertThat(result.image()).isEqualTo(originUser.image());
            }
        }

        @Nested
        @DisplayName("[password]")
        class PasswordTest {

            @Test
            @DisplayName("현재 user와 동일한 password가 들어오면 변경되지 않아야 한다.")
            void updateFailByEqualWithNowPasswordTest() {

                // setup & given
                Password password = new Password("password");
                UserUpdateModel userUpdateModel = new UserUpdateModel(null, null,
                    password, null, null);
                when(passwordEncoder.encode("password")).thenReturn("encoded_password");

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.password().password()).isEqualTo(originUser.password().password());
            }

            @Test
            @DisplayName("password가 변경가능 할 경우 변경되어야 한다.")
            void updateSuccessByPasswordTest() {

                // setup & given
                Password password = new Password("passwordChange");
                UserUpdateModel userUpdateModel = new UserUpdateModel(null, null,
                    password, null, null);
                when(passwordEncoder.encode("passwordChange")).thenReturn("encoded_Change");

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.username()).isEqualTo(originUser.username());
                assertThat(result.email()).isEqualTo(originUser.email());

                assertThat(result.password().password())
                    .isEqualTo("encoded_Change");
                assertThat(result.password().password())
                    .isNotEqualTo(originUser.password().password());

                assertThat(result.bio()).isEqualTo(originUser.bio());
                assertThat(result.image()).isEqualTo(originUser.image());
            }
        }

        @Nested
        @DisplayName("[bio]")
        class BioTest {

            @Test
            @DisplayName("현재 user와 동일한 bio가 들이어오면 변경되지 않아야 한다.")
            void updateFailByEqualWithNowBioTest() {

                // given
                Bio bio = new Bio("bio");
                UserUpdateModel userUpdateModel = new UserUpdateModel(null, null,
                    null, bio, null);

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.bio()).isEqualTo(originUser.bio());
            }

            @Test
            @DisplayName("bio가 변경가능 할 경우 변경되어야 한다.")
            void updateSuccessByBioTest() {

                // given
                Bio bio = new Bio("bioChange");
                UserUpdateModel userUpdateModel = new UserUpdateModel(null, null,
                    null, bio, null);

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.username()).isEqualTo(originUser.username());
                assertThat(result.email()).isEqualTo(originUser.email());
                assertThat(result.password().password())
                    .isEqualTo(originUser.password().password());

                assertThat(result.bio().get()).isEqualTo(bio);
                assertThat(result.bio().get()).isNotEqualTo(originUser.bio());

                assertThat(result.image()).isEqualTo(originUser.image());
            }
        }

        @Nested
        @DisplayName("[image]")
        class ImageTest {

            @Test
            @DisplayName("현재 user와 동일한 image가 들이어오면 변경되지 않아야 한다.")
            void updateFailByEqualWithNowImageTest() {

                // given
                Image image = new Image("image");
                UserUpdateModel userUpdateModel = new UserUpdateModel(null, null,
                    null, null, image);

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.image()).isEqualTo(originUser.image());
            }

            @Test
            @DisplayName("image가 변경가능 할 경우 변경되어야 한다.")
            void updateSuccessByImageTest() {

                // given
                Image image = new Image("imageChange");
                UserUpdateModel userUpdateModel = new UserUpdateModel(null, null,
                    null, null, image);

                // when
                User result = userService.update(userUpdateModel, userId);

                // then
                assertThat(result.username()).isEqualTo(originUser.username());
                assertThat(result.email()).isEqualTo(originUser.email());
                assertThat(result.password().password()).isEqualTo(originUser.password().password());
                assertThat(result.bio()).isEqualTo(originUser.bio());

                assertThat(result.image().get()).isEqualTo(image);
                assertThat(result.image().get()).isNotEqualTo(originUser.image());
            }

        }

    }

}
