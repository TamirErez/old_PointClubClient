package com.tamir.client;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class UserInfo implements Serializable {
    //default serialVersion id
    private static final long serialVersionUID = 1L;

    private String username;

    public UserInfo(String username){
        this.username = username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getusername() {
        return this.username;
    }

    @NonNull
    @Override
    public String toString() {
        return "Username: " + this.username;
    }
}
