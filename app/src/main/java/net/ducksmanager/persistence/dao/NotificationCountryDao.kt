package net.ducksmanager.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.ducksmanager.persistence.models.dm.NotificationCountry

@Dao
interface NotificationCountryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(issueList: List<NotificationCountry>)

    @Query("DELETE FROM notificationCountries")
    fun deleteAll()
}