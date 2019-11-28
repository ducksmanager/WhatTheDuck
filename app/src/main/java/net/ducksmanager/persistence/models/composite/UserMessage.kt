package net.ducksmanager.persistence.models.composite

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "user_messages")
class UserMessage(messageKey: @NotNull String?, @ColumnInfo val isShown: Boolean) {
    @ColumnInfo
    @PrimaryKey
    @NonNull
    private val messageKey: String = messageKey!!

    fun getMessageKey(): @NotNull String? {
        return messageKey
    }
}