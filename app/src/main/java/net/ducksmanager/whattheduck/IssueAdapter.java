package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.ducksmanager.retrievetasks.GetPurchaseList;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class IssueAdapter extends ItemAdapter<Issue> {
    IssueAdapter(Activity activity, ArrayList<Issue> items) {
        super(activity, R.layout.row, items);
    }

    @Override
    protected ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return view -> {
            int position = ((RecyclerView) view.getParent()).getChildLayoutPosition(view);
            if (List.type.equals(Collection.CollectionType.COA.toString())) {
                final Issue selectedIssue = IssueAdapter.this.getItem(position);
                if (WhatTheDuck.userCollection.getIssue(WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication(), selectedIssue.getIssueNumber()) != null) {
                    WhatTheDuck.wtd.info(new WeakReference<>(IssueAdapter.this.getOriginActivity()), R.string.input_error__issue_already_possessed);
                } else {
                    WhatTheDuck.setSelectedIssue(selectedIssue.getIssueNumber());
                    GetPurchaseList.initAndShowAddIssue(IssueAdapter.this.getOriginActivity());
                }
            }
        };
    }

    class ViewHolder extends ItemAdapter.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    protected boolean isHighlighted(Issue i) {
        return i.getIssueCondition() != null;
    }

    @Override
    protected Integer getPrefixImageResource(Issue i, Activity activity) {
        if (this.resourceToInflate == R.layout.row && i.getIssueCondition() != null) {
            return Issue.issueConditionToResourceId(i.getIssueCondition());
        } else {
            return android.R.color.transparent;
        }
    }

    @Override
    protected Integer getSuffixImageResource(Issue i) {
        if (this.resourceToInflate == R.layout.row && i.getPurchase() != null) {
            return R.drawable.ic_clock;
        } else {
            return null;
        }
    }

    @Override
    protected String getSuffixText(Issue i) {
        if (this.resourceToInflate == R.layout.row && i.getPurchase() != null) {
            return PurchaseAdapter.dateFormat.format(i.getPurchase().getPurchaseDate());
        } else {
            return null;
        }
    }

    @Override
    protected String getIdentifier(Issue i) {
        return i.getIssueNumber();
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
