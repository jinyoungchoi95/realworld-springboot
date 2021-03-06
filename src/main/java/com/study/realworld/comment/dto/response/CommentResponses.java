package com.study.realworld.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.study.realworld.comment.domain.Comment;
import com.study.realworld.comment.dto.response.CommentResponse.CommentResponseNested;
import com.study.realworld.user.domain.User;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommentResponses {

    @JsonProperty("comments")
    private List<CommentResponseNested> commentResponseNesteds;

    CommentResponses() {
    }

    private CommentResponses(List<CommentResponseNested> commentResponseNesteds) {
        this.commentResponseNesteds = commentResponseNesteds;
    }

    public static CommentResponses fromComments(List<Comment> comments) {
        return new CommentResponses(
            comments.stream()
                .map(CommentResponseNested::fromComment)
                .collect(Collectors.toList())
        );
    }

    public static CommentResponses fromCommentsAndUser(List<Comment> comments, User user) {
        return new CommentResponses(
            comments.stream()
                .map(comment -> CommentResponseNested.fromCommentAndUser(comment, user))
                .collect(Collectors.toList())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommentResponses that = (CommentResponses) o;
        return Objects.equals(commentResponseNesteds, that.commentResponseNesteds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentResponseNesteds);
    }

}
