package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;

import net.ducksmanager.inducks.coa.PublicationListing;

public class PublicationList extends List {

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
        startActivity(i);
    }

    @NonNull
    LinearLayoutManager getLayoutManager() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        return llm;
    }
        
    protected void show() {
        if (WhatTheDuck.getSelectedCountry() != null) {
            super.show(new PublicationAdapter(this, getCollection().getPublicationList(WhatTheDuck.getSelectedCountry())));
        }
    }
}
