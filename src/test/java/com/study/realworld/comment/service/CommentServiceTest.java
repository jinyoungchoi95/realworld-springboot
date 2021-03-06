package com.study.realworld.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.study.realworld.article.domain.Article;
import com.study.realworld.article.domain.ArticleContent;
import com.study.realworld.article.domain.Body;
import com.study.realworld.article.domain.Description;
import com.study.realworld.article.domain.Slug;
import com.study.realworld.article.domain.SlugTitle;
import com.study.realworld.article.domain.Title;
import com.study.realworld.article.service.ArticleService;
import com.study.realworld.comment.domain.Comment;
import com.study.realworld.comment.domain.CommentBody;
import com.study.realworld.comment.domain.CommentRepository;
import com.study.realworld.comment.dto.response.CommentResponse;
import com.study.realworld.comment.dto.response.CommentResponses;
import com.study.realworld.global.exception.BusinessException;
import com.study.realworld.global.exception.ErrorCode;
import com.study.realworld.tag.domain.Tag;
import com.study.realworld.user.domain.Bio;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Follows;
import com.study.realworld.user.domain.Password;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.Username;
import com.study.realworld.user.service.UserService;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private CommentService commentService;

    private User author;
    private Article article;
    private CommentBody commentBody;

    @BeforeEach
    void beforeEach() {
        author = User.builder()
            .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jake@jake.jake"))
            .password(Password.of("jakejake"))
            .follows(Follows.of(new HashSet<>()))
            .build();
        ArticleContent articleContent = ArticleContent.builder()
            .slugTitle(SlugTitle.of(Title.of("How to train your dragon")))
            .description(Description.of("Ever wonder how?"))
            .body(Body.of("It takes a Jacobian"))
            .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
            .build();
        article = Article.from(articleContent, author);
        commentBody = CommentBody.of("It takes a Jacobian");
    }

    @Nested
    @DisplayName("createComment Comment ?????? ?????? ?????????")
    class createCommentTest {

        @Test
        @DisplayName("????????? ????????? ?????? ????????? ??? exception??? ???????????? ??????.")
        void createCommentExceptionByNotFoundUserTest() {

            // setup & given
            Long userId = 1L;
            when(userService.findById(userId)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> commentService.createComment(userId, null, null))
                .withMessageMatching(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("???????????? ?????? ???????????? ??? exception??? ???????????? ??????.")
        void createCommentExceptionByNotFoundArticleTest() {

            // setup & given
            Long userId = 1L;
            Slug slug = Slug.of("how-to-train-your-dragon");
            when(userService.findById(userId)).thenReturn(author);
            when(articleService.findBySlug(slug)).thenThrow(new BusinessException(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> commentService.createComment(userId, slug, null))
                .withMessageMatching(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG.getMessage());
        }

        @Test
        @DisplayName("comment??? ????????? ??? ??????.")
        void createCommentSuccessTest() {

            // setup & given
            Long userId = 1L;
            Slug slug = Slug.of("how-to-train-your-dragon");
            when(userService.findById(userId)).thenReturn(author);
            when(articleService.findBySlug(slug)).thenReturn(article);
            Comment comment = Comment.from(commentBody, author, article);
            when(commentRepository.save(comment)).thenReturn(comment);

            CommentResponse expected = CommentResponse.fromComment(comment);

            // when
            CommentResponse result = commentService.createComment(userId, slug, commentBody);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("getCommentsByArticleSlug comments ?????? ?????????")
    class getCommentsByArticleSlugTest {

        @Test
        @DisplayName("???????????? ?????? ???????????? ??? exception??? ???????????? ??????.")
        void getCommentsByArticleSlugExceptionByNotFoundArticleTest() {

            // setup & given
            Slug slug = Slug.of("how-to-train-your-dragon");
            when(articleService.findBySlug(slug)).thenThrow(new BusinessException(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> commentService.findCommentsByArticleSlug(slug))
                .withMessageMatching(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG.getMessage());
        }

        @Test
        @DisplayName("comments??? ????????? ??? ??????.")
        void getCommentsByArticleSlugTest() {

            // given
            Slug slug = Slug.of("how-to-train-your-dragon");
            when(articleService.findBySlug(slug)).thenReturn(article);
            List<Comment> comments = Arrays.asList(Comment.from(commentBody, author, article),
                Comment.from(commentBody, author, article),
                Comment.from(commentBody, author, article));
            when(commentRepository.findAllByArticle(article)).thenReturn(comments);

            CommentResponses expected = CommentResponses.fromComments(comments);

            // when
            CommentResponses result = commentService.findCommentsByArticleSlug(slug);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("getCommentsByArticleSlug comments ?????? ????????? (parameter += Login user")
    class getCommentsByArticleSlugAndLoginUserTest {

        @Test
        @DisplayName("?????? ????????? ??? exception??? ???????????? ??????.")
        void getCommentsByArticleSlugExceptionByNotFoundUserTest() {

            // setup & given
            Long userId = 1L;
            Slug slug = Slug.of("how-to-train-your-dragon");
            when(userService.findById(userId)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> commentService.findCommentsByArticleSlug(userId, slug))
                .withMessageMatching(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("???????????? ?????? ???????????? ??? exception??? ???????????? ??????.")
        void getCommentsByArticleSlugExceptionByNotFoundArticleTest() {

            // setup & given
            Long userId = 1L;
            when(userService.findById(userId)).thenReturn(author);
            Slug slug = Slug.of("how-to-train-your-dragon");
            when(articleService.findBySlug(slug)).thenThrow(new BusinessException(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> commentService.findCommentsByArticleSlug(userId, slug))
                .withMessageMatching(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG.getMessage());
        }

        @Test
        @DisplayName("comments??? ????????? ??? ??????.")
        void getCommentsByArticleUnfollowingSlugTest() {

            // setup & given
            Long userId = 1L;
            when(userService.findById(userId)).thenReturn(author);
            Slug slug = Slug.of("how-to-train-your-dragon");
            when(articleService.findBySlug(slug)).thenReturn(article);
            List<Comment> comments = Arrays.asList(Comment.from(commentBody, author, article),
                Comment.from(commentBody, author, article),
                Comment.from(commentBody, author, article));
            when(commentRepository.findAllByArticle(article)).thenReturn(comments);

            CommentResponses expected = CommentResponses.fromCommentsAndUser(comments, author);

            // when
            CommentResponses result = commentService.findCommentsByArticleSlug(userId, slug);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("deleteCommentByCommentId Comment ?????? ?????? ?????????")
    class deleteCommentByCommentIdTest {

        @Test
        @DisplayName("????????? ????????? ?????? ????????? ??? exception??? ???????????? ??????.")
        void deleteCommentByCommentIdExceptionByNotFoundUserTest() {

            // setup & given
            Long userId = 1L;
            when(userService.findById(userId)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> commentService.deleteCommentByCommentId(userId, null, null))
                .withMessageMatching(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("???????????? ?????? ???????????? ??? exception??? ???????????? ??????.")
        void deleteCommentByCommentIdExceptionByNotFoundArticleTest() {

            // setup & given
            Long userId = 1L;
            Slug slug = Slug.of("how-to-train-your-dragon");
            when(userService.findById(userId)).thenReturn(author);
            when(articleService.findBySlug(slug)).thenThrow(new BusinessException(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> commentService.deleteCommentByCommentId(userId, slug, null))
                .withMessageMatching(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG.getMessage());
        }

        @Test
        @DisplayName("?????? comment??? ??? exception??? ???????????? ??????.")
        void deleteCommentByCommentIdExceptionByNotFoundComment() {

            // setup & given
            Long userId = 1L;
            Slug slug = Slug.of("how-to-train-your-dragon");
            Long commentId = 2L;
            when(userService.findById(userId)).thenReturn(author);
            when(articleService.findBySlug(slug)).thenReturn(article);
            when(commentRepository.findByIdAndArticle(commentId, article))
                .thenThrow(new BusinessException(ErrorCode.INVALID_COMMENT_NOT_FOUND));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> commentService.deleteCommentByCommentId(userId, slug, commentId))
                .withMessageMatching(ErrorCode.INVALID_COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("comment ???????????? ????????? ?????? ??? exception??? ???????????? ??????.")
        void deleteCommentByCommentIdExceptionByMissMatchUser() {

            // setup & given
            Long userId = 1L;
            User user = User.builder()
                .email(Email.of("email1@email1.com"))
                .build();
            Slug slug = Slug.of("how-to-train-your-dragon");
            Long commentId = 2L;
            Comment comment = Comment.from(commentBody, author, article);
            when(userService.findById(userId)).thenReturn(user);
            when(articleService.findBySlug(slug)).thenReturn(article);
            when(commentRepository.findByIdAndArticle(commentId, article)).thenReturn(Optional.of(comment));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> commentService.deleteCommentByCommentId(userId, slug, commentId))
                .withMessageMatching(ErrorCode.INVALID_COMMENT_AUTHOR_DISMATCH.getMessage());
        }

        @Test
        @DisplayName("comment??? ????????? ??? ??????.")
        void deleteCommentByCommentIdSuccessTest() {

            // setup & given
            Long userId = 1L;
            Slug slug = Slug.of("how-to-train-your-dragon");
            Long commentId = 2L;
            Comment comment = Comment.from(commentBody, author, article);
            when(userService.findById(userId)).thenReturn(author);
            when(articleService.findBySlug(slug)).thenReturn(article);
            when(commentRepository.findByIdAndArticle(commentId, article)).thenReturn(Optional.of(comment));

            OffsetDateTime start = OffsetDateTime.now();
            commentService.deleteCommentByCommentId(userId, slug, commentId);
            OffsetDateTime end = OffsetDateTime.now();

            // when
            OffsetDateTime result = comment.deletedAt();

            // then
            assertThat(result).isAfter(start).isBefore(end);
        }

    }

}