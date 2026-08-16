package com.example.ChatApplication.Handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;
public enum BusinessErrorCodes {
    NO_CODE(0, NOT_IMPLEMENTED, "No code"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "User account is locked"),
    ACCOUNT_DISABLED(303, FORBIDDEN, "User account is disabled"),
    BAD_CREDENTIALS(304, FORBIDDEN, "Login and / or Password is incorrect"),
    REFRESH_TOKEN_EXPIRED(305,FORBIDDEN,"Refresh token not correct. Please make a new signin request"),


     ;
    @Getter
    private final int code;
    @Getter
    private final HttpStatus status;

    @Getter
    private final String description;
    BusinessErrorCodes(int code, HttpStatus status,String description){
       this.code=code;
       this.status=status;
       this.description=description;
    }
}
