package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
        super(list, R.layout.row, items);
    }


    @Override
    protected ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return view -> {
            int position = ((RecyclerView)view.getParent()).getChildLayoutPosition(view);
            Country selectedCountry = CountryAdapter.this.getItem(position);
            WhatTheDuck.setSelectedCountry (selectedCountry.getShortName());

            Intent i = new Intent(originActivity, PublicationList.class);
            originActivity.startActivity(i);
        };
    }

    class ViewHolder extends ItemAdapter.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }

    @Override
    protected boolean isHighlighted(Country i) {
        return WhatTheDuck.userCollection.hasCountry(i.getShortName());
    }

    @Override
    protected Integer getPrefixImageResource(Country i, Activity a) {
        String uri = "@drawable/flags_" + i.getShortName();
        int imageResource = a.getResources().getIdentifier(uri, null, a.getPackageName());

        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown;
        }
        return imageResource;
    }

    @Override
    protected Integer getSuffixImageResource(Country i) {
        return null;
    }

    @Override
    protected String getSuffixText(Country i) {
        return null;
    }

    @Override
    protected String getText(Country i) {
        return i.getFullName();
    }

    @Override
    protected String getComparatorText(Country i) {
        return getText(i);
    }
}
