package com.example.shreyus.myapp;

public interface Command<T> {
    void execute(T data);
}
