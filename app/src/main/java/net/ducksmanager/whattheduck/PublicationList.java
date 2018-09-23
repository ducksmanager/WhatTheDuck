package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;

import net.ducksmanager.inducks.coa.PublicationListing;

public class PublicationList extends ItemList<PublicationAdapter.Publication> {

    @Override
    protected boolean needsToDownloadFullList() {
        return ! PublicationListing.hasFullList(WhatTheDuck.getSelectedCountry());
    }

    @Override
    protected void downloadFullList() {
        new PublicationListing(this, WhatTheDuck.getSelectedCountry(), (e, result) ->
            PublicationList.this.notifyCompleteList()
        ).fetch();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WhatTheDuck.setSelectedPublication(null);
        show();
    }

    @Override
    protected boolean userHasItemsInCollectionForCurrent() {
        return WhatTheDuck.userCollection.hasCountry(WhatTheDuck.getSelectedCountry());
    }

    @Override
    protected boolean shouldShow() {
        return WhatTheDuck.getSelectedCountry() != null;
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
    protected ItemAdapter<PublicationAdapter.Publication> getItemAdapter() {
        return new PublicationAdapter(this, getCollection().getPublicationList(WhatTheDuck.getSelectedCountry()));
    }

    @Override
    public void onBackPressed() {
        if (type.equals(Collection.CollectionType.COA.toString())) {
            onBackFromAddIssueActivity();
        }
        else {
            startActivity(new Intent(WhatTheDuck.wtd, CountryList.class));
        }
    }
}
