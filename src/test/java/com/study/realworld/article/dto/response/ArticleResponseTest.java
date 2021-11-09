package com.study.realworld.article.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.study.realworld.article.domain.Article;
import com.study.realworld.article.domain.ArticleContent;
import com.study.realworld.article.domain.Body;
import com.study.realworld.article.domain.Description;
import com.study.realworld.article.domain.SlugTitle;
import com.study.realworld.article.domain.Title;
import com.study.realworld.article.dto.response.ArticleResponse.ArticleResponseNested;
import com.study.realworld.tag.domain.Tag;
import com.study.realworld.user.domain.Bio;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Password;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.Username;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArticleResponseTest {

    private User user;
    private Article article;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
            .id(1L)
            .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jake@jake.jake"))
            .password(Password.of("jakejake"))
            .build();

        ArticleContent articleContent = ArticleContent.builder()
            .slugTitle(SlugTitle.of(Title.of("How to train your dragon")))
            .description(Description.of("Ever wonder how?"))
            .body(Body.of("It takes a Jacobian"))
            .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
            .build();

        article = Article.from(articleContent, user);
    }

    @Test
    void articleResponseTest() {
        ArticleResponse articleResponse = new ArticleResponse();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void articleResponseEqualsHashCodeTest() {

        // given
        ArticleResponse expected = ArticleResponse.fromArticle(article);

        // when
        ArticleResponse result = ArticleResponse.fromArticle(article);

        // then
        assertThat(result)
            .isEqualTo(expected)
            .hasSameHashCodeAs(expected);
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

    @Test
    void articleResponseNestedTest() {
        ArticleResponseNested articleResponseNested = new ArticleResponseNested();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void articleResponseNestedEqualsHashCodeTest() {

        // given
        ArticleResponseNested expected = ArticleResponseNested.fromArticle(article);

        // when
        ArticleResponseNested result = ArticleResponseNested.fromArticle(article);

        // then
        assertThat(result)
            .isEqualTo(expected)
            .hasSameHashCodeAs(expected);
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}