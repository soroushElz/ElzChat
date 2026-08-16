package com.example.ChatApplication.Group.DTO;

import java.util.List;

public record UpdateBanListRequest(List<Long> addUsersToBanlist,
                                   List<Long> removeUsersFromBanlist) {
}
