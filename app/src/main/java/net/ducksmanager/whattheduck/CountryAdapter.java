package net.ducksmanager.whattheduck;

import android.app.Activity;

import java.util.ArrayList;

public class CountryAdapter extends ItemAdapter<CountryAdapter.Country> {

    public static class Country {
        final String shortName;
        final String fullName;

        public Country(String shortName, String fullName) {
            this.shortName = shortName;
            this.fullName = fullName;
        }

        public String getShortName() {
            return shortName;
        }

        public String getFullName() {
            return fullName;
        }
    }

    public CountryAdapter(List list, ArrayList<Country> items) {
        super(list, items);
    }

    @Override
    protected boolean isHighlighted(Country i) {
        return WhatTheDuck.userCollection.hasCountry(i.getShortName());
    }

    @Override
    protected Integer getImageResource(Country i, Activity a) {
        String uri = "@drawable/flags_" + i.getShortName();
        int imageResource = a.getResources().getIdentifier(uri, null, a.getPackageName());

        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown;
        }
        return imageResource;
    }

    @Override
    protected String getText(Country i) {
        return i.getFullName();
    }
}
