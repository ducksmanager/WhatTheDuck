package net.ducksmanager.persistence.models.dm

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "notificationCountries")
class NotificationCountry(
    @Expose
    @PrimaryKey
    val country: String
)