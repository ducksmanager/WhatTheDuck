package net.ducksmanager.persistence.models.composite;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_settings")
public class UserSetting {

    @PrimaryKey
    @NonNull
    private String settingKey;

    @ColumnInfo
    private String value;

    public UserSetting(@NotNull String settingKey, String value) {
        this.settingKey = settingKey;
        this.value = value;
    }

    @NotNull
    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(@NotNull String settingKey) {
        this.settingKey = settingKey;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
