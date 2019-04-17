package net.ducksmanager.persistence.models.dm;

import com.google.gson.annotations.Expose;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @Expose
    @PrimaryKey
    @NonNull
    private final String username;

    @Expose
    private final String password;

    @Expose
    private String email = null;

    @Ignore
    public User(@NotNull String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(@NotNull String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    @NotNull
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
