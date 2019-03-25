package net.ducksmanager.persistence.models.composite;

import net.ducksmanager.persistence.models.dm.Issue;

import androidx.room.Embedded;

public class CoverSearchIssueWithUserIssueDetails {
    @Embedded
    private CoverSearchIssue issue;

    @Embedded
    private Issue userIssue;

    public CoverSearchIssueWithUserIssueDetails(CoverSearchIssue issue, Issue userIssue) {
        this.issue = issue;
        this.userIssue = userIssue;
    }

    public CoverSearchIssue getIssue() {
        return issue;
    }

    public void setIssue(CoverSearchIssue issue) {
        this.issue = issue;
    }

    public Issue getUserIssue() {
        return userIssue;
    }

    public void setUserIssue(Issue userIssue) {
        this.userIssue = userIssue;
    }
}
