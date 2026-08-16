package com.example.ChatApplication.auth;

import com.example.ChatApplication.Exception.TokenRefreshException;
import com.example.ChatApplication.auth.DTO.AuthenticationRequest;
import com.example.ChatApplication.auth.DTO.AuthenticationResponse;
import com.example.ChatApplication.auth.DTO.RegistrationRequest;
import com.example.ChatApplication.auth.DTO.TokenRefreshRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest registrationRequest) throws JsonProcessingException {
        authenticationService.register(registrationRequest);
        ObjectMapper objectMapper = new ObjectMapper();

        return ResponseEntity.ok(objectMapper.writeValueAsString(Map.of("message","registered successfully")));
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest Request) {
        return ResponseEntity.ok(authenticationService.authenticate(Request));
    }



    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/refreshtoken")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request)
            throws TokenRefreshException {

        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }







}