package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.dm.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @get:Query("SELECT * FROM users LIMIT 1")
    val currentUser: User?

    @Query("DELETE FROM users")
    fun deleteAll()
}