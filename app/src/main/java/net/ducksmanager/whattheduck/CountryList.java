package net.ducksmanager.whattheduck;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;

public class CountryList extends List<CountryAdapter.Country> {

    @Override
    protected boolean needsToDownloadFullList() {
        return type.equals(CollectionType.COA.toString()) && !CountryListing.hasFullList;
    }

    @Override
    protected void downloadFullList() {
        new CountryListing(this, (e, result) ->
            CountryList.this.notifyCompleteList()
        ).fetch();
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

        WhatTheDuck.setSelectedCountry(null);
        WhatTheDuck.setSelectedPublication(null);
        show();
    }

    protected void show() {
        super.show(new CountryAdapter(this, getCollection().getCountryList()));
    }

    @Override
    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return (adapterView, view, position, l) -> {
            CountryAdapter.Country selectedCountry = (CountryAdapter.Country) CountryList.this.lv.getItemAtPosition((int) l);
            WhatTheDuck.setSelectedCountry(selectedCountry.getShortName());

            Intent i = new Intent(this, PublicationList.class);
            i.putExtra("type", this.type);
            startActivity(i);
        };
    }
}