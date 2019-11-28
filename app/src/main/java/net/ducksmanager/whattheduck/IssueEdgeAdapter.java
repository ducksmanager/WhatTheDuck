package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueDetails;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static net.ducksmanager.whattheduck.WhatTheDuck.*;

public class IssueEdgeAdapter extends ItemAdapter<InducksIssueWithUserIssueDetails> {
    private final int orientation;
    private final RecyclerView recyclerView;
    private Integer expectedEdgeHeight;

    IssueEdgeAdapter(ItemList itemList, List<InducksIssueWithUserIssueDetails> items, RecyclerView recyclerView, int orientation) {
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

    class ViewHolder extends ItemAdapter.ViewHolder {
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

        InducksIssueWithUserIssueDetails i = getItem(position);
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
    private String getEdgeUrl(InducksIssueWithUserIssueDetails i) {
        return String.format(
            "%s/edges/%s/gen/%s.%s.png",
            config.getProperty(CONFIG_KEY_EDGES_URL),
            selectedCountry,
            selectedPublication
                .replaceFirst("[^/]+/", "")
                .replaceAll(" ", ""),
            i.getIssue().getInducksIssueNumber().replaceAll(" ", ""));
    }

    @Override
    protected boolean isPossessed(InducksIssueWithUserIssueDetails i) {
        return i.getUserIssue() != null;
    }

    @Override
    protected Integer getPrefixImageResource(InducksIssueWithUserIssueDetails i, Activity activity) {
        return null;
    }

    @Override
    protected Integer getSuffixImageResource(InducksIssueWithUserIssueDetails i) {
        return null;
    }

    @Override
    protected String getSuffixText(InducksIssueWithUserIssueDetails i) {
        return null;
    }

    @Override
    protected String getIdentifier(InducksIssueWithUserIssueDetails i) {
        return null;
    }

    @Override
    protected String getText(InducksIssueWithUserIssueDetails i) {
        return null;
    }

    @Override
    protected String getComparatorText(InducksIssueWithUserIssueDetails i) {
        return i.getIssue().getInducksIssueNumber();
    }
}
