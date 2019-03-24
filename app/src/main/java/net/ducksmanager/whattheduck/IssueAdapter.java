package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueDetails;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class IssueAdapter extends ItemAdapter<InducksIssueWithUserIssueDetails> {
    IssueAdapter(ItemList itemList, List<InducksIssueWithUserIssueDetails> items) {
        super(itemList, R.layout.row, items);
    }

    @Override
    protected ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return view -> {
            int position = ((RecyclerView) view.getParent()).getChildLayoutPosition(view);
            if (ItemList.type.equals(WhatTheDuck.CollectionType.COA.toString())) {
                final InducksIssueWithUserIssueDetails selectedIssue = IssueAdapter.this.getItem(position);
                if (selectedIssue.getUserIssue() != null) {
                    WhatTheDuck.wtd.info(new WeakReference<>(IssueAdapter.this.getOriginActivity()), R.string.input_error__issue_already_possessed, Toast.LENGTH_SHORT);
                } else {
                    WhatTheDuck.wtd.toggleProgressbarLoading(new WeakReference<>(IssueAdapter.this.getOriginActivity()), true);
                    WhatTheDuck.setSelectedIssue(selectedIssue.getIssue().getIssueNumber());

                    Intent i = new Intent(originActivity, net.ducksmanager.whattheduck.AddIssue.class);
                    originActivity.startActivity(i);
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
    protected Integer getPrefixImageResource(InducksIssueWithUserIssueDetails i, Activity activity) {
        if (this.resourceToInflate == R.layout.row && i.getUserIssue() != null) {
            return InducksIssueWithUserIssueDetails.issueConditionToResourceId(i.getUserIssue().getCondition());
        } else {
            return android.R.color.transparent;
        }
    }

    @Override
    protected Integer getSuffixImageResource(InducksIssueWithUserIssueDetails i) {
        if (i.getUserIssue() != null && i.getUserIssue().getPurchaseId() != null) {
            return R.drawable.ic_clock;
        } else {
            return null;
        }
    }

    @Override
    protected String getSuffixText(InducksIssueWithUserIssueDetails i) {
        if (i.getUserIssue() != null && i.getUserIssue().getPurchaseId() != null) {
            return PurchaseAdapter.dateFormat.format(i.getUserIssue().getPurchaseId());
        } else {
            return null;
        }
    }

    @Override
    protected String getIdentifier(InducksIssueWithUserIssueDetails i) {
        return i.getIssue().getIssueNumber();
    }

    @Override
    protected String getText(InducksIssueWithUserIssueDetails i) {
        return i.getIssue().getIssueNumber();
    }

    @Override
    protected String getComparatorText(InducksIssueWithUserIssueDetails i) {
        return getText(i);
    }

    @Override
    protected boolean isPossessed(InducksIssueWithUserIssueDetails i) {
        return i.getUserIssue() != null;
    }
}
