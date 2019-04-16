package net.ducksmanager.persistence.models.composite;

import com.google.gson.annotations.Expose;

import net.ducksmanager.persistence.models.dm.User;

import androidx.room.ColumnInfo;

public class UserToCreate extends User {
    @Expose
    @ColumnInfo
    private String password2;

    public UserToCreate(String username, String password, String password2, String email) {
        super(username, password, email);
        this.password2 = password2;
    }

    public String getPassword2() {
        return password2;
    }
}
