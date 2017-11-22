package net.ducksmanager.whattheduck;

import android.app.Activity;

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
        super(list, items);
    }

    @Override
    protected boolean isHighlighted(Publication i) {
        return WhatTheDuck.userCollection.hasPublication(WhatTheDuck.getSelectedCountry(), i.getPublicationCode());
    }

    @Override
    protected Integer getImageResource(Publication i, Activity activity) {
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
