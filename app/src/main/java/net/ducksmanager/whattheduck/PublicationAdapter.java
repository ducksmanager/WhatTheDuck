package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class PublicationAdapter extends ItemAdapter<PublicationAdapter.Publication> {

    static class Publication {
        final String publicationCode;
        final String publicationTitle;

        Publication(String publicationCode, String publicationTitle) {
            this.publicationCode = publicationCode;
            this.publicationTitle = publicationTitle;
        }

        String getPublicationCode() {
            return publicationCode;
        }

        String getPublicationTitle() {
            return publicationTitle;
        }
    }

    PublicationAdapter(ItemList itemList, ArrayList<Publication> items) {
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
    protected View.OnClickListener getOnClickListener() {
        return view -> {
            int position = ((RecyclerView)view.getParent()).getChildLayoutPosition(view);
            PublicationAdapter.Publication selectedPublication = PublicationAdapter.this.getItem(position);
            WhatTheDuck.setSelectedPublication (selectedPublication.getPublicationCode());

            Intent i = new Intent(originActivity, IssueList.class);
            originActivity.startActivity(i);
        };
    }

    @Override
    protected boolean isHighlighted(Publication i) {
        return WhatTheDuck.userCollection.hasPublication(WhatTheDuck.getSelectedCountry(), i.getPublicationCode());
    }

    @Override
    protected Integer getPrefixImageResource(Publication i, Activity activity) {
        return null;
    }


    @Override
    protected Integer getSuffixImageResource(Publication i) {
        return null;
    }

    @Override
    protected String getIdentifier(Publication i) {
        return i.getPublicationCode();
    }

    @Override
    protected String getSuffixText(Publication i) {
        return null;
    }

    @Override
    protected String getText(Publication i) {
        return i.getPublicationTitle();
    }

    @Override
    protected String getComparatorText(Publication i) {
        return getText(i);
    }
}
