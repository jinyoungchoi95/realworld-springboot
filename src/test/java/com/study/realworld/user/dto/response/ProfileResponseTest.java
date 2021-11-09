package com.study.realworld.user.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.study.realworld.user.domain.Bio;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Password;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.Username;
import com.study.realworld.user.dto.response.ProfileResponse.ProfileResponseNested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProfileResponseTest {

    private User user;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
            .id(1L)
            .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jake@jake.jake"))
            .password(Password.of("jakejake"))
            .build();
    }

    @Test
    void profileResponseTest() {
        ProfileResponse profileResponse = new ProfileResponse();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void profileResponseEqualsHashCodeTest() {

        // given
        ProfileResponse expected = ProfileResponse.fromUserAndFollowing(user, false);

        // when
        ProfileResponse result = ProfileResponse.fromUserAndFollowing(user, false);

        // then
        assertThat(result)
            .isEqualTo(expected)
            .hasSameHashCodeAs(expected);
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

    @Test
    void profileResponseNestedTest() {
        ProfileResponseNested profileResponseNested = new ProfileResponseNested();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void profileResponseNestedEqualsHashCodeTest() {

        // given
        ProfileResponseNested expected = ProfileResponseNested.fromProfileAndFollowing(user.profile(), false);

        // when
        ProfileResponseNested result = ProfileResponseNested.fromProfileAndFollowing(user.profile(), false);

        // then
        assertThat(result)
            .isEqualTo(expected)
            .hasSameHashCodeAs(expected);
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}