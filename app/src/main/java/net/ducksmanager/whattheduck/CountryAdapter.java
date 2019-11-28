package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class CountryAdapter extends ItemAdapter<InducksCountryNameWithPossession> {

    CountryAdapter(ItemList itemList, List<InducksCountryNameWithPossession> items) {
        super(itemList, R.layout.row, items);
    }

    @Override
    protected ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    protected boolean isPossessed(InducksCountryNameWithPossession c) {
        return c.isPossessed();
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return view -> {
            int position = ((RecyclerView)view.getParent()).getChildLayoutPosition(view);
            InducksCountryNameWithPossession selectedCountry = CountryAdapter.this.getItem(position);
            WhatTheDuck.selectedCountry = selectedCountry.country.getCountryCode();

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
    protected Integer getPrefixImageResource(InducksCountryNameWithPossession c, Activity a) {
        String uri = "@drawable/flags_" + c.country.getCountryCode();
        int imageResource = a.getResources().getIdentifier(uri, null, a.getPackageName());

        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown;
        }
        return imageResource;
    }

    @Override
    protected Integer getSuffixImageResource(InducksCountryNameWithPossession c) {
        return null;
    }

    @Override
    protected String getSuffixText(InducksCountryNameWithPossession c) {
        return null;
    }

    @Override
    protected String getText(InducksCountryNameWithPossession c) {
        return c.country.getCountryName();
    }

    @Override
    protected String getIdentifier(InducksCountryNameWithPossession c) {
        return c.country.getCountryCode();
    }

    @Override
    protected String getComparatorText(InducksCountryNameWithPossession c) {
        return getText(c);
    }
}
