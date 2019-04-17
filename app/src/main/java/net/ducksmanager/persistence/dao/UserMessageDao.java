package net.ducksmanager.persistence.dao;

import net.ducksmanager.persistence.models.composite.UserMessage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserMessage userMessage);

    @Query("SELECT * FROM user_messages WHERE messageKey = :key")
    UserMessage findByKey(String key);

    @Query("DELETE FROM user_messages")
    void deleteAll();
}
