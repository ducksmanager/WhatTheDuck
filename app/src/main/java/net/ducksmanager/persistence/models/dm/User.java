package net.ducksmanager.persistence.models.dm;

import com.google.gson.annotations.Expose;

public class User {
    @Expose
    private final String username;

    @Expose
    private final String password;

    @Expose
    private final String email;

    protected User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

}
