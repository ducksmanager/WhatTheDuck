package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;

public class CountryList extends List {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (
            (type.equals(CollectionType.USER.toString()))
         || (type.equals(CollectionType.COA.toString()) && CountryListing.hasFullList())) {
        	show();
        }
        else {
            new CountryListing(this).execute();
        }

        this.findViewById(R.id.navigation).setVisibility(View.GONE);
    }

    public void show() {
        super.show(getCollection().getCountryList(this.type));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String selectedCountry = this.getListView().getItemAtPosition(((Long)id).intValue()).toString().replace("* ", "");
        WhatTheDuck.setSelectedCountry (CountryListing.getCountryShortName(selectedCountry));

        Intent i = new Intent(this, PublicationList.class);
        i.putExtra("type", this.type);
        startActivity(i);
    }
}