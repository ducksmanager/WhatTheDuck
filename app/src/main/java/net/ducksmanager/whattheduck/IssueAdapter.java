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
    protected Integer getPrefixImageResource(Issue i, Activity activity) {
        if (i.getIssueCondition() != null) {
            return Issue.issueConditionToResourceId(i.getIssueCondition());
        } else {
            return android.R.color.transparent;
        }
    }

    @Override
    protected Integer getSuffixImageResource(Issue i) {
        if (i.getPurchase() != null) {
            return R.drawable.ic_clock;
        } else {
            return null;
        }
    }

    @Override
    protected String getSuffixText(Issue i) {
        if (i.getPurchase() != null) {
            return PurchaseAdapter.dateFormat.format(i.getPurchase().getPurchaseDate());
        } else {
            return null;
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
