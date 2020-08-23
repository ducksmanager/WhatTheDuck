package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.internal.Sync

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync ORDER BY timestamp DESC LIMIT 1")
    fun findLatest() : Sync?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sync: Sync)
}