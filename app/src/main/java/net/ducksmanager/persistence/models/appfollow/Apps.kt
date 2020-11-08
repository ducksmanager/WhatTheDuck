package net.ducksmanager.persistence.models.appfollow

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Apps(
        @Expose
        @SerializedName(value = "apps_app")
        val apps: List<AppWrapper>
)

class AppWrapper(
        @Expose
        val app: AppVersion
)

@Entity(tableName = "app_version")
class AppVersion(
        @Expose
        @PrimaryKey
        val version: String
)