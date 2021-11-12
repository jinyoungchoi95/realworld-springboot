package com.study.realworld.testutil;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.snippet.Attributes.key;

import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.snippet.Attributes;

public interface DocumentFormatGenerator {

    static Attributes.Attribute getAuthorizationRequired() {
        return key("required").value("O");
    }

    static HeaderDescriptor getAuthorizationHeaderDescriptor() {
        return headerWithName("Authorization")
            .description("Token to use APIs that require authentication +\nAuthorization: Token {jwt.token.here}")
            .attributes(getAuthorizationRequired());
    }

}
