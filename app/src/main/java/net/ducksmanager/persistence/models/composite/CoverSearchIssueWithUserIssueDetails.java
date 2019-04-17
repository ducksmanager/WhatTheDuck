package net.ducksmanager.persistence.models.composite;

import net.ducksmanager.persistence.models.dm.Issue;

import androidx.room.Embedded;

public class CoverSearchIssueWithUserIssueDetails {
    @Embedded
    private final CoverSearchIssue coverSearchIssue;

    @Embedded
    private final Issue userIssue;

    public CoverSearchIssueWithUserIssueDetails(CoverSearchIssue coverSearchIssue, Issue userIssue) {
        this.coverSearchIssue = coverSearchIssue;
        this.userIssue = userIssue;
    }

    public CoverSearchIssue getCoverSearchIssue() {
        return coverSearchIssue;
    }

    public Issue getUserIssue() {
        return userIssue;
    }

}
