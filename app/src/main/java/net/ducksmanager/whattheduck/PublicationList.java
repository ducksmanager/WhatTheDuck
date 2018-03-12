package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;

import net.ducksmanager.inducks.coa.PublicationListing;

public class PublicationList extends List {

    @Override
    protected boolean needsToDownloadFullList() {
        return ! PublicationListing.hasFullList(WhatTheDuck.getSelectedCountry());
    }

    @Override
    protected void downloadFullList() {
        new PublicationListing(this, WhatTheDuck.getSelectedCountry(), activity ->
            ((List)activity.get()).notifyCompleteList()
        ).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WhatTheDuck.setSelectedPublication(null);
        show();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(WhatTheDuck.wtd, CountryList.class);
        i.putExtra("type", type);
        startActivity(i);
    }
        
    protected void show() {
        if (WhatTheDuck.getSelectedCountry() != null) {
            super.show(new PublicationAdapter(this, getCollection().getPublicationList(WhatTheDuck.getSelectedCountry())));
        }
    }

    @Override
    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return (adapterView, view, position, l) -> {
            PublicationAdapter.Publication selectedPublication = (PublicationAdapter.Publication) PublicationList.this.lv.getItemAtPosition((int) l);
            WhatTheDuck.setSelectedPublication(selectedPublication.getPublicationCode());

            Intent i = new Intent(this, IssueList.class);
            i.putExtra("type", this.type);
            startActivity(i);
        };
    }
}
