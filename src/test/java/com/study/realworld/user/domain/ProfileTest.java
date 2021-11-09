package com.study.realworld.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProfileTest {

    @Test
    void Profile() {
        Profile profile = new Profile();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void profileEqualsHashCodeTest() {

        // given
        Profile expected = Profile.Builder()
            .username(Username.of("username"))
            .bio(Bio.of("bio"))
            .image(Image.of("image"))
            .build();

        // when
        Profile result = Profile.Builder()
            .username(Username.of("username"))
            .bio(Bio.of("bio"))
            .image(Image.of("image"))
            .build();

        // then
        assertThat(result)
            .isEqualTo(expected)
            .hasSameHashCodeAs(expected);
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}
