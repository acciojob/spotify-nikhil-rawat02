package com.driver;

public class SongNotFoundException extends Exception {
    public SongNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
