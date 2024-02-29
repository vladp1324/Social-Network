package com.example.socialnetwork.utils;

public interface Observer<E extends Event> {
    void update(E t);
}