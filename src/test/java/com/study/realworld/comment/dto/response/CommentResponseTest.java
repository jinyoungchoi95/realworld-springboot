package com.study.realworld.comment.dto.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.study.realworld.article.domain.Article;
import com.study.realworld.article.domain.ArticleContent;
import com.study.realworld.article.domain.Body;
import com.study.realworld.article.domain.Description;
import com.study.realworld.article.domain.SlugTitle;
import com.study.realworld.article.domain.Title;
import com.study.realworld.comment.domain.Comment;
import com.study.realworld.comment.domain.CommentBody;
import com.study.realworld.comment.dto.response.CommentResponse.CommentResponseNested;
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

class CommentResponseTest {

    private User user;
    private Article article;
    private Comment comment;

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

        CommentBody commentBody = CommentBody.of("It takes a Jacobian");

        comment = Comment.from(commentBody, user, article);
    }

    @Test
    void commentResponseTest() {
        CommentResponse commentResponse = new CommentResponse();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void commentResponseEqualsHashCodeTest() {

        // given
        CommentResponse expected = CommentResponse.fromComment(comment);

        // when
        CommentResponse result = CommentResponse.fromComment(comment);

        // then
        assertThat(result)
            .isEqualTo(expected)
            .hasSameHashCodeAs(expected);
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

    @Test
    void commentResponseNestedTest() {
        CommentResponseNested commentResponseNested = new CommentResponseNested();
    }

    @Test
    @DisplayName("equals hashCode 테스트")
    void commentResponseNestedEqualsHashCodeTest() {

        // given
        CommentResponseNested expected = CommentResponseNested.fromComment(comment);

        // when
        CommentResponseNested result = CommentResponseNested.fromComment(comment);

        // then
        assertThat(result)
            .isEqualTo(expected)
            .hasSameHashCodeAs(expected);
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}