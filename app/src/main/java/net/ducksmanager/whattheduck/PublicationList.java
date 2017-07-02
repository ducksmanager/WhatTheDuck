package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.util.SimpleCallback;

public class PublicationList extends List {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WhatTheDuck.setSelectedPublication(null);

        super.onCreate(savedInstanceState);

        final String selectedCountry = WhatTheDuck.getSelectedCountry();
        final String countryFullName = CountryListing.getCountryFullName(selectedCountry);

        if (PublicationListing.hasFullList(selectedCountry)) {
            this.show();
        }
        else {
            new PublicationListing(this, selectedCountry, new SimpleCallback() {
                @Override
                public void onDownloadFinished(Activity activity) {
                    ((List)activity).show();
                }
            }).execute();
        }

        setNavigationCountry(countryFullName, selectedCountry);
        setNavigationPublication(null, null);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(WhatTheDuck.wtd, CountryList.class);
        i.putExtra("type", type);
        startActivity(i);
    }
        
    public void show() {
        if (WhatTheDuck.getSelectedCountry() != null) {
            super.show(new PublicationAdapter(this, getCollection().getPublicationList(WhatTheDuck.getSelectedCountry())));
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        PublicationAdapter.Publication selectedPublication = (PublicationAdapter.Publication) this.getListView().getItemAtPosition(((Long) id).intValue());
        WhatTheDuck.setSelectedPublication (selectedPublication.getPublicationCode());

        Intent i = new Intent(this, IssueList.class);
        i.putExtra("type", this.type);
        startActivity(i);
    }
}
