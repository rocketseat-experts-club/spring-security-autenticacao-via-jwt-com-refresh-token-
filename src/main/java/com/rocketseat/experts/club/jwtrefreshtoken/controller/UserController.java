package com.rocketseat.experts.club.jwtrefreshtoken.controller;

import com.rocketseat.experts.club.jwtrefreshtoken.jwt.Util;
import com.rocketseat.experts.club.jwtrefreshtoken.model.RefreshToken;
import com.rocketseat.experts.club.jwtrefreshtoken.model.User;
import com.rocketseat.experts.club.jwtrefreshtoken.request.LoginRequest;
import com.rocketseat.experts.club.jwtrefreshtoken.request.TokenRefreshRequest;
import com.rocketseat.experts.club.jwtrefreshtoken.response.TokenRefreshResponse;
import com.rocketseat.experts.club.jwtrefreshtoken.service.LoginService;
import com.rocketseat.experts.club.jwtrefreshtoken.service.RefreshTokenService;
import com.rocketseat.experts.club.jwtrefreshtoken.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    LoginService loginService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    private Util util;

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User user){
        log.info("criando um novo usuário com as infos : [{}]", user);
        return this.userService.createNewUser(user);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) throws Exception {
        log.info("usuário [{}] fazendo login", loginRequest.getUsername());
   return loginService.login(loginRequest);
    }


    @PostMapping("/refreshtoken")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> refreshtoken( @RequestBody TokenRefreshRequest request) {

        log.info("Solicitação de criação de refresh token para o token [{}]", request);
        return refreshTokenService.findByToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = refreshTokenService.generateTokenFromUsername(user.getUsername());
                   log.info("refresh token gerado com sucesso [{}]", token);
                    return ResponseEntity.ok(new TokenRefreshResponse(token, request.getRefreshToken(), "Bearer"));
                })
                .orElseThrow(() -> new CredentialsExpiredException(request.getRefreshToken()));
    }


    }