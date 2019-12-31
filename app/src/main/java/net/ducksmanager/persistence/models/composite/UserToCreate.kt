package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo
import com.google.gson.annotations.Expose
import net.ducksmanager.persistence.models.dm.User

class UserToCreate(
    username: String,
    password: String,

    @ColumnInfo
    @Expose
    private val password2: String,

    email: String
) : User(username, password, email)