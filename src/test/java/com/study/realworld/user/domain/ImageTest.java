package com.study.realworld.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ImageTest {

    @Test
    void imageTest() {
        Image image = new Image();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void imageEqualsHashCodeTest() {

        // given
        String image = "image";

        // when
        Image result = Image.of(image);

        // then
        assertThat(result)
            .isEqualTo(Image.of(image))
            .hasSameHashCodeAs(Image.of(image));
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}
