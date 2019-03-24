package net.ducksmanager.whattheduck;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ProgressBar;

import net.ducksmanager.apigateway.DmServer;
import net.ducksmanager.persistence.models.coa.InducksCountryName;
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession;
import net.ducksmanager.util.ReleaseNotes;
import net.ducksmanager.util.Settings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Response;

public class CountryList extends ItemList<InducksCountryNameWithPossession> {

    public static boolean hasFullList = false;

    @Override
    protected boolean hasFullList() {
        return hasFullList;
    }

    @Override
    protected void downloadFullList() {
        String locale = getApplicationContext().getResources().getConfiguration().locale.getLanguage();
        this.findViewById(R.id.progressBar).setVisibility(ProgressBar.VISIBLE);
        DmServer.api.getCountries(locale).enqueue(new DmServer.Callback<HashMap<String, String>>(this.findViewById(R.id.progressBar)) {
            @Override
            public void onSuccessfulResponse(Response<HashMap<String, String>> response) {
                List<InducksCountryName> countries = new ArrayList<>();
                for(String countryCode : response.body().keySet()) {
                    countries.add(new InducksCountryName(countryCode, response.body().get(countryCode)));
                }
                WhatTheDuck.appDB.inducksCountryDao().insertList(countries);
                hasFullList = true;
                setData();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_WELCOME)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CountryList.this);
            builder.setTitle(getString(R.string.welcomeTitle));
            builder.setMessage(getString(R.string.welcomeMessage));
            builder.setPositiveButton(R.string.ok, (dialogInterface, which) -> {
                ReleaseNotes.current.showOnVersionUpdate(new WeakReference<>(CountryList.this));
                dialogInterface.dismiss();
            });
            Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_WELCOME);
            Settings.saveSettings();
            builder.create().show();
        }
        else {
            ReleaseNotes.current.showOnVersionUpdate(new WeakReference<>(this));
        }


        WhatTheDuck.setSelectedCountry(null);
        WhatTheDuck.setSelectedPublication(null);
        show();
    }

    @Override
    protected boolean userHasItemsInCollectionForCurrent() {
        return true;
    }

    @Override
    protected boolean shouldShow() {
        return true;
    }

    @Override
    protected boolean shouldShowNavigationCountry() {
        return false;
    }

    @Override
    protected boolean shouldShowNavigationPublication() {
        return false;
    }

    @Override
    protected boolean shouldShowToolbar() {
        return true;
    }

    @Override
    protected boolean shouldShowAddToCollectionButton() {
        return true;
    }

    @Override
    protected boolean shouldShowFilter(List<InducksCountryNameWithPossession> countries) {
        return countries.size() > MIN_ITEM_NUMBER_FOR_FILTER;
    }

    @Override
    protected boolean hasDividers() {
        return true;
    }

    @Override
    protected ItemAdapter<InducksCountryNameWithPossession> getItemAdapter() {
        return new CountryAdapter(this, data);
    }

    protected void setData() {
        WhatTheDuck.appDB.inducksCountryDao().findAll().observe(CountryList.this, this::storeCountryList);
    }

    private void storeCountryList(List<InducksCountryNameWithPossession> inducksCountryNames) {
        this.data = inducksCountryNames;
        this.notifyCompleteList();
    }

    @Override
    public void onBackPressed() {
        if (type.equals(WhatTheDuck.CollectionType.COA.toString())) {
            onBackFromAddIssueActivity();
        }
    }
}