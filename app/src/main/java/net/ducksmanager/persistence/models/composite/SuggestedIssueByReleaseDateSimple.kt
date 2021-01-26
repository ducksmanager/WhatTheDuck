package net.ducksmanager.persistence.models.composite

import androidx.room.Embedded
import androidx.room.Entity


@Entity(tableName = "suggested_issues_by_release_date", primaryKeys = ["publicationCode", "issueNumber"])
class SuggestedIssueByReleaseDateSimple(

    @Embedded
    var suggestedIssue: SuggestedIssueSimple
)