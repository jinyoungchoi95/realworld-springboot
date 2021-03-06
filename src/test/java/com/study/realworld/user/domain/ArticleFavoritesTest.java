package com.study.realworld.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.study.realworld.article.domain.Article;
import com.study.realworld.article.domain.ArticleContent;
import com.study.realworld.article.domain.Body;
import com.study.realworld.article.domain.Description;
import com.study.realworld.article.domain.SlugTitle;
import com.study.realworld.article.domain.Title;
import com.study.realworld.articlefavorite.domain.ArticleFavorite;
import com.study.realworld.global.exception.BusinessException;
import com.study.realworld.global.exception.ErrorCode;
import com.study.realworld.tag.domain.Tag;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ArticleFavoritesTest {

    private User user;
    private User author;
    private ArticleContent articleContent;
    private Article article;

    @BeforeEach
    void beforeEach() {
        user = User.builder()
            .id(1L)
            .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jake@jake.jake"))
            .password(Password.of("jakejake"))
            .build();

        author = User.builder()
            .id(2L)
            .profile(Username.of("jakefriend"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jakefriend@jake.jake"))
            .password(Password.of("jakejake"))
            .build();

        articleContent = ArticleContent.builder()
            .slugTitle(SlugTitle.of(Title.of("How to train your dragon")))
            .description(Description.of("Ever wonder how?"))
            .body(Body.of("It takes a Jacobian"))
            .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
            .build();

        article = Article.from(articleContent, author);
    }

    @Test
    void articleFavoritesTest() {
        ArticleFavorites articleFavorite = new ArticleFavorites();
    }

    @Nested
    @DisplayName("checkCanFavorite ????????? ???????????? ?????????")
    class checkCanFavoriteTest {

        @Test
        @DisplayName("?????? ???????????? ????????? ??????????????? ?????? exception??? ???????????? ??????.")
        void checkCanFavoriteByExistFavoriteExceptionTest() {

            // given
            Set<ArticleFavorite> favoriteSet = new HashSet<>();
            ArticleFavorite favorite = ArticleFavorite.from(user, article);
            favoriteSet.add(favorite);
            ArticleFavorites favorites = ArticleFavorites.of(favoriteSet);

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> favorites.checkCanFavorite(favorite))
                .withMessageMatching(ErrorCode.INVALID_FAVORITE_ARTICLE.getMessage());
        }

        @Test
        @DisplayName("??????????????? ????????? ?????? ????????? ???????????? ??????.")
        void checkCanFavoriteSuccessTest() {

            // given
            Set<ArticleFavorite> favoriteSet = new HashSet<>();
            ArticleFavorite favorite = ArticleFavorite.from(user, article);
            ArticleFavorites favorites = ArticleFavorites.of(favoriteSet);

            ArticleFavorite expected = ArticleFavorite.from(user, article);

            // when
            ArticleFavorite result = favorites.checkCanFavorite(favorite);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("checkCanUnfavorite ????????? ?????????????????? ?????????")
    class checkCanUnfavoriteTest {

        @Test
        @DisplayName("??????????????? ????????? ????????? ???????????? ?????? exception??? ???????????? ??????.")
        void checkCanUnfavoriteByNotExistFavoriteExceptionTest() {

            // given
            Set<ArticleFavorite> favoriteSet = new HashSet<>();
            ArticleFavorite favorite = ArticleFavorite.from(user, article);
            ArticleFavorites favorites = ArticleFavorites.of(favoriteSet);

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> favorites.checkCanUnfavorite(favorite))
                .withMessageMatching(ErrorCode.INVALID_UNFAVORITE_ARTICLE.getMessage());
        }

        @Test
        @DisplayName("??????????????? ????????? ?????? ????????? ???????????? ??????.")
        void checkCanUnfavoriteSuccessTest() {

            // given
            Set<ArticleFavorite> favoriteSet = new HashSet<>();
            ArticleFavorite favorite = ArticleFavorite.from(user, article);
            favoriteSet.add(favorite);
            ArticleFavorites favorites = ArticleFavorites.of(favoriteSet);

            ArticleFavorite expected = ArticleFavorite.from(user, article);

            // when
            ArticleFavorite result = favorites.checkCanUnfavorite(favorite);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("isFavoriteArticle ????????? ???????????? ?????? ?????????")
    class isFavoriteArticleTest {

        private ArticleFavorite articleFavorite = ArticleFavorite.from(user, article);

        @Test
        @DisplayName("true")
        void trueTest() {

            // given
            Set<ArticleFavorite> articleFavoriteSet = new HashSet<>();
            articleFavoriteSet.add(articleFavorite);
            ArticleFavorites favorites = ArticleFavorites.of(articleFavoriteSet);

            // when
            boolean result = favorites.isFavoriteArticle(articleFavorite);

            // then
            assertTrue(result);
        }

        @Test
        @DisplayName("false")
        void falseTest() {

            // given
            ArticleFavorites favorites = ArticleFavorites.of(new HashSet<>());

            // when
            boolean result = favorites.isFavoriteArticle(articleFavorite);

            // then
            assertFalse(result);
        }

    }

    @Test
    @DisplayName("equals hashCode ?????????")
    void favoriteArticlesEqualsHashCodeTest() {

        // given
        Set<ArticleFavorite> articleFavoriteSet = new HashSet<>();
        ArticleFavorite favorite = ArticleFavorite.from(user, article);
        articleFavoriteSet.add(favorite);

        // when
        ArticleFavorites result = ArticleFavorites.of(articleFavoriteSet);

        // then
        assertThat(result)
            .isEqualTo(ArticleFavorites.of(articleFavoriteSet))
            .hasSameHashCodeAs(ArticleFavorites.of(articleFavoriteSet));
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}