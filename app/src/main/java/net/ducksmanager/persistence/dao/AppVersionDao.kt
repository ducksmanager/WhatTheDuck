package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.appfollow.AppVersion

@Dao
interface AppVersionDao {
    @Query("SELECT * FROM app_version")
    fun find() : AppVersion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(appVersion: AppVersion)

    @Query("DELETE FROM app_version")
    fun deleteAll()
}