package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;

import net.ducksmanager.inducks.coa.IssueListing;

public class IssueList extends List<Issue> {

    @Override
    protected boolean needsToDownloadFullList() {
        return ! IssueListing.hasFullList(WhatTheDuck.getSelectedPublication());
    }

    @Override
    protected void downloadFullList() {
        new IssueListing(this, WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication(), (e, result) ->
            IssueList.this.notifyCompleteList()
        ).fetch();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        show();
    }

    @Override
    protected boolean shouldShow() {
        return WhatTheDuck.getSelectedCountry() != null && WhatTheDuck.getSelectedPublication() != null;
    }

    @Override
    protected ItemAdapter getItemAdapter() {
        return new IssueAdapter(
            this,
            getCollection().getIssueList(
                WhatTheDuck.getSelectedCountry(),
                WhatTheDuck.getSelectedPublication()
            )
        );
    }

    @Override
    protected boolean userHasItemsInCollectionForCurrent() {
        return WhatTheDuck.userCollection.hasPublication(WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication());
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(WhatTheDuck.wtd, PublicationList.class);
        i.putExtra("type", type);
        startActivity(i);
    }
}
