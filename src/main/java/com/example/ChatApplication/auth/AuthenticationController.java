package com.example.ChatApplication.auth;

import com.example.ChatApplication.Exception.ActivationTokenException;
import com.example.ChatApplication.Exception.ChangePasswordTokenException;
import com.example.ChatApplication.Exception.TokenRefreshException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name="Authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest registrationRequest) throws MessagingException {
        authenticationService.register(registrationRequest);
        return ResponseEntity.accepted().build();
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest Request) {
        return ResponseEntity.ok(authenticationService.authenticate(Request));
    }

    @GetMapping("/activate-account")
    public String confirm(
            @RequestParam String token)
            throws MessagingException, ActivationTokenException {
        authenticationService.activateAccount(token);
        return "user Account Enabled ";
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/refreshtoken")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request)
            throws TokenRefreshException {

        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }


    @PostMapping("/forgetPassword")
    public String resetForgottenPassword(@RequestParam(name="email",required = true) String email) throws MessagingException {
        authenticationService.resetForgottenPassword(email);
        return "change password email has been sent";
    }

    @PostMapping("/changePassword")
    public String validatePasswordChange(@RequestParam(name = "token",required = true) String token,
                                         @Valid @RequestBody ChangePasswordRequest request)
            throws MessagingException , ChangePasswordTokenException {
        authenticationService.changePassword(token, request.getNewPassword());
        return "password changed successfully!";
    }


}