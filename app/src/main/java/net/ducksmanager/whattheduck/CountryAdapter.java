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
    protected boolean isPossessed(InducksCountryNameWithPossession inducksCountryNameWithPossession) {
        return inducksCountryNameWithPossession.getPossessed();
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return view -> {
            int position = ((RecyclerView)view.getParent()).getChildLayoutPosition(view);
            InducksCountryNameWithPossession selectedCountry = CountryAdapter.this.getItem(position);
            WhatTheDuck.setSelectedCountry (selectedCountry.getCountry().getCountryCode());

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
    protected Integer getPrefixImageResource(InducksCountryNameWithPossession i, Activity a) {
        String uri = "@drawable/flags_" + i.getCountry().getCountryCode();
        int imageResource = a.getResources().getIdentifier(uri, null, a.getPackageName());

        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown;
        }
        return imageResource;
    }

    @Override
    protected Integer getSuffixImageResource(InducksCountryNameWithPossession i) {
        return null;
    }

    @Override
    protected String getSuffixText(InducksCountryNameWithPossession i) {
        return null;
    }

    @Override
    protected String getText(InducksCountryNameWithPossession i) {
        return i.getCountry().getCountryName();
    }

    @Override
    protected String getIdentifier(InducksCountryNameWithPossession i) {
        return i.getCountry().getCountryCode();
    }

    @Override
    protected String getComparatorText(InducksCountryNameWithPossession i) {
        return getText(i);
    }
}
