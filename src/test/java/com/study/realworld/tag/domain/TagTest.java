package com.study.realworld.tag.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TagTest {

    @Test
    void tagTest() {
        Tag tag = new Tag();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void tagEqualsHashCodeTest() {

        // given
        String name = "name";

        // when
        Tag result = Tag.of(name);

        // then
        assertThat(result)
            .isEqualTo(Tag.of(name))
            .hasSameHashCodeAs(Tag.of(name));
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}