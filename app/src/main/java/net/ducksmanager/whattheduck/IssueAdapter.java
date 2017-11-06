package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

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

    protected View getNewView() {
        return getLayoutInflater().inflate(R.layout.row_edge, null);
    }

    @Override
    protected void processView(View v, Issue i) {
        super.processView(v, i);

        String url = WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_EDGES_URL)
                    + ((IssueList) getContext()).getSelectedCountry()
                    + "/gen/" + ((IssueList) getContext()).getSelectedPublication().replaceFirst("[^/]+/", "")
                    + "." + i.getIssueNumber() + ".png";

        Picasso
            .with(getContext())
            .load(url)
            .rotate(90f)
            .into((ImageView) v.findViewById(R.id.itemedge));
    }

    @Override
    protected String getText(Issue i) {
        return i.getIssueNumber();
    }
}
