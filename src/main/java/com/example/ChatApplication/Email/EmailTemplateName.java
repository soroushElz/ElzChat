package com.example.ChatApplication.Email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACTIVATE_ACCOUNT("activate_account"),
    CHANGE_PASSWORD("change_password");


    private final String name;
    EmailTemplateName(String name) {
        this.name = name;
    }

}
