package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static net.ducksmanager.whattheduck.WhatTheDuckApplication.selectedPublication;

public class PublicationAdapter extends ItemAdapter<InducksPublicationWithPossession> {

    PublicationAdapter(ItemList itemList, List<InducksPublicationWithPossession> items) {
        super(itemList, R.layout.row, items);
    }

    @Override
    protected ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    class ViewHolder extends ItemAdapter.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }

    @Override
    protected boolean isPossessed(InducksPublicationWithPossession inducksPublicationWithPossession) {
        return inducksPublicationWithPossession.getPossessed();
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return view -> {
            int position = ((RecyclerView)view.getParent()).getChildLayoutPosition(view);
            selectedPublication = PublicationAdapter.this.getItem(position).getPublication().getPublicationCode();

            Intent i = new Intent(originActivity, IssueList.class);
            originActivity.startActivity(i);
        };
    }

    @Override
    protected Integer getPrefixImageResource(InducksPublicationWithPossession i, Activity activity) {
        return null;
    }


    @Override
    protected Integer getSuffixImageResource(InducksPublicationWithPossession i) {
        return null;
    }

    @Override
    protected String getIdentifier(InducksPublicationWithPossession i) {
        return i.getPublication().getPublicationCode();
    }

    @Override
    protected String getSuffixText(InducksPublicationWithPossession i) {
        return null;
    }

    @Override
    protected String getText(InducksPublicationWithPossession i) {
        return i.getPublication().getTitle();
    }

    @Override
    protected String getComparatorText(InducksPublicationWithPossession i) {
        return getText(i);
    }
}
