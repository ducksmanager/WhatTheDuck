package net.ducksmanager.persistence.dao

import androidx.annotation.Nullable
import androidx.room.*
import net.ducksmanager.persistence.models.composite.UserSetting

@Dao
interface UserSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userSetting: UserSetting)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(@Nullable userSetting: UserSetting?)

    @Query("SELECT user_settings.* FROM user_settings WHERE settingKey = :key")
    fun findByKey(key: String): UserSetting?

    @Query("DELETE FROM user_settings")
    fun deleteAll()
}