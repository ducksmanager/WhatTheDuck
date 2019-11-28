package net.ducksmanager.persistence.models.dm

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "users")
open class User {
    @JvmField
    @Expose
    @PrimaryKey
    val username: String

    @JvmField
    @Expose
    val password: String

    @Expose
    var email: String? = null
        private set

    @Ignore
    constructor(username: String, password: String) {
        this.username = username
        this.password = password
    }

    constructor(username: String, password: String, email: String?) {
        this.username = username
        this.password = password
        this.email = email
    }

}