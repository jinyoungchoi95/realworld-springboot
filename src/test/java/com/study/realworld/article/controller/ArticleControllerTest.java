package com.study.realworld.article.controller;

import static com.study.realworld.user.controller.ApiDocumentUtils.getDocumentRequest;
import static com.study.realworld.user.controller.ApiDocumentUtils.getDocumentResponse;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.study.realworld.article.domain.Article;
import com.study.realworld.article.domain.ArticleContent;
import com.study.realworld.article.domain.Body;
import com.study.realworld.article.domain.Description;
import com.study.realworld.article.domain.Slug;
import com.study.realworld.article.domain.SlugTitle;
import com.study.realworld.article.domain.Title;
import com.study.realworld.article.service.ArticleService;
import com.study.realworld.tag.domain.Tag;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Password;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.Username;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
class ArticleControllerTest {

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private ArticleController articleController;

    private MockMvc mockMvc;

    @BeforeEach
    void beforeEach(RestDocumentationContextProvider restDocumentationContextProvider) {
        SecurityContextHolder.clearContext();
        mockMvc = MockMvcBuilders.standaloneSetup(articleController)
            .apply(documentationConfiguration(restDocumentationContextProvider))
            .alwaysExpect(status().isOk())
            .build();
    }

    @Test
    void getArticleTest() throws Exception {

        // setup
        User author = User.Builder()
            .profile(Username.of("username"), null, null)
            .email(Email.of("email@email.com"))
            .password(Password.of("password"))
            .build();
        ArticleContent articleContent = ArticleContent.builder()
            .slugTitle(SlugTitle.of(Title.of("title title title")))
            .description(Description.of("test article description"))
            .body(Body.of("test article body"))
            .tags(Arrays.asList(Tag.of("tag1"), Tag.of("tag2")))
            .build();
        Article article = Article.from(articleContent, author);

        OffsetDateTime now = OffsetDateTime.now();
        ReflectionTestUtils.setField(article, "createdAt", now);
        ReflectionTestUtils.setField(article, "updatedAt", now);

        when(articleService.findBySlug(Slug.of("title-title-title"))).thenReturn(article);

        // given
        final String slug = "title-title-title";
        final String URL = "/api/articles/{slug}";

        // when
        ResultActions resultActions = mockMvc.perform(get(URL, slug)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print());

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.article.slug", is(article.slug().slug())))
            .andExpect(jsonPath("$.article.title", is(article.title().title())))
            .andExpect(jsonPath("$.article.description", is(article.description().description())))
            .andExpect(jsonPath("$.article.body", is(article.body().body())))
            .andExpect(jsonPath("$.article.tagList[0]", is(article.tags().get(0).name())))
            .andExpect(jsonPath("$.article.tagList[1]", is(article.tags().get(1).name())))
            .andExpect(jsonPath("$.article.createdAt",
                is(article.updatedAt().format(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(UTC)))))
            .andExpect(jsonPath("$.article.updatedAt",
                is(article.updatedAt().format(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(UTC)))))
            .andExpect(jsonPath("$.article.author.username", is(article.author().username().value())))
            .andExpect(jsonPath("$.article.author.bio", is(nullValue())))
            .andExpect(jsonPath("$.article.author.image", is(nullValue())))
            .andExpect(jsonPath("$.article.author.following", is(false)))

            .andDo(document("article-find",
                getDocumentRequest(),
                getDocumentResponse(),
                responseFields(
                    fieldWithPath("article.slug").type(JsonFieldType.STRING).description("article's slug"),
                    fieldWithPath("article.title").type(JsonFieldType.STRING).description("article's title"),
                    fieldWithPath("article.description").type(JsonFieldType.STRING).description("article's description"),
                    fieldWithPath("article.body").type(JsonFieldType.STRING).description("article's body"),
                    fieldWithPath("article.tagList").type(JsonFieldType.ARRAY).description("article's tag's list"),
                    fieldWithPath("article.createdAt").type(JsonFieldType.STRING).description("article's create time"),
                    fieldWithPath("article.updatedAt").type(JsonFieldType.STRING).description("article's update time"),

                    fieldWithPath("article.author.username").type(JsonFieldType.STRING)
                        .description("author's username"),
                    fieldWithPath("article.author.bio").type(JsonFieldType.STRING).description("author's bio")
                        .optional(),
                    fieldWithPath("article.author.image").type(JsonFieldType.STRING).description("author's image")
                        .optional(),
                    fieldWithPath("article.author.following").type(JsonFieldType.BOOLEAN)
                        .description("author's following")
                )
            ))
        ;
    }

    @Test
    void createArticleTestTest() throws Exception {

        // setup
        User author = User.Builder()
            .profile(Username.of("username"), null, null)
            .email(Email.of("email@email.com"))
            .password(Password.of("password"))
            .build();
        ArticleContent articleContent = ArticleContent.builder()
            .slugTitle(SlugTitle.of(Title.of("title title title")))
            .description(Description.of("test article description"))
            .body(Body.of("test article body"))
            .tags(Arrays.asList(Tag.of("tag1"), Tag.of("tag2")))
            .build();
        Article article = Article.from(articleContent, author);

        OffsetDateTime now = OffsetDateTime.now();
        ReflectionTestUtils.setField(article, "createdAt", now);
        ReflectionTestUtils.setField(article, "updatedAt", now);

        when(articleService.createArticle(any(), any())).thenReturn(article);

        // given
        final String URL = "/api/articles";
        final String content = "{\n"
            + "  \"article\": {\n"
            + "    \"title\": \"title title title\",\n"
            + "    \"description\": \"test article description\",\n"
            + "    \"body\": \"test article body\",\n"
            + "    \"tagList\": [\"tag1\", \"tag2\"]\n"
            + "  }\n"
            + "}";

        // when
        ResultActions resultActions = mockMvc.perform(post(URL)
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print());

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.article.slug", is(article.slug().slug())))
            .andExpect(jsonPath("$.article.title", is(article.title().title())))
            .andExpect(jsonPath("$.article.description", is(article.description().description())))
            .andExpect(jsonPath("$.article.body", is(article.body().body())))
            .andExpect(jsonPath("$.article.tagList[0]", is(article.tags().get(0).name())))
            .andExpect(jsonPath("$.article.tagList[1]", is(article.tags().get(1).name())))
            .andExpect(jsonPath("$.article.createdAt",
                is(article.updatedAt().format(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(UTC)))))
            .andExpect(jsonPath("$.article.updatedAt",
                is(article.updatedAt().format(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(UTC)))))
            .andExpect(jsonPath("$.article.author.username", is(article.author().username().value())))
            .andExpect(jsonPath("$.article.author.bio", is(nullValue())))
            .andExpect(jsonPath("$.article.author.image", is(nullValue())))
            .andExpect(jsonPath("$.article.author.following", is(false)))

            .andDo(document("article-create",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("article.title").type(JsonFieldType.STRING).description("article title"),
                    fieldWithPath("article.description").type(JsonFieldType.STRING).description("article description"),
                    fieldWithPath("article.body").type(JsonFieldType.STRING).description("article body"),
                    fieldWithPath("article.tagList").type(JsonFieldType.ARRAY).description("article tag's list")
                        .optional()
                ),
                responseFields(
                    fieldWithPath("article.slug").type(JsonFieldType.STRING).description("created article's slug"),
                    fieldWithPath("article.title").type(JsonFieldType.STRING).description("created article's title"),
                    fieldWithPath("article.description").type(JsonFieldType.STRING)
                        .description("created article's description"),
                    fieldWithPath("article.body").type(JsonFieldType.STRING).description("created article's body"),
                    fieldWithPath("article.tagList").type(JsonFieldType.ARRAY)
                        .description("created article's tag's list"),
                    fieldWithPath("article.createdAt").type(JsonFieldType.STRING)
                        .description("created article's create time"),
                    fieldWithPath("article.updatedAt").type(JsonFieldType.STRING)
                        .description("created article's update time"),

                    fieldWithPath("article.author.username").type(JsonFieldType.STRING)
                        .description("author's username"),
                    fieldWithPath("article.author.bio").type(JsonFieldType.STRING).description("author's bio")
                        .optional(),
                    fieldWithPath("article.author.image").type(JsonFieldType.STRING).description("author's image")
                        .optional(),
                    fieldWithPath("article.author.following").type(JsonFieldType.BOOLEAN)
                        .description("author's following")
                )
            ))
        ;
    }

    @Test
    void updateArticleTest() throws Exception {

        // setup
        User author = User.Builder()
            .profile(Username.of("username"), null, null)
            .email(Email.of("email@email.com"))
            .password(Password.of("password"))
            .build();
        ArticleContent articleContent = ArticleContent.builder()
            .slugTitle(SlugTitle.of(Title.of("title title")))
            .description(Description.of("change test article description"))
            .body(Body.of("change test article body"))
            .tags(Arrays.asList(Tag.of("tag1"), Tag.of("tag2")))
            .build();
        Article article = Article.from(articleContent, author);

        OffsetDateTime now = OffsetDateTime.now();
        ReflectionTestUtils.setField(article, "createdAt", now);
        ReflectionTestUtils.setField(article, "updatedAt", now);

        when(articleService.updateArticle(any(), eq(Slug.of("title-title-title")), any())).thenReturn(article);

        // given
        final String slug = "title-title-title";
        final String URL = "/api/articles/{slug}";
        final String content = "{\n"
            + "  \"article\": {\n"
            + "    \"title\": \"title title\",\n"
            + "    \"description\": \"change test article description\",\n"
            + "    \"body\": \"change test article body\"\n"
            + "  }\n"
            + "}";

        // when
        ResultActions resultActions = mockMvc.perform(put(URL, slug)
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print());

        // then
        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

            .andExpect(jsonPath("$.article.slug", is(article.slug().slug())))
            .andExpect(jsonPath("$.article.title", is(article.title().title())))
            .andExpect(jsonPath("$.article.description", is(article.description().description())))
            .andExpect(jsonPath("$.article.body", is(article.body().body())))
            .andExpect(jsonPath("$.article.tagList[0]", is(article.tags().get(0).name())))
            .andExpect(jsonPath("$.article.tagList[1]", is(article.tags().get(1).name())))
            .andExpect(jsonPath("$.article.createdAt",
                is(article.updatedAt().format(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(UTC)))))
            .andExpect(jsonPath("$.article.updatedAt",
                is(article.updatedAt().format(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(UTC)))))
            .andExpect(jsonPath("$.article.author.username", is(article.author().username().value())))
            .andExpect(jsonPath("$.article.author.bio", is(nullValue())))
            .andExpect(jsonPath("$.article.author.image", is(nullValue())))
            .andExpect(jsonPath("$.article.author.following", is(false)))

            .andDo(document("article-create",
                getDocumentRequest(),
                getDocumentResponse(),
                requestFields(
                    fieldWithPath("article.title").type(JsonFieldType.STRING).description("article title").optional(),
                    fieldWithPath("article.description").type(JsonFieldType.STRING).description("article description")
                        .optional(),
                    fieldWithPath("article.body").type(JsonFieldType.STRING).description("article body")
                        .optional(),
                    fieldWithPath("article.tagList").type(JsonFieldType.ARRAY).description("article tag's list")
                        .optional()
                ),
                responseFields(
                    fieldWithPath("article.slug").type(JsonFieldType.STRING).description("changed article's slug"),
                    fieldWithPath("article.title").type(JsonFieldType.STRING).description("changed article's title"),
                    fieldWithPath("article.description").type(JsonFieldType.STRING)
                        .description("created article's description"),
                    fieldWithPath("article.body").type(JsonFieldType.STRING).description("changed article's body"),
                    fieldWithPath("article.tagList").type(JsonFieldType.ARRAY)
                        .description("article's tag's list"),
                    fieldWithPath("article.createdAt").type(JsonFieldType.STRING)
                        .description("article's create time"),
                    fieldWithPath("article.updatedAt").type(JsonFieldType.STRING)
                        .description("article's update time"),

                    fieldWithPath("article.author.username").type(JsonFieldType.STRING)
                        .description("author's username"),
                    fieldWithPath("article.author.bio").type(JsonFieldType.STRING).description("author's bio")
                        .optional(),
                    fieldWithPath("article.author.image").type(JsonFieldType.STRING).description("author's image")
                        .optional(),
                    fieldWithPath("article.author.following").type(JsonFieldType.BOOLEAN)
                        .description("author's following")
                )
            ))
        ;
    }

    @Test
    void deleteArticleTest() throws Exception {

        // given
        String slug = "title-title-title";
        final String URL = "/api/articles/{slug}";

        // when
        ResultActions resultActions = mockMvc.perform(delete(URL, slug)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(print());

        // then
        resultActions
            .andExpect(status().isOk())

            .andDo(document("article-delete",
                getDocumentRequest(),
                getDocumentResponse()
            ))
        ;
    }

}