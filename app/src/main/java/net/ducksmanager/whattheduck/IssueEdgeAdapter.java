package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class IssueEdgeAdapter extends ItemAdapter<Issue> {
    private final int orientation;
    private final RecyclerView recyclerView;
    private Integer expectedEdgeHeight;

    IssueEdgeAdapter(ItemList itemList, ArrayList<Issue> items, RecyclerView recyclerView, int orientation) {
        super(itemList, R.layout.row_edge, items);
        this.orientation = orientation;
        this.recyclerView = recyclerView;
    }

    @Override
    protected ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return null;
    }

    protected class ViewHolder extends ItemAdapter.ViewHolder {
        final ImageView edgeImage;

        ViewHolder(View v) {
            super(v);
            edgeImage = v.findViewById(R.id.edgeimage);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder itemHolder = (ViewHolder) holder;

        Issue i = getItem(position);
        if (i != null) {
            if (expectedEdgeHeight == null) {
                expectedEdgeHeight = orientation == Configuration.ORIENTATION_LANDSCAPE ? recyclerView.getHeight() : recyclerView.getWidth();
            }

            Picasso
                .with(((ViewHolder) holder).itemView.getContext())
                .load(getEdgeUrl(i))
                .resize(0, expectedEdgeHeight)
                .rotate(orientation == Configuration.ORIENTATION_LANDSCAPE ? 0 : 90f)
                .into(itemHolder.edgeImage);
        }
    }

    @NonNull
    private String getEdgeUrl(Issue i) {
        return String.format(
            "%s/edges/%s/gen/%s.%s.png",
            WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_EDGES_URL),
            WhatTheDuck.getSelectedCountry(),
            WhatTheDuck.getSelectedPublication()
                .replaceFirst("[^/]+/", "")
                .replaceAll(" ", ""),
            i.getCleanIssueNumber());
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
    protected String getIdentifier(Issue i) {
        return null;
    }

    @Override
    protected String getText(Issue i) {
        return null;
    }

    @Override
    protected String getComparatorText(Issue i) {
        return i.getIssueNumber();
    }
}
