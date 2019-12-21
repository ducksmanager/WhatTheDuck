package net.ducksmanager.persistence.models.composite

import androidx.room.Embedded
import net.ducksmanager.persistence.models.dm.Issue

class CoverSearchIssueWithUserIssueAndScore(
    @Embedded
    val coverSearchIssue: CoverSearchIssue,

    @Embedded
    val userIssue: Issue?,

    val suggestionScore: Int = 0
)