package com.example.ChatApplication.user.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Set;

@Builder
public record UpdateBlockListRequest(Set<Long> blockUsersList
                                     ,Set<Long> unBlockUsersList){

    public UpdateBlockListRequest {
        unBlockUsersList = (unBlockUsersList == null) ? Set.of() : unBlockUsersList;
        blockUsersList = (blockUsersList == null) ? Set.of() : blockUsersList;
    }
}
