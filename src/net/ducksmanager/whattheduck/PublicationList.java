package net.ducksmanager.whattheduck;

import net.ducksmanager.inducks.coa.CoaListing;
import net.ducksmanager.inducks.coa.CoaListing.ListType;
import net.ducksmanager.whattheduck.Collection.CollectionType;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class PublicationList extends List {
    protected static final int ACTIVITY_PUBLICATIONLIST=1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (type.equals(CollectionType.USER.toString())) {
        	setTitle(getString(R.string.my_collection)+">"+CoaListing.getCountryFullName(getCollection().getSelectedCountry()));
            show();
        }
        else {
        	setTitle(getString(R.string.insert_issue_menu)+">"+CoaListing.getCountryFullName(getCollection().getSelectedCountry()));
    		new CoaListing(this, ListType.PUBLICATION_LIST, R.id.progressBarLoading, getCollection().getSelectedCountry(), null).execute(new Object[0]);
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
