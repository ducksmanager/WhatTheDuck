package net.ducksmanager.persistence.models.dm;

import com.google.gson.annotations.Expose;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "issues")
public class User {
    @Expose
    @PrimaryKey
    private String username;

    @Expose
    @ColumnInfo
    private String password;

    @Expose
    @ColumnInfo
    private String email;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

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
