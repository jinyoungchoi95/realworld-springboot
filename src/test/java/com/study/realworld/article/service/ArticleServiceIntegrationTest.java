package com.study.realworld.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.study.realworld.article.domain.Article;
import com.study.realworld.article.domain.ArticleContent;
import com.study.realworld.article.domain.ArticleRepository;
import com.study.realworld.article.domain.Body;
import com.study.realworld.article.domain.Description;
import com.study.realworld.article.domain.SlugTitle;
import com.study.realworld.article.domain.Title;
import com.study.realworld.article.dto.response.ArticleResponse;
import com.study.realworld.article.dto.response.ArticleResponses;
import com.study.realworld.articlefavorite.domain.ArticleFavorite;
import com.study.realworld.articlefavorite.domain.ArticleFavoriteRepository;
import com.study.realworld.tag.domain.Tag;
import com.study.realworld.tag.domain.TagRepository;
import com.study.realworld.user.domain.Bio;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Password;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.Username;
import com.study.realworld.user.service.UserService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class ArticleServiceIntegrationTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ArticleFavoriteRepository articleFavoriteRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private User favoritingUser;
    private ArticleContent articleContent;

    @BeforeEach
    void beforeEachTest() {
        user = User.builder()
            .profile(Username.of("jake"), Bio.of("I work at statefarm"), null)
            .email(Email.of("jake@jake.jake"))
            .password(Password.of("jakejake"))
            .build();

        favoritingUser = User.builder()
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
    }

    @Test
    @DisplayName("?????? ?????????????????? tag??? ?????? ???????????? ????????? ????????? ?????? tag??? ??? ???????????? ?????? ?????? tag??? ??????????????????.")
    void createArticleByExistTagTest() {

        // given
        Tag existTag = Tag.of("tag");
        articleContent.tags()
            .forEach(tag -> tagRepository.save(Tag.of(tag)));
        tagRepository.save(existTag);
        User author = userService.join(user);

        Article article = Article.from(articleContent, author);
        ArticleResponse expected = ArticleResponse.fromArticle(article);
        entityManager.flush();
        entityManager.clear();

        // when
        ArticleResponse result = articleService.createArticle(author.id(), articleContent);
        entityManager.clear();

        // then
        assertAll(
            () -> assertThat(result).isEqualTo(expected),
            () -> assertThat(tagRepository.findAll().size()).isEqualTo(3)
        );
    }

    @Test
    @DisplayName("author??? slug??? ?????? Article??? soft delete ??? ??? ??????.")
    void deleteArticleBySlugSuccessTest() {

        // given
        userService.join(user);
        Article article = Article.from(articleContent, user);
        articleRepository.save(article);
        entityManager.clear();

        articleService.deleteArticleByAuthorAndSlug(user.id(), article.slug());

        // when
        Optional<Article> result = articleRepository.findByAuthorAndArticleContentSlugTitleSlug(user, article.slug());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("????????? tag??? author??? ?????? articles page??? ???????????? ??? ??????.")
    void findAllArticlesTest() {

        // setup
        userService.join(user);
        userService.join(favoritingUser);

        List<Article> articles = new ArrayList<>();
        for (int i=1; i<=10; i++){
            ArticleContent articleContent = ArticleContent.builder()
                .slugTitle(SlugTitle.of(Title.of("How to train your dragon" + i)))
                .description(Description.of("Ever wonder how?" + i))
                .body(Body.of("It takes a Jacobian" + i))
                .tags(Arrays.asList(Tag.of("dragons"), Tag.of("training")))
                .build();
            articles.add(Article.from(articleContent, user));
        }

        for (Article inputArticle : articles) {
            Article article = articleRepository.save(inputArticle);
            articleFavoriteRepository.save(ArticleFavorite.from(favoritingUser, article));
        }
        entityManager.flush();
        entityManager.clear();

        // given
        int offset = 0; // page number
        int limit = 4;  // airtlce counts
        PageRequest pageRequest = PageRequest.of(offset, limit);

        ArticleResponses expected = ArticleResponses.fromArticles(articleRepository.findAll().subList(0, 4));

        // when
        ArticleResponses result = articleService.findArticleResponsesByTagAndAuthorAndFavorited(pageRequest, "dragons", "jake", "jakefriend");

        // then
        assertThat(result).isEqualTo(expected);
    }

}
