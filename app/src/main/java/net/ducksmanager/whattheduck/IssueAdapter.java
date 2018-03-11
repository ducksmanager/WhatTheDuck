package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import net.ducksmanager.retrievetasks.GetPurchaseList;

import java.util.ArrayList;

public class IssueAdapter extends ItemAdapter<Issue> {

    private final boolean isLandscape;

    IssueAdapter(Activity activity, ArrayList<Issue> items, Boolean isLandscape) {
        super(activity, R.layout.row_edge, items);
        this.isLandscape = isLandscape;
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
//                    WhatTheDuck.wtd.info(new WeakReference<IssueAdapter.this>(context), R.string.input_error__issue_already_possessed);
                } else {
                    WhatTheDuck.setSelectedIssue(selectedIssue.getIssueNumber());
                    GetPurchaseList.initAndShowAddIssue(new IssueList());
                }
            }
        };
    }

    class ViewHolder extends ItemAdapter.ViewHolder {
        ImageView edgeView;

        ViewHolder(View v) {
            super(v);
            edgeView = v.findViewById(R.id.itemedge);
        }
    }


    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        IssueAdapter.ViewHolder issueHolder = (IssueAdapter.ViewHolder) holder;
        Issue i = getItem(position);

        Boolean isLandscapeView = true; //TODO

        String url = WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_EDGES_URL)
            + "/edges/"
            + WhatTheDuck.getSelectedCountry()
            + "/gen/"
            + WhatTheDuck.getSelectedPublication()
            .replaceFirst("[^/]+/", "")
            .replaceAll(" ", "")
            + "." + i.getIssueNumber() + ".png";

        ImageView edgeView = issueHolder.edgeView.findViewById(R.id.itemedge);
        Picasso
            .with(context)
            .load(url)
            .rotate(isLandscape ? 0 : 90f)
            .into(edgeView);
    }

    @Override
    protected boolean isHighlighted(Issue i) {
        return false;
    }

    @Override
    protected Integer getPrefixImageResource(Issue i, Activity activity) {
        return null;
    }

    @Override
    protected Integer getSuffixImageResource(Issue i) {
        return null;
    }

    @Override
    protected String getSuffixText(Issue i) {
        return null;
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