package net.ducksmanager.persistence.models.composite;

import net.ducksmanager.persistence.models.coa.InducksIssue;
import net.ducksmanager.persistence.models.dm.IssueSimple;
import net.ducksmanager.whattheduck.R;

import androidx.room.Embedded;

public class InducksIssueWithUserIssueDetails {
    private static final String BAD_CONDITION="mauvais";
    private static final String NOTSOGOOD_CONDITION="moyen";
    private static final String GOOD_CONDITION="bon";
    private static final String NO_CONDITION="indefini";

    @Embedded
    private InducksIssue issue;

    @Embedded
    private IssueSimple userIssue;

    public InducksIssueWithUserIssueDetails(InducksIssue issue, IssueSimple userIssue) {
        this.issue = issue;
        this.userIssue = userIssue;
    }

    public static Integer issueConditionToResourceId(String issueCondition) {
        if (issueCondition == null || issueCondition.equals(NO_CONDITION))
            return R.drawable.condition_none;
        else if (issueCondition.equals(BAD_CONDITION))
            return R.drawable.condition_bad;
        else if (issueCondition.equals(NOTSOGOOD_CONDITION))
            return R.drawable.condition_notsogood;
        else if (issueCondition.equals(GOOD_CONDITION))
            return R.drawable.condition_good;
        return R.drawable.condition_none;
    }

    public InducksIssue getIssue() {
        return issue;
    }

    public IssueSimple getUserIssue() {
        return userIssue;
    }

}
