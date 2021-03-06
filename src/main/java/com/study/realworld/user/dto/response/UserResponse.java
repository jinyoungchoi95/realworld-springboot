package com.study.realworld.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.study.realworld.user.domain.Bio;
import com.study.realworld.user.domain.Email;
import com.study.realworld.user.domain.Image;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.domain.Username;

@JsonTypeName("user")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class UserResponse {

    @JsonProperty("username")
    private Username username;

    @JsonProperty("email")
    private Email email;

    @JsonProperty("bio")
    private Bio bio;

    @JsonProperty("image")
    private Image image;

    @JsonProperty("token")
    private String token;

    protected UserResponse() {
    }

    private UserResponse(Username username, Email email, Bio bio, Image image, String token) {
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.image = image;
        this.token = token;
    }

    public static UserResponse fromUserAndToken(User user, String accessToken) {
        return new UserResponse(
            user.username(),
            user.email(),
            user.bio(),
            user.image(),
            accessToken
        );
    }

}
