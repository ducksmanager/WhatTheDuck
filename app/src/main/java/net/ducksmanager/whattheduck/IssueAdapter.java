package net.ducksmanager.whattheduck;

import android.app.Activity;

import java.util.ArrayList;

public class IssueAdapter extends ItemAdapter<Issue> {
    public IssueAdapter(List list, ArrayList<Issue> items) {
        super(list, items);
    }

    @Override
    protected boolean isHighlighted(Issue i) {
        return i.getIssueCondition() != null;
    }

    @Override
    protected Integer getImageResource(Issue i, Activity activity) {
        if (i.getIssueCondition() != null) {
            return Issue.issueConditionToResourceId(i.getIssueCondition());
        } else {
            return android.R.color.transparent;
        }
    }

    @Override
    protected String getText(Issue i) {
        return i.getIssueNumber();
    }

    @Override
    protected String getComparatorText(Issue i) {
        return getText(i);
    }
}
