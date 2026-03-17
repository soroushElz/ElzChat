package com.example.ChatApplication.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank
    @NotNull(message="password is mandatory")
    @Size(min=8,message = "Password should be 8 characters long minimum")
    private String newPassword;
}
