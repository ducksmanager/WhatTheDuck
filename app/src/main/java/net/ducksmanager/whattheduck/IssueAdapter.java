package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class IssueAdapter extends ItemAdapter<Issue> {
    public IssueAdapter(List list, ArrayList<Issue> items) {
        super(list, R.layout.row_edge, items);
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
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        if (resourceToInflate != R.layout.row) {
            Issue i = getItem(position);

            String url = WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_EDGES_URL)
                + "/"
                + WhatTheDuck.getSelectedCountry()
                + "/gen/"
                + WhatTheDuck.getSelectedPublication()
                    .replaceFirst("[^/]+/", "")
                    .replaceAll(" ", "")
                + "." + i.getIssueNumber() + ".png";

            Picasso
                .with(getContext())
                .load(url)
                .rotate(90f)
                .into((ImageView) v.findViewById(R.id.itemedge));
        }

        return v;
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
