package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.composite.UserSetting

@Dao
interface UserSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userSetting: UserSetting)

    @Query("SELECT user_settings.* FROM user_settings WHERE settingKey = :key")
    fun findByKey(key: String): UserSetting?

    @Query("DELETE FROM user_settings")
    fun deleteAll()
}