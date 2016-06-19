package net.ducksmanager.whattheduck;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class CountryList extends List {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (type.equals(CollectionType.USER.toString())) {
        	show();
	        setTitle(R.string.my_collection);
        }
        else {
            new CountryListing(this, R.id.progressBarLoading).execute();
            setTitle(getString(R.string.insert_issue_menu)+">"+getString(R.string.insert_issue__choose_country));
        }
    }
    
    public void show() {
        super.show(getCollection().getCountryList(this.type));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String selectedCountry = this.getListView().getItemAtPosition(((Long)id).intValue()).toString().replace("* ", "");
        getCollection().setSelectedCountry (CountryListing.getCountryShortName(selectedCountry));

        Intent i = new Intent(this, PublicationList.class);
        i.putExtra("type", this.type);
        startActivity(i);
    }
}