package com.study.realworld.follow.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.study.realworld.user.domain.Bio;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Password;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FollowResponseTest {

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
    void followResponseTest() {
        FollowResponse followResponse = new FollowResponse();
    }

    @Test
    void followResponseEqualsHashCodeTest() {

        // given
        FollowResponse expected = FollowResponse.fromUserAndFollowing(user, false);

        // when
        FollowResponse result = FollowResponse.fromUserAndFollowing(user, false);

        // then
        assertThat(result)
            .isEqualTo(expected)
            .hasSameHashCodeAs(expected);
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}