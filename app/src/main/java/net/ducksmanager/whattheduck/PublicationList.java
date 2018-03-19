package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;

import net.ducksmanager.inducks.coa.PublicationListing;

public class PublicationList extends List<PublicationAdapter.Publication> {

    @Override
    protected boolean needsToDownloadFullList() {
        return ! PublicationListing.hasFullList(WhatTheDuck.getSelectedCountry());
    }

    @Override
    protected void downloadFullList() {
        new PublicationListing(this, WhatTheDuck.getSelectedCountry(), activity ->
            ((List)activity.get()).notifyCompleteList()
        ).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WhatTheDuck.setSelectedPublication(null);
        show();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(WhatTheDuck.wtd, CountryList.class);
        i.putExtra("type", type);
        startActivity(i);
    }

    @Override
    protected boolean shouldShow() {
        return WhatTheDuck.getSelectedCountry() != null;
    }

    @Override
    protected ItemAdapter getItemAdapter() {
        return new PublicationAdapter(this, getCollection().getPublicationList(WhatTheDuck.getSelectedCountry()));
    }
}
