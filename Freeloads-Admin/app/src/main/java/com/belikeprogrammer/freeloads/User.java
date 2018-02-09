package com.belikeprogrammer.freeloads;

/**
 * Created by GOWVIK on 1/5/2018.
 */

public class User {
    private String name;
    private String bio;

    public User(String name, String bio) {
        this.name = name;
        this.bio = bio;
    }

    public User() {
    }

    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }
}
