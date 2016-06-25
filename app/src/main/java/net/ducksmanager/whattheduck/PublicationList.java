package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;

public class PublicationList extends List {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String selectedCountry = getCollection().getSelectedCountry();
        final String countryFullName = CountryListing.getCountryFullName(selectedCountry);

        if (type.equals(CollectionType.USER.toString())) {
        	setTitle(getString(R.string.my_collection)+">"+ countryFullName);
            this.show();
        }
        else {
        	setTitle(getString(R.string.insert_issue_menu)+">"+ countryFullName);
            new PublicationListing(this, R.id.progressBarLoading, selectedCountry).execute();
        }

        setNavigationCountry(countryFullName, selectedCountry);
    }

    @Override
    public void onBackPressed() {
    	Intent i = new Intent(WhatTheDuck.wtd, CountryList.class);
        i.putExtra("type", type);
        startActivity(i);
	}
        
    public void show() {
    	if (getCollection().getSelectedCountry() != null) {
	    	super.show(getCollection().getPublicationList(getCollection().getSelectedCountry(), this.type));
    	}
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
            	WhatTheDuck.coaCollection.setSelectedCountry(getCollection().getSelectedCountry());
                Intent i = new Intent(WhatTheDuck.wtd, PublicationList.class);
                i.putExtra("type", CollectionType.COA.toString());
                startActivity(i);
                return super.onMenuItemSelected(featureId, item, true);
        }
        return super.onMenuItemSelected(featureId, item, false);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String publicationShortName = PublicationListing.getPublicationShortName(getCollection().getSelectedCountry(),
                this.getListView().getItemAtPosition(((Long) id).intValue()).toString().replace("* ", ""));
    	getCollection().setSelectedPublication (publicationShortName);

        Intent i = new Intent(this, IssueList.class);
        i.putExtra("type", this.type);
        startActivity(i);
    }
}
