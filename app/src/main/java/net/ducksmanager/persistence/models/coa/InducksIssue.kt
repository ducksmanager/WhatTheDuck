package net.ducksmanager.persistence.models.coa

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "inducks_issue", primaryKeys = ["inducksPublicationCode", "inducksIssueNumber"])
class InducksIssue(
    @ColumnInfo
    val inducksPublicationCode: String,

    @ColumnInfo
    val inducksIssueNumber: String
)