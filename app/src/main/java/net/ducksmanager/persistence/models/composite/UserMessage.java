package net.ducksmanager.persistence.models.composite;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_messages")
public class UserMessage {

    @ColumnInfo
    @PrimaryKey
    @NonNull
    private final String messageKey;

    @ColumnInfo
    private final Boolean isShown;

    public UserMessage(@NotNull String messageKey, Boolean isShown) {
        this.messageKey = messageKey;
        this.isShown = isShown;
    }

    @NotNull
    public String getMessageKey() {
        return messageKey;
    }

    public Boolean isShown() {
        return isShown;
    }
}
