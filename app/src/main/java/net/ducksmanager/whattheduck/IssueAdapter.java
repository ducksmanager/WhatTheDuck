package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.ducksmanager.retrievetasks.GetPurchaseList;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class IssueAdapter extends ItemAdapter<Issue> {

    private final boolean isLandscape;

    IssueAdapter(Activity activity, ArrayList<Issue> items, Boolean isEdgeView, boolean isLandscape) {
        super(activity, isEdgeView ? R.layout.row_edge : R.layout.row, items);
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
                    WhatTheDuck.wtd.info(new WeakReference<>(IssueAdapter.this.getOriginActivity()), R.string.input_error__issue_already_possessed);
                } else {
                    WhatTheDuck.setSelectedIssue(selectedIssue.getIssueNumber());
                    GetPurchaseList.initAndShowAddIssue(IssueAdapter.this.getOriginActivity());
                }
            }
        };
    }

    class ViewHolder extends ItemAdapter.ViewHolder {
        final ImageView edgeView;

        ViewHolder(View v) {
            super(v);
            edgeView = IssueAdapter.this.resourceToInflate == R.layout.row_edge ? v.findViewById(R.id.itemedge) : null;
        }
    }


    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final IssueAdapter.ViewHolder issueHolder = (IssueAdapter.ViewHolder) holder;

        if (issueHolder.edgeView != null) {
            Issue i = getItem(position);

            final String url = WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_EDGES_URL)
                + "/edges/"
                + WhatTheDuck.getSelectedCountry()
                + "/gen/"
                + WhatTheDuck.getSelectedPublication()
                    .replaceFirst("[^/]+/", "")
                    .replaceAll(" ", "")
                + "." + i.getIssueNumber().replaceAll(" ", "") + ".png";

            final Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    issueHolder.edgeView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
                    issueHolder.edgeView.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    System.err.println("Failed to load " + url);
                    issueHolder.edgeView.setLayoutParams(new LinearLayout.LayoutParams(32, 32));
                    issueHolder.edgeView.setImageResource(R.drawable.ico_delete_asset);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    issueHolder.edgeView.setLayoutParams(new LinearLayout.LayoutParams(32, 32));
                    issueHolder.edgeView.setImageResource(R.drawable.ic_clock);
                }
            };

            Picasso.with(IssueAdapter.this.getOriginActivity())
                .load(url)
                .rotate(isLandscape ? 0 : 90f)
                .into(target);
        }
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
    protected String getText(Issue i) {
        return i.getIssueNumber();
    }

    @Override
    protected String getComparatorText(Issue i) {
        return getText(i);
    }
}
