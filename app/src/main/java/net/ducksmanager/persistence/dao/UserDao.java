package net.ducksmanager.persistence.dao;

import net.ducksmanager.persistence.models.dm.User;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Query("SELECT * FROM users LIMIT 1")
    User getCurrentUser();

    @Query("DELETE FROM users")
    void deleteAll();
}
