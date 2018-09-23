package net.ducksmanager.whattheduck;

import android.app.AlertDialog;
import android.os.Bundle;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;

public class CountryList extends ItemList<CountryAdapter.Country> {

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

    @Override
    protected boolean userHasItemsInCollectionForCurrent() {
        return true;
    }

    @Override
    protected boolean shouldShow() {
        return true;
    }

    @Override
    protected boolean shouldShowNavigation() {
        return true;
    }

    @Override
    protected boolean shouldShowToolbar() {
        return true;
    }

    @Override
    protected boolean shouldShowAddToCollectionButton() {
        return true;
    }

    @Override
    protected boolean hasDividers() {
        return true;
    }

    @Override
    protected ItemAdapter<CountryAdapter.Country> getItemAdapter() {
        return new CountryAdapter(this, getCollection().getCountryList());
    }

    @Override
    public void onBackPressed() {
        if (type.equals(CollectionType.COA.toString())) {
            onBackFromAddIssueActivity();
        }
    }
}