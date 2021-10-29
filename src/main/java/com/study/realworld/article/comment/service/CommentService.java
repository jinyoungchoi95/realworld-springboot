package com.study.realworld.article.comment.service;

import com.study.realworld.article.comment.domain.Comment;
import com.study.realworld.article.comment.domain.CommentBody;
import com.study.realworld.article.comment.domain.CommentRepository;
import com.study.realworld.article.domain.Article;
import com.study.realworld.article.domain.Slug;
import com.study.realworld.article.service.ArticleService;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final ArticleService articleService;

    public CommentService(CommentRepository commentRepository, UserService userService, ArticleService articleService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.articleService = articleService;
    }

    @Transactional
    public Comment createComment(Long userId, Slug slug, CommentBody commentBody) {
        User author = userService.findById(userId);
        Article article = articleService.findBySlug(slug);

        Comment comment = Comment.from(commentBody, author, article);
        return commentRepository.save(comment);
    }

}
