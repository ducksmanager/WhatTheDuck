package net.ducksmanager.persistence.models.composite

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inducks_issue_count")
class InducksIssueCount(
        @ColumnInfo
        @PrimaryKey
        @NonNull
        val code: String,
        @ColumnInfo
        val count: Int
)