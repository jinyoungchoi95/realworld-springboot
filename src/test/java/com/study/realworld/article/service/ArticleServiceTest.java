package com.study.realworld.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.study.realworld.article.domain.Article;
import com.study.realworld.article.domain.ArticleContent;
import com.study.realworld.article.domain.ArticleRepository;
import com.study.realworld.article.domain.Body;
import com.study.realworld.article.domain.Description;
import com.study.realworld.article.domain.Slug;
import com.study.realworld.article.domain.SlugTitle;
import com.study.realworld.article.domain.Title;
import com.study.realworld.article.dto.response.ArticleResponse;
import com.study.realworld.article.dto.response.ArticleResponses;
import com.study.realworld.article.service.model.ArticleUpdateModel;
import com.study.realworld.global.exception.BusinessException;
import com.study.realworld.global.exception.ErrorCode;
import com.study.realworld.tag.domain.Tag;
import com.study.realworld.tag.service.TagService;
import com.study.realworld.user.domain.Bio;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Password;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.Username;
import com.study.realworld.user.service.UserService;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserService userService;

    @Mock
    private TagService tagService;

    @InjectMocks
    private ArticleService articleService;

    private User user;
    private ArticleContent articleContent;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
            .id(1L)
            .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jake@jake.jake"))
            .password(Password.of("jakejake"))
            .build();
        articleContent = ArticleContent.builder()
            .slugTitle(SlugTitle.of(Title.of("How to train your dragon")))
            .description(Description.of("Ever wonder how?"))
            .body(Body.of("It takes a Jacobian"))
            .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
            .build();
    }

    @Nested
    @DisplayName("findBySlug Article ?????? ?????? ?????????")
    class findBySlugTest {

        @Test
        @DisplayName("slug??? ????????? article??? ????????? ??? ??????.")
        void findBySlugSuccessTest() {

            // given
            Article expected = Article.from(articleContent, user);
            when(articleRepository.findByArticleContentSlugTitleSlug(expected.slug()))
                .thenReturn(Optional.of(expected));

            // when
            Article result = articleService.findBySlug(expected.slug());

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("?????? article??? ?????????????????? ??? exception??? ???????????? ??????.")
        void findBySlugExceptionTest() {

            // given
            Article article = Article.from(articleContent, user);
            when(articleRepository.findByArticleContentSlugTitleSlug(article.slug()))
                .thenReturn(Optional.empty());

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.findBySlug(article.slug()))
                .withMessageMatching(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG.getMessage());
        }

    }

    @Nested
    @DisplayName("findArticleResponseBySlug ArticleResponse ?????? ?????? ?????????")
    class findArticleResponseBySlugTest {

        @Test
        @DisplayName("slug??? ????????? article??? ????????? ??? ??????.")
        void findArticleResponseBySlugSuccessTest() {

            // given
            Article article = Article.from(articleContent, user);
            Slug slug = article.slug();
            when(articleRepository.findByArticleContentSlugTitleSlug(slug)).thenReturn(Optional.of(article));
            ArticleResponse expected = ArticleResponse.fromArticle(article);

            // when
            ArticleResponse result = articleService.findArticleResponseBySlug(slug);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("?????? article??? ?????????????????? ??? exception??? ???????????? ??????.")
        void findArticleResponseBySlugExceptionTest() {

            // given
            Article article = Article.from(articleContent, user);
            when(articleRepository.findByArticleContentSlugTitleSlug(article.slug())).thenReturn(Optional.empty());

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.findArticleResponseBySlug(article.slug()))
                .withMessageMatching(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG.getMessage());
        }

    }

    @Nested
    @DisplayName("findArticleResponseBySlug Article(parameter + userId) ????????? ?????? ?????? ?????? ?????????")
    class findArticleResponseBySlugLoginUserTest {

        @Test
        @DisplayName("?????? user??? ?????????????????? ??? exception??? ???????????? ??????.")
        void findArticleResponseBySlugExceptionByNotFoundUserTest() {

            // given
            Long userId = 1L;
            Slug slug = articleContent.slug();
            when(userService.findById(userId)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.findArticleResponseBySlug(userId, slug))
                .withMessageMatching(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("?????? article??? ?????????????????? ??? exception??? ???????????? ??????.")
        void findArticleResponseBySlugExceptionTest() {

            // given
            Long userId = 1L;
            Slug slug = articleContent.slug();
            when(userService.findById(userId)).thenReturn(user);
            when(articleRepository.findByArticleContentSlugTitleSlug(slug)).thenReturn(Optional.empty());

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.findArticleResponseBySlug(userId, slug))
                .withMessageMatching(ErrorCode.ARTICLE_NOT_FOUND_BY_SLUG.getMessage());
        }

        @Test
        @DisplayName("login??? ????????? slug??? ????????? article??? ????????? ??? ??????.")
        void findArticleResponseBySlugSuccessTest() {

            // given
            Long userId = 1L;
            Article article = Article.from(articleContent, user);
            Slug slug = article.slug();
            when(userService.findById(userId)).thenReturn(user);
            when(articleRepository.findByArticleContentSlugTitleSlug(slug)).thenReturn(Optional.of(article));

            ArticleResponse expected = ArticleResponse.fromArticle(article);

            // when
            ArticleResponse result = articleService.findArticleResponseBySlug(userId, slug);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Test
    @DisplayName("????????? offset, limit??? ?????? Page ???????????? ????????? ??? ??????.")
    void findArticleResponsesByTagAndAuthorTest() {

        // setup & given
        int offset = 0;
        int limit = 4;
        List<Article> articles = Arrays.asList(Article.from(articleContent, user),
            Article.from(articleContent, user),
            Article.from(articleContent, user),
            Article.from(articleContent, user),
            Article.from(articleContent, user)
        );
        PageRequest pageRequest = PageRequest.of(offset, limit);
        when(articleRepository.findPageByTagAndAuthorAndFavorited(pageRequest, null, null, null))
            .thenReturn(new PageImpl<>(articles.subList(0, 4), pageRequest, articles.size()));

        ArticleResponses expected = ArticleResponses.fromArticles(articles.subList(0, 4));

        // when
        ArticleResponses result = articleService.findArticleResponsesByTagAndAuthorAndFavorited(pageRequest, null, null, null);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Nested
    @DisplayName("findArticleResponsesByTagAndAuthor ????????? ?????? articles ?????? ?????????")
    class findArticleResponsesByTagAndAuthorLoginUserTest {

        @Test
        @DisplayName("?????? ????????? id??? ????????? ????????? ????????? ??? Exception??? ??????????????? ??????.")
        void findArticleResponsesByTagAndAuthorExceptionByNotFoundUserTest() {

            // setup & given
            Long userId = 1L;
            when(userService.findById(userId)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.findArticleResponsesByTagAndAuthorAndFavorited(userId, null, null, null, null))
                .withMessageMatching(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("????????? offset, limit??? ?????? Page ???????????? ????????? ??? ??????.")
        void findArticleResponsesByTagAndAuthorSuccessTest() {

            // setup & given
            Long userId = 1L;
            when(userService.findById(userId)).thenReturn(user);
            int offset = 0;
            int limit = 4;
            List<Article> articles = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                articleContent = ArticleContent.builder()
                    .slugTitle(SlugTitle.of(Title.of("How to train your dragon" + i)))
                    .description(Description.of("Ever wonder how?" + i))
                    .body(Body.of("It takes a Jacobian" + i))
                    .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
                    .build();
                articles.add(Article.from(articleContent, user));
            }

            PageRequest pageRequest = PageRequest.of(offset, limit);
            when(articleRepository.findPageByTagAndAuthorAndFavorited(pageRequest, null, null, null))
                .thenReturn(new PageImpl<>(articles.subList(0, 4), pageRequest, articles.size()));

            ArticleResponses expected = ArticleResponses.fromArticlesAndUser(articles.subList(0, 4), user);

            // when
            ArticleResponses result = articleService.findArticleResponsesByTagAndAuthorAndFavorited(userId, pageRequest, null, null, null);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("findFeedArticleResponse ??????????????? ?????? article ?????? ?????????")
    class findFeedArticleResponsesTest {

        @Test
        @DisplayName("?????? ????????? id??? ????????? ????????? ????????? ??? Exception??? ??????????????? ??????.")
        void findFeedArticleResponsesExceptionByNotFoundUserTest() {

            // setup & given
            Long userId = 1L;
            when(userService.findById(userId)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.findFeedArticleResponses(userId, null))
                .withMessageMatching(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("following??? ????????? article feed??? ????????? ??? ??????.")
        void findFeedArticleResponsesSuccessTest() {

            // given
            Long userId = 1L;
            when(userService.findById(userId)).thenReturn(user);
            List<Article> articles = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                articleContent = ArticleContent.builder()
                    .slugTitle(SlugTitle.of(Title.of("How to train your dragon" + i)))
                    .description(Description.of("Ever wonder how?" + i))
                    .body(Body.of("It takes a Jacobian" + i))
                    .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
                    .build();
                articles.add(Article.from(articleContent, user));
            }
            when(articleRepository.findByAuthorIn(any(), eq(user.followees())))
                .thenReturn(new PageImpl<>(articles.subList(0, 4)));

            ArticleResponses expected = ArticleResponses.fromArticles(articles.subList(0, 4));

            // when
            ArticleResponses result = articleService.findFeedArticleResponses(userId, null);

            // then
            assertThat(result).isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("user id??? ????????? article??? ????????? ??? ??????.")
    void createArticleTest() {

        // setup & given
        Long userId = 1L;
        when(userService.findById(userId)).thenReturn(user);
        when(tagService.refreshTagByExistedTagName(articleContent.tags()))
            .thenReturn(Arrays.asList(Tag.of("dragons"), Tag.of("training")));
        Article article = Article.from(articleContent, user);
        when(articleRepository.save(article)).thenReturn(article);

        ArticleResponse expected = ArticleResponse.fromArticle(article);

        // when
        ArticleResponse result = articleService.createArticle(userId, articleContent);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Nested
    @DisplayName("updateArticle Article ?????? ?????????")
    class updateArticle {

        private ArticleUpdateModel articleUpdateModel;

        @Test
        @DisplayName("????????? ???????????? ????????? exception??? ???????????? ??????.")
        void userNotFoundExceptionTest() {

            // given
            Article article = Article.from(articleContent, user);
            Long userId = 2L;
            Slug slug = article.slug();
            when(userService.findById(userId)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.updateArticle(userId, slug, articleUpdateModel))
                .withMessageMatching(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("Article??? ???????????? ????????? exception??? ???????????? ??????.")
        void updateArticleBySlugExceptionTest() {

            // given
            Article article = Article.from(articleContent, user);
            Long userId = 1L;
            Slug slug = article.slug();
            when(userService.findById(userId)).thenReturn(user);
            when(articleRepository.findByAuthorAndArticleContentSlugTitleSlug(user, slug)).thenReturn(Optional.empty());

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.updateArticle(userId, slug, articleUpdateModel))
                .withMessageMatching(ErrorCode.ARTICLE_NOT_FOUND_BY_AUTHOR_AND_SLUG.getMessage());
        }

        @Test
        @DisplayName("title??? ????????? ??? ??????.")
        void changeTitleTest() {

            // given
            Article article = Article.from(articleContent, user);
            Long userId = 1L;
            when(userService.findById(userId)).thenReturn(user);
            when(articleRepository.findByAuthorAndArticleContentSlugTitleSlug(user, article.slug()))
                .thenReturn(Optional.of(article));
            articleUpdateModel = new ArticleUpdateModel(Title.of("title title"), null, null);

            ArticleContent expectedContent = ArticleContent.builder()
                .slugTitle(SlugTitle.of(Title.of("title title")))
                .description(Description.of("Ever wonder how?"))
                .body(Body.of("It takes a Jacobian"))
                .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
                .build();
            ArticleResponse expected = ArticleResponse.fromArticle(Article.from(expectedContent, user));

            // when
            ArticleResponse result = articleService.updateArticle(userId, article.slug(), articleUpdateModel);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("description??? ????????? ??? ??????.")
        void changeDescriptionTest() {

            // given
            Article article = Article.from(articleContent, user);
            Long userId = 1L;
            when(userService.findById(userId)).thenReturn(user);
            when(articleRepository.findByAuthorAndArticleContentSlugTitleSlug(user, article.slug()))
                .thenReturn(Optional.of(article));
            Description changeDescription = Description.of("new description");
            articleUpdateModel = new ArticleUpdateModel(null, changeDescription, null);

            ArticleContent expectedContent = ArticleContent.builder()
                .slugTitle(SlugTitle.of(Title.of("How to train your dragon")))
                .description(Description.of("new description"))
                .body(Body.of("It takes a Jacobian"))
                .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
                .build();
            ArticleResponse expected = ArticleResponse.fromArticle(Article.from(expectedContent, user));

            // when
            ArticleResponse result = articleService.updateArticle(userId, article.slug(), articleUpdateModel);

            // then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("body??? ????????? ??? ??????.")
        void changeBodyTest() {

            // given
            Article article = Article.from(articleContent, user);
            Long userId = 1L;
            when(userService.findById(userId)).thenReturn(user);
            when(articleRepository.findByAuthorAndArticleContentSlugTitleSlug(user, article.slug()))
                .thenReturn(Optional.of(article));
            Body changeBody = Body.of("new body");
            articleUpdateModel = new ArticleUpdateModel(null, null, changeBody);

            ArticleContent expectedContent = ArticleContent.builder()
                .slugTitle(SlugTitle.of(Title.of("How to train your dragon")))
                .description(Description.of("Ever wonder how?"))
                .body(Body.of("new body"))
                .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
                .build();
            ArticleResponse expected = ArticleResponse.fromArticle(Article.from(expectedContent, user));

            // when
            ArticleResponse result = articleService.updateArticle(userId, article.slug(), articleUpdateModel);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("deleteArticleByAuthorAndSlug Article ?????? ?????????")
    class deleteArticleByAuthorAndSlug {

        @Test
        @DisplayName("????????? ???????????? ????????? exception??? ???????????? ??????.")
        void userNotFoundExceptionTest() {

            // given
            Article article = Article.from(articleContent, user);
            Long userId = 2L;
            Slug slug = article.slug();
            when(userService.findById(userId)).thenThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.deleteArticleByAuthorAndSlug(userId, slug))
                .withMessageMatching(ErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("Article??? ???????????? ????????? exception??? ???????????? ??????.")
        void deleteArticleBySlugExceptionTest() {

            // given
            Article article = Article.from(articleContent, user);
            Long userId = 1L;
            Slug slug = article.slug();
            when(userService.findById(userId)).thenReturn(user);
            when(articleRepository.findByAuthorAndArticleContentSlugTitleSlug(user, slug)).thenReturn(Optional.empty());

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> articleService.deleteArticleByAuthorAndSlug(userId, slug))
                .withMessageMatching(ErrorCode.ARTICLE_NOT_FOUND_BY_AUTHOR_AND_SLUG.getMessage());
        }

        @Test
        @DisplayName("Article??? ????????? ??? ??????.")
        void deleteArticleBySlugSuccessTest() {

            // given
            Article article = Article.from(articleContent, user);
            Long userId = 1L;
            Slug slug = article.slug();
            when(userService.findById(userId)).thenReturn(user);
            when(articleRepository.findByAuthorAndArticleContentSlugTitleSlug(user, slug))
                .thenReturn(Optional.of(article));
            OffsetDateTime start = OffsetDateTime.now();
            articleService.deleteArticleByAuthorAndSlug(userId, slug);
            OffsetDateTime end = OffsetDateTime.now();

            // when
            OffsetDateTime result = article.deletedAt();

            // then
            assertThat(result).isAfter(start).isBefore(end);
        }
    }

}