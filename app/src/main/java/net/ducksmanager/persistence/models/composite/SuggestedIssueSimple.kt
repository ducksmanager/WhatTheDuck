package net.ducksmanager.persistence.models.composite

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity(tableName = "suggested_issues", primaryKeys = ["publicationCode", "issueNumber"])
class SuggestedIssueSimple(
    val publicationCode: String,

    val issueNumber: String,

    @ColumnInfo
    val suggestionScore: Int,

    @ColumnInfo
    val oldestdate: String?,

    @ColumnInfo
    val stories: MutableSet<String> = mutableSetOf()
)