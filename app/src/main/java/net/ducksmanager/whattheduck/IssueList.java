package net.ducksmanager.whattheduck;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Switch;

import net.ducksmanager.inducks.coa.IssueListing;

import java.util.ArrayList;

public class IssueList extends ItemList<Issue> {
    enum ViewType {
        LIST_VIEW,
        EDGE_VIEW,
    }

    static ViewType viewType = ViewType.LIST_VIEW;

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
            switchViewWrapper.setVisibility(View.GONE);
        }
        else {
            switchViewWrapper.setVisibility(View.VISIBLE);
            switchView.setChecked(viewType.equals(ViewType.EDGE_VIEW));
            switchView.setOnClickListener(view -> {
                if (switchView.isChecked()) {
                    if (WhatTheDuck.showDataConsumptionMessage && WhatTheDuck.isMobileConnection()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(IssueList.this);
                        builder.setTitle(getString(R.string.bookcaseViewTitle));
                        builder.setMessage(getString(R.string.bookcaseViewMessage));
                        builder.setNegativeButton(R.string.cancel, (dialogInterface, which) -> {
                            switchView.toggle();
                            dialogInterface.dismiss();
                        });
                        builder.setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            WhatTheDuck.showDataConsumptionMessage = false;
                            dialogInterface.dismiss();
                            switchBetweenViews();
                        });
                        builder.create().show();
                    }
                    else {
                        switchBetweenViews();
                    }
                }
                else {
                    switchBetweenViews();
                }
            });
        }

        ArrayList<Issue> issueList = getCollection().getIssueList(
            WhatTheDuck.getSelectedCountry(),
            WhatTheDuck.getSelectedPublication()
        );

        if (viewType.equals(ViewType.EDGE_VIEW)) {
            int deviceOrientation = getResources().getConfiguration().orientation;
            int listOrientation = (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE)
                ? LinearLayoutManager.HORIZONTAL
                : LinearLayoutManager.VERTICAL;

            RecyclerView recyclerView = this.findViewById(R.id.itemList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, listOrientation, false));

            return new IssueEdgeAdapter(this, issueList, recyclerView, deviceOrientation);
        }
        else {
            return new IssueAdapter(this, issueList);
        }
    }

    private void switchBetweenViews() {
        viewType = ((Switch)this.findViewById(R.id.switchView)).isChecked() ? ViewType.EDGE_VIEW : ViewType.LIST_VIEW;
        loadList();
        show();
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
