package net.ducksmanager.persistence.models.composite;

import net.ducksmanager.persistence.models.dm.Issue;

import androidx.room.Embedded;

public class CoverSearchIssueWithUserIssueDetails {
    @Embedded
    private CoverSearchIssue coverSearchIssue;

    @Embedded
    private Issue userIssue;

    public CoverSearchIssueWithUserIssueDetails(CoverSearchIssue coverSearchIssue, Issue userIssue) {
        this.coverSearchIssue = coverSearchIssue;
        this.userIssue = userIssue;
    }

    public CoverSearchIssue getCoverSearchIssue() {
        return coverSearchIssue;
    }

    public void setCoverSearchIssue(CoverSearchIssue coverSearchIssue) {
        this.coverSearchIssue = coverSearchIssue;
    }

    public Issue getUserIssue() {
        return userIssue;
    }

    public void setUserIssue(Issue userIssue) {
        this.userIssue = userIssue;
    }
}
