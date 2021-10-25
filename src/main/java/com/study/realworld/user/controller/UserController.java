package com.study.realworld.user.controller;

import static com.study.realworld.user.controller.response.UserResponse.fromUserAndToken;

import com.study.realworld.security.JwtService;
import com.study.realworld.user.controller.request.UserJoinRequest;
import com.study.realworld.user.controller.request.UserLoginRequest;
import com.study.realworld.user.controller.request.UserUpdateRequest;
import com.study.realworld.user.controller.response.UserResponse;
import com.study.realworld.user.domain.User;
import com.study.realworld.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class UserController {

    private final UserService userService;

    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> join(@RequestBody UserJoinRequest request) {
        User user = userService.join(request.toUser());
        return ResponseEntity.ok().body(fromUserAndToken(user, jwtService.createToken(user)));
    }

    @PostMapping("/users/login")
    public ResponseEntity<UserResponse> login(@RequestBody UserLoginRequest request) {
        User user = userService.login(request.toEmail(), request.toPassword());
        return ResponseEntity.ok().body(fromUserAndToken(user, jwtService.createToken(user)));
    }

    @GetMapping("/user")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal Long loginId) {
        User user = userService.findById(loginId);
        return ResponseEntity.ok().body(fromUserAndToken(user, getTokenByContextHolder()));
    }

    @PutMapping("/user")
    public ResponseEntity<UserResponse> update(@RequestBody UserUpdateRequest request,
        @AuthenticationPrincipal Long loginId) {
        User user = userService.update(request.toUserUpdateModel(), loginId);
        return ResponseEntity.ok().body(fromUserAndToken(user, getTokenByContextHolder()));
    }

    private String getTokenByContextHolder() {
        return SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
    }

}