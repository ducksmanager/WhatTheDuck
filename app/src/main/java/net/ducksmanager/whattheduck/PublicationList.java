package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;

public class PublicationList extends List {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WhatTheDuck.setSelectedPublication(null);

        super.onCreate(savedInstanceState);

        final String selectedCountry = WhatTheDuck.getSelectedCountry();
        final String countryFullName = CountryListing.getCountryFullName(selectedCountry);

        if (type.equals(CollectionType.USER.toString())
        || (type.equals(CollectionType.COA.toString()) && PublicationListing.hasFullList(selectedCountry))) {
            this.show();
        }
        else {
            new PublicationListing(this, selectedCountry).execute();
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
            super.show(getCollection().getPublicationList(WhatTheDuck.getSelectedCountry(), this.type));
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String publicationShortName = PublicationListing.getPublicationShortName(
            WhatTheDuck.getSelectedCountry(),
            this.getListView().getItemAtPosition(((Long) id).intValue()).toString().replace("* ", "")
        );
        WhatTheDuck.setSelectedPublication (publicationShortName);

        Intent i = new Intent(this, IssueList.class);
        i.putExtra("type", this.type);
        startActivity(i);
    }
}
