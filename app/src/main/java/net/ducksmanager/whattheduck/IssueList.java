package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import net.ducksmanager.inducks.coa.IssueListing;

import java.util.ArrayList;

public class IssueList extends ItemList<Issue> {
    enum ViewType {
        LIST_VIEW,
        EDGE_VIEW,
    }

    ViewType viewType = ViewType.LIST_VIEW;


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
    protected boolean hasDividers() {
        return !viewType.equals(ViewType.EDGE_VIEW);
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
    protected ItemAdapter<Issue> getItemAdapter() {
        View switchViewWrapper = this.findViewById(R.id.switchViewWrapper);
        Switch switchView = switchViewWrapper.findViewById(R.id.switchView);

        if (type.equals(Collection.CollectionType.COA.toString())) {
            viewType = ViewType.LIST_VIEW;
        }
        else {
            viewType = switchView.isChecked() ? ViewType.EDGE_VIEW : ViewType.LIST_VIEW;
        }

        if (type.equals(Collection.CollectionType.COA.toString())) {
            switchViewWrapper.setVisibility(View.GONE);
        }
        else {
            switchViewWrapper.setVisibility(View.VISIBLE);
            switchView.setOnClickListener(view -> {
                loadList();
                show();
            });
        }

        ArrayList<Issue> issueList = getCollection().getIssueList(
            WhatTheDuck.getSelectedCountry(),
            WhatTheDuck.getSelectedPublication()
        );
        return viewType.equals(ViewType.EDGE_VIEW)
            ? new IssueEdgeAdapter(this, issueList)
            : new IssueAdapter(this, issueList);
    }

    @Override
    protected boolean userHasItemsInCollectionForCurrent() {
        return WhatTheDuck.userCollection.hasPublication(WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication());
    }

    @Override
    public void onBackPressed() {
        if (type.equals(Collection.CollectionType.COA.toString())) {
            onBackFromAddIssueActivity();
        }
        else {
            startActivity(new Intent(WhatTheDuck.wtd, PublicationList.class));
        }
    }
}
