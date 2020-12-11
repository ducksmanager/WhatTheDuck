package net.ducksmanager.persistence.models.composite

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "user_settings")
class UserSetting(settingKey: @NotNull String?, value: String) {

    companion object {
        const val SETTING_KEY_NOTIFICATIONS_ENABLED = "SETTING_KEY_NOTIFICATIONS_ENABLED"
        const val SETTING_KEY_ISSUE_SELECTION_TIP_ENABLED = "SETTING_KEY_ISSUE_SELECTION_TIP_ENABLED"
    }

    @PrimaryKey
    @NonNull
    private var settingKey: String

    @ColumnInfo
    var value: String

    fun getSettingKey(): @NotNull String = settingKey

    fun setSettingKey(settingKey: @NotNull String?) {
        this.settingKey = settingKey!!
    }

    init {
        this.settingKey = settingKey!!
        this.value = value
    }
}