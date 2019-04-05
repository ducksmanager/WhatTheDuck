package net.ducksmanager.persistence.models.composite;

import net.ducksmanager.persistence.models.coa.InducksIssue;
import net.ducksmanager.persistence.models.dm.Issue;
import net.ducksmanager.whattheduck.R;

import androidx.room.Embedded;

public class InducksIssueWithUserIssueDetails {
    public static final String BAD_CONDITION="mauvais";
    public static final String NOTSOGOOD_CONDITION="moyen";
    public static final String GOOD_CONDITION="bon";
    public static final String NO_CONDITION="indefini";

    @Embedded
    private InducksIssue issue;

    @Embedded
    private Issue userIssue;

    public InducksIssueWithUserIssueDetails(InducksIssue issue, Issue userIssue) {
        this.issue = issue;
        this.userIssue = userIssue;
    }

    public static Integer issueConditionToResourceId(String issueCondition) {
        if (issueCondition == null) {
            return R.drawable.condition_none;
        }
        switch (issueCondition) {
            case NO_CONDITION:
                return R.drawable.condition_none;
            case BAD_CONDITION:
                return R.drawable.condition_bad;
            case NOTSOGOOD_CONDITION:
                return R.drawable.condition_notsogood;
            case GOOD_CONDITION:
                return R.drawable.condition_good;
            default:
                return R.drawable.condition_none;
        }
    }

    public static int issueConditionToStringId(String issueCondition) {
        switch (issueCondition) {
            case BAD_CONDITION:
                return R.string.condition_bad;
            case NOTSOGOOD_CONDITION:
                return R.string.condition_notsogood;
            case GOOD_CONDITION:
                return R.string.condition_good;
            default:
                return -1;
        }
    }

    public InducksIssue getIssue() {
        return issue;
    }

    public Issue getUserIssue() {
        return userIssue;
    }

}
