package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class IssueEdgeAdapter extends ItemAdapter<Issue> {
    private int windowWidth;

    IssueEdgeAdapter(ItemList itemList, ArrayList<Issue> items) {
        super(itemList, R.layout.row_edge, items);
    }


    @Override
    protected ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return null;
    }

    class ViewHolder extends ItemAdapter.ViewHolder {
        final ImageView edgeImage;

        ViewHolder(View v) {
            super(v);
            edgeImage = v.findViewById(R.id.edgeimage);
        }
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        windowWidth = originActivity.getWindow().getDecorView().getWidth();
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder itemHolder = (ViewHolder) holder;

        Issue i = getItem(position);
        if (i != null) {
            String url = WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_EDGES_URL)
                + "/edges/"
                + WhatTheDuck.getSelectedCountry()
                + "/gen/"
                + WhatTheDuck.getSelectedPublication()
                .replaceFirst("[^/]+/", "")
                .replaceAll(" ", "")
                + "." + i.getIssueNumber() + ".png";

            Picasso
                .with(((ViewHolder) holder).itemView.getContext())
                .load(url)
                .resize(0, windowWidth)
                .rotate(90f)
                .into(itemHolder.edgeImage);
        }
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
        return null;
    }
}
