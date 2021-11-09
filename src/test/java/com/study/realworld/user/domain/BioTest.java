package com.study.realworld.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BioTest {

    @Test
    void bioTest() {
        Bio bio = new Bio();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void bioEqualsHashCodeTest() {

        // given
        String bio = "bio";

        // when
        Bio result = Bio.of(bio);

        // when & then
        assertThat(result)
            .isEqualTo(Bio.of(bio))
            .hasSameHashCodeAs(Bio.of(bio));
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}
