package com.driver;

public class PlayListNotFoundException extends Exception {
    public PlayListNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
