package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

public class PublicationAdapter extends ItemAdapter<PublicationAdapter.Publication> {

    public static class Publication {
        final String publicationCode;
        final String publicationTitle;

        public Publication(String publicationCode, String publicationTitle) {
            this.publicationCode = publicationCode;
            this.publicationTitle = publicationTitle;
        }

        public String getPublicationCode() {
            return publicationCode;
        }

        public String getPublicationTitle() {
            return publicationTitle;
        }
    }

    public PublicationAdapter(List list, ArrayList<Publication> items) {
        super(list, R.layout.row, items);
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

            Intent i = new Intent(context, IssueList.class);
            context.startActivity(i);
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
