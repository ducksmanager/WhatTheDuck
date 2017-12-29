package net.ducksmanager.whattheduck;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;

public class CountryList extends List {

    @Override
    protected boolean needsToDownloadFullList() {
        return type.equals(CollectionType.COA.toString()) && !CountryListing.hasFullList;
    }

    @Override
    protected void downloadFullList() {
        new CountryListing(this, activity ->
            ((List)activity.get()).notifyCompleteList()
        ).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (WhatTheDuck.getShowWelcomeMessage()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CountryList.this);
            builder.setTitle(getString(R.string.welcomeTitle));
            builder.setMessage(getString(R.string.welcomeMessage));
            builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss());
            WhatTheDuck.setShowWelcomeMessage(false);
            WhatTheDuck.saveSettings(null);
            builder.create().show();
        }

        this.findViewById(R.id.navigation).setVisibility(View.GONE);

        show();
    }

    protected void show() {
        super.show(new CountryAdapter(this, getCollection().getCountryList()));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        CountryAdapter.Country selectedCountry = (CountryAdapter.Country) this.getListView().getItemAtPosition(((Long)id).intValue());
        WhatTheDuck.setSelectedCountry (selectedCountry.getShortName());

        Intent i = new Intent(this, PublicationList.class);
        i.putExtra("type", this.type);
        startActivity(i);
    }
}