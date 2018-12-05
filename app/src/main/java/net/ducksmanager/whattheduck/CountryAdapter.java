package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class CountryAdapter extends ItemAdapter<CountryAdapter.Country> {

    static class Country {
        final String shortName;
        final String fullName;

        Country(String shortName, String fullName) {
            this.shortName = shortName;
            this.fullName = fullName;
        }

        String getShortName() {
            return shortName;
        }

        String getFullName() {
            return fullName;
        }
    }

    CountryAdapter(ItemList itemList, ArrayList<Country> items) {
        super(itemList, R.layout.row, items);
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

    protected class ViewHolder extends ItemAdapter.ViewHolder {
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
    protected String getIdentifier(Country i) {
        return i.getShortName();
    }

    @Override
    protected String getComparatorText(Country i) {
        return getText(i);
    }
}
