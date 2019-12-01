package net.ducksmanager.persistence.models.composite

import androidx.room.Embedded
import net.ducksmanager.persistence.models.dm.Issue

class CoverSearchIssueWithUserIssueDetails(
    @Embedded
    val coverSearchIssue: CoverSearchIssue,

    @Embedded
    val userIssue: Issue?
)