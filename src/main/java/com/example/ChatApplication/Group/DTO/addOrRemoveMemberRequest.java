package com.example.ChatApplication.Group.DTO;

import lombok.Builder;

import java.util.List;
@Builder
public record addOrRemoveMemberRequest (List<Long> addNewMembers,
                                                List<Long> removeMembers
                                                ){



}
