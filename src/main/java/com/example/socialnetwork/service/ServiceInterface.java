package com.example.socialnetwork.service;

import com.example.socialnetwork.domain.FriendRequest;
import com.example.socialnetwork.domain.Friendship;
import com.example.socialnetwork.domain.Message;
import com.example.socialnetwork.domain.User;

import java.util.Optional;

public interface ServiceInterface {
    public Optional<User> addUser(String firstName, String lastName, String username, String password);

    public Optional<User> removeUser(Long id);

    public Optional<User> updateUser(Long id, String first_name, String last_name, String username, String password);

    public Optional<Friendship> removeFriendship(Long id1, Long id2);

    Iterable<User> getAllUsers();

    Iterable<Friendship> getAllFriendships();

    Iterable<FriendRequest> getAllFriendRequests();

    Iterable<Message> getAllMessages();

}