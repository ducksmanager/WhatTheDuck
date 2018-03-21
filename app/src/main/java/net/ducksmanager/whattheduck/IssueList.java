package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Switch;

import net.ducksmanager.inducks.coa.IssueListing;

public class IssueList extends List<Issue> {

    private boolean isLandscape;
    public enum ViewType {LIST,EDGE}
    private static String viewType = ViewType.LIST.toString();

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

        this.isLandscape = getResources().getConfiguration().orientation == 2 || getResources().getConfiguration().orientation == 4;
        Switch viewSwitch = this.findViewById(R.id.switchView);
        if (viewSwitch != null) {
            viewSwitch.setChecked(viewType.equals(ViewType.EDGE.toString()));
            viewSwitch.setOnClickListener(view -> {
                IssueList.this.goToAlternativeView(
                    viewSwitch.isChecked()
                        ? ViewType.EDGE.toString()
                        : ViewType.LIST.toString()
                );
            });
        }
        show();
    }

    private void goToAlternativeView(String newViewType) {
        viewType = newViewType;
        show();
    }

    @NonNull
    LinearLayoutManager getLayoutManager() {
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(isLandscape ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL);
        return llm;
    }

    @Override
    void show() {
        View viewSwitchWrapper = this.findViewById(R.id.switchViewWrapper);
        if (viewSwitchWrapper != null) {
            viewSwitchWrapper.setVisibility(isEdgeViewPossible() ? View.VISIBLE : View.GONE);
        }
        super.show();
    }

    @Override
    protected boolean shouldShow() {
        return WhatTheDuck.getSelectedCountry() != null && WhatTheDuck.getSelectedPublication() != null;
    }

    private boolean isEdgeViewPossible() {
        return type.equals(Collection.CollectionType.USER.toString());
    }

    @Override
    protected ItemAdapter getItemAdapter() {
        return new IssueAdapter(
            this,
            getCollection().getIssueList(
                WhatTheDuck.getSelectedCountry(),
                WhatTheDuck.getSelectedPublication()
            ),
            isEdgeViewPossible() && viewType.equals(ViewType.EDGE.toString()),
            isLandscape);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(WhatTheDuck.wtd, PublicationList.class);
        i.putExtra("type", type);
        startActivity(i);
    }
}
