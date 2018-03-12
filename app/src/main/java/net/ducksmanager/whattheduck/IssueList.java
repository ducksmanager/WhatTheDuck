package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;

import net.ducksmanager.inducks.coa.IssueListing;
import net.ducksmanager.retrievetasks.GetPurchaseList;
import net.ducksmanager.whattheduck.Collection.CollectionType;

import java.lang.ref.WeakReference;

public class IssueList extends List<Issue> {

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

        show();
    }

    protected void show() {
        if (WhatTheDuck.getSelectedCountry() != null && WhatTheDuck.getSelectedPublication() != null) {
            super.show(new IssueAdapter(this, getCollection().getIssueList(
                WhatTheDuck.getSelectedCountry(),
                WhatTheDuck.getSelectedPublication()
            )));
        }
    }

    @Override
    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return (adapterView, view, position, l) -> {
            if (type.equals(CollectionType.COA.toString())) {
                final Issue selectedIssue = (Issue) IssueList.this.lv.getItemAtPosition((int) l);
                if (WhatTheDuck.userCollection.getIssue(WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication(), selectedIssue.getIssueNumber()) != null) {
                    WhatTheDuck.wtd.info(new WeakReference<>(IssueList.this), R.string.input_error__issue_already_possessed);
                }
                else {
                    WhatTheDuck.setSelectedIssue(selectedIssue.getIssueNumber());
                    GetPurchaseList.initAndShowAddIssue(IssueList.this);
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(WhatTheDuck.wtd, PublicationList.class);
        i.putExtra("type", type);
        startActivity(i);
    }
}
