package com.study.realworld.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.study.realworld.article.domain.Article;
import com.study.realworld.article.domain.ArticleContent;
import com.study.realworld.article.domain.Body;
import com.study.realworld.article.domain.Description;
import com.study.realworld.article.domain.SlugTitle;
import com.study.realworld.article.domain.Title;
import com.study.realworld.articlefavorite.domain.ArticleFavorite;
import com.study.realworld.follow.domain.Follow;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;
    private User followee;

    private Article article;

    @BeforeEach
    void beforeEachTest() {
        user = User.builder()
            .id(1L)
            .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jake@jake.jake"))
            .password(Password.of("jakejake"))
            .follows(new Follows())
            .articleFavorites(new ArticleFavorites())
            .build();

        followee = User.builder()
            .id(2L)
            .profile(Username.of("jakefriend"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jakefriend@jake.jake"))
            .password(Password.of("jakejake"))
            .follows(new Follows())
            .build();

        ArticleContent articleContent = ArticleContent.builder()
            .slugTitle(SlugTitle.of(Title.of("How to train your dragon")))
            .description(Description.of("Ever wonder how?"))
            .body(Body.of("It takes a Jacobian"))
            .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
            .build();

        article = Article.from(articleContent, followee);
    }

    @Test
    void userTest() {
        User user = new User();
    }

    @Test
    @DisplayName("encodePassword ???????????? ??????????????? ??? User ????????? ??????????????? ?????????????????? ??????.")
    void userEncodePasswordTest() {

        // setup & given
        User user = User.builder().password(Password.of("password")).build();
        when(passwordEncoder.encode(user.password().password()))
            .thenReturn("encoded_password");

        // when
        user.encodePassword(passwordEncoder);

        // then
        assertThat(user.password().password()).isEqualTo("encoded_password");
    }

    @Test
    @DisplayName("????????? ??????????????? ???????????? ??????????????? ????????? Exception??? ???????????? ????????? ??????.")
    void passwordMatchTest() {

        // setup
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);

        // given
        User user = User.builder().password(Password.of("encoded_password")).build();
        Password password = Password.of("password");

        // when & then
        assertDoesNotThrow(() -> user.login(password, passwordEncoder));
    }

    @Test
    @DisplayName("????????? ??????????????? ???????????? ??????????????? ????????? Excpetion??? ???????????? ??????.")
    void passwordDismatchTest() {

        // setup
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(false);

        // given
        User user = User.builder().password(Password.of("encoded_password")).build();
        Password password = Password.of("password");

        // when & then
        assertThatExceptionOfType(BusinessException.class)
            .isThrownBy(() -> user.login(password, passwordEncoder))
            .withMessageMatching(ErrorCode.PASSWORD_DISMATCH.getMessage());
    }

    @Nested
    @DisplayName("followUser user ????????? ??????")
    class followUserTest {

        @Test
        @DisplayName("?????? ???????????? ????????? ??????????????? ?????? exception??? ???????????? ??????.")
        void followUserExceptionByExistFollowTest() {

            // given
            Set<Follow> followSet = new HashSet<>();
            Follow follow = Follow.builder()
                .follower(user)
                .followee(followee)
                .build();
            followSet.add(follow);
            user = User.builder()
                .id(1L)
                .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
                .email(Email.of("jake@jake.jake"))
                .password(Password.of("jakejake"))
                .follows(Follows.of(followSet))
                .build();

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> user.followUser(followee))
                .withMessageMatching(ErrorCode.INVALID_FOLLOW.getMessage());
        }

        @Test
        @DisplayName("??????????????? ????????? ???????????? ??? ??????.")
        void followUserSuccessTest() {

            // when
            boolean result = user.followUser(followee);

            // then
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("unfollowUser user ???????????? ??????")
    class unfollowUserTest {

        @Test
        @DisplayName("????????? ?????? ????????? ?????????????????? ?????? exception??? ???????????? ??????.")
        void unfollowUserExceptionByNoExistFollowTest() {

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> user.unfollowUser(followee))
                .withMessageMatching(ErrorCode.INVALID_UNFOLLOW.getMessage());
        }

        @Test
        @DisplayName("??????????????? ????????? ??????????????? ??? ??????.")
        void unfollowUserSuccessTest() {

            // given
            Set<Follow> followSet = new HashSet<>();
            Follow follow = Follow.builder()
                .follower(user)
                .followee(followee)
                .build();
            followSet.add(follow);
            user = User.builder()
                .id(1L)
                .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
                .email(Email.of("jake@jake.jake"))
                .password(Password.of("jakejake"))
                .follows(Follows.of(followSet))
                .build();

            // when
            boolean result = user.unfollowUser(followee);

            // then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("createFavoriteForFavoriting Favorite ??????(???????????????)")
    class createFavoriteForFavoritingTest {

        @Test
        @DisplayName("???????????? ???????????? ??????????????? ?????? exception??? ???????????? ??????.")
        void createFavoriteForFavoritingExceptionByExistFavoriteTest() {

            // given
            Set<ArticleFavorite> favoriteSet = new HashSet<>();
            ArticleFavorite favorite = ArticleFavorite.from(user, article);
            favoriteSet.add(favorite);
            user = User.builder()
                .id(1L)
                .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
                .email(Email.of("jake@jake.jake"))
                .password(Password.of("jakejake"))
                .articleFavorites(ArticleFavorites.of(favoriteSet))
                .build();

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> user.createFavoriteForFavoriting(article))
                .withMessageMatching(ErrorCode.INVALID_FAVORITE_ARTICLE.getMessage());
        }

        @Test
        @DisplayName("??????????????? ????????? ???????????? ????????? ??? ??????.")
        void favoriteArticleSuccessTest() {

            // given
            Set<ArticleFavorite> favoriteSet = new HashSet<>();
            ArticleFavorite favorite = ArticleFavorite.from(user, article);
            user = User.builder()
                .id(1L)
                .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
                .email(Email.of("jake@jake.jake"))
                .password(Password.of("jakejake"))
                .articleFavorites(ArticleFavorites.of(favoriteSet))
                .build();

            ArticleFavorite expected = ArticleFavorite.from(user, article);

            // when
            ArticleFavorite result = user.createFavoriteForFavoriting(article);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Nested
    @DisplayName("createFavoriteForUnfavoriting Favorite ??????(?????????????????????)")
    class createFavoriteForUnfavoritingTest {

        @Test
        @DisplayName("????????? ?????? ???????????? ????????? ???????????? ?????? exception??? ???????????? ??????.")
        void createFavoriteForUnfavoritingExceptionByNotExistFavoriteTest() {

            // when & then
            assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> user.createFavoriteForUnfavoriting(article))
                .withMessageMatching(ErrorCode.INVALID_UNFAVORITE_ARTICLE.getMessage());
        }

        @Test
        @DisplayName("??????????????? ????????? ????????? ???????????? ????????? ??? ??????.")
        void reateFavoriteForUnfavoritingSuccessTest() {

            // given
            Set<ArticleFavorite> favoriteSet = new HashSet<>();
            ArticleFavorite favorite = ArticleFavorite.from(user, article);
            favoriteSet.add(favorite);
            user = User.builder()
                .id(1L)
                .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
                .email(Email.of("jake@jake.jake"))
                .password(Password.of("jakejake"))
                .articleFavorites(ArticleFavorites.of(favoriteSet))
                .build();

            ArticleFavorite expected = ArticleFavorite.from(user, article);

            // when
            ArticleFavorite result = user.createFavoriteForUnfavoriting(article);

            // then
            assertThat(result).isEqualTo(expected);
        }

    }

    @Test
    @DisplayName("????????? ????????? ????????? ????????? ??? ??????.")
    void isFavoriteArticleTest() {

        // given
        Set<ArticleFavorite> favoriteSet = new HashSet<>();
        ArticleFavorite favorite = ArticleFavorite.from(user, article);
        favoriteSet.add(favorite);
        user = User.builder()
            .id(1L)
            .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jake@jake.jake"))
            .password(Password.of("jakejake"))
            .articleFavorites(ArticleFavorites.of(favoriteSet))
            .build();

        // when
        boolean result = user.isFavoriteArticle(article);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("equals hashCode ?????????")
    void userEqualsHashCodeTest() {

        // given
        Email email = Email.of("email@email.com");
        User expected = User.builder().email(email).build();

        // when
        User result = User.builder().email(email).build();

        // when & then
        assertThat(result)
            .isEqualTo(expected)
            .hasSameHashCodeAs(expected);
        assertEquals(result, result);
        assertNotEquals(result, null);
    }

}
