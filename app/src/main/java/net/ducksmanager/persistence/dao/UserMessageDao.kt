package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.composite.UserMessage

@Dao
interface UserMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userMessage: UserMessage)

    @Query("SELECT * FROM user_messages WHERE messageKey = :key")
    fun findByKey(key: String): UserMessage?

    @Query("DELETE FROM user_messages")
    fun deleteAll()
}