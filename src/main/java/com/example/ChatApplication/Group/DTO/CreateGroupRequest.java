package com.example.ChatApplication.Group.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
@NotNull
public record CreateGroupRequest(@NotNull @NotEmpty String name,
                                List<Long> memberIds) {


}
