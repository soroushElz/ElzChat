package com.example.ChatApplication.user.services;

import com.example.ChatApplication.user.User;

@FunctionalInterface
public interface IUserRetrievalStrategy<T> {

    User getUser(T identifier);
}
