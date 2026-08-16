package com.example.ChatApplication.auth.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
