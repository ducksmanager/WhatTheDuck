package net.ducksmanager.persistence.dao;

import net.ducksmanager.persistence.models.composite.UserSetting;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserSetting userSetting);

    @Query("SELECT user_settings.* FROM user_settings WHERE settingKey = :key")
    UserSetting findByKey(String key);

    @Query("DELETE FROM user_settings")
    void deleteAll();
}
