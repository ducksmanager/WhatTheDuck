package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;

import net.ducksmanager.inducks.coa.IssueListing;

public class IssueList extends List<Issue> {

    private boolean isLandscape;

    @Override
    protected boolean needsToDownloadFullList() {
        return ! IssueListing.hasFullList(WhatTheDuck.getSelectedPublication());
    }

    @Override
    protected void downloadFullList() {
        new IssueListing(this, WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication(), activity ->
            ((List)activity.get()).notifyCompleteList()
        ).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.isLandscape = getResources().getConfiguration().orientation == 2;
        show();
    }

    @NonNull
    LinearLayoutManager getLayoutManager() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(isLandscape ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL);
        return llm;
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
            ),
            isLandscape);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(WhatTheDuck.wtd, PublicationList.class);
        i.putExtra("type", type);
        startActivity(i);
    }
}
