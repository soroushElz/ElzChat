package com.example.ChatApplication.user.dtos;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class UserDto {
    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", email='" + email + '\'' +
                '}';
    }

    public UserDto(){}

    private Long id;

    private String email;

    public UserDto(Long id,String userName){
        this.email=userName;
        this.id=id;
    }


}
