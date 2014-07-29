package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import net.ducksmanager.inducks.coa.CoaListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;

public class PublicationList extends List {
    protected static final int ACTIVITY_PUBLICATIONLIST=1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String selectedCountry = getCollection().getSelectedCountry();
        if (type.equals(CollectionType.USER.toString())) {
        	setTitle(getString(R.string.my_collection)+">"+CoaListing.getCountryFullName(selectedCountry));
            this.show();
        }
        else {
        	setTitle(getString(R.string.insert_issue_menu)+">"+CoaListing.getCountryFullName(selectedCountry));

            if (!WhatTheDuck.coaCollection.hasCountry(selectedCountry)) {
                new PublicationListing(this, R.id.progressBarLoading, selectedCountry, null).execute();
            }
            else {
                this.show();
            }
        }
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        String publicationShortName = CoaListing.getPublicationShortName(getCollection().getSelectedCountry(), 
        																 this.getListView().getItemAtPosition(((Long)id).intValue()).toString().replace("* ", ""));
    	getCollection().setSelectedPublication (publicationShortName);

        Intent i = new Intent(this, IssueList.class);
        i.putExtra("type", this.type);
        startActivity(i);
    }

    private void saveState() {
    }
}
