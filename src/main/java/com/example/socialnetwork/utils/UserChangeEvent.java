package com.example.socialnetwork.utils;

import com.example.socialnetwork.domain.User;

public class UserChangeEvent implements Event {
    private ChangeEventType type;
    private User newUser;
    private User oldUser;

    public UserChangeEvent(ChangeEventType type, User newMovie) {
        this.type = type;
        this.newUser = newMovie;
    }

    public UserChangeEvent(ChangeEventType type, User newUser, User oldUser) {
        this.type = type;
        this.newUser = newUser;
        this.oldUser = oldUser;
    }

    public ChangeEventType getType() {
        return type;
    }

    public User getNewUser() {
        return newUser;
    }

    public User getOldUser() {
        return oldUser;
    }
}

