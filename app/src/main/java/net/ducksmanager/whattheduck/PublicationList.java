package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import net.ducksmanager.apigateway.DmServer;
import net.ducksmanager.persistence.models.coa.InducksPublication;
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Response;

import static net.ducksmanager.whattheduck.WhatTheDuckApplication.*;

public class PublicationList extends ItemList<InducksPublicationWithPossession> {

    @Override
    protected boolean hasList() {
        return false; // FIXME
    }

    @Override
    protected void downloadList(Activity currentActivity) {
        DmServer.api.getPublications(selectedCountry).enqueue(new DmServer.Callback<HashMap<String, String>>("getInducksPublications", currentActivity) {
            @Override
            public void onSuccessfulResponse(Response<HashMap<String, String>> response) {
                List<InducksPublication> publications = new ArrayList<>();
                for(String publicationCode : response.body().keySet()) {
                    publications.add(new InducksPublication(publicationCode, response.body().get(publicationCode)));
                }
                appDB.inducksPublicationDao().insertList(publications);
                setData();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedPublication = null;
        show();
    }

    @Override
    protected boolean isPossessedByUser() {
        for (InducksPublicationWithPossession i: data) {
            if (i.getPossessed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void setData() {
        appDB.inducksPublicationDao().findByCountry(selectedCountry).observe(this, this::storeItemList);
    }

    @Override
    protected boolean shouldShow() {
        return selectedCountry != null;
    }

    @Override
    protected boolean shouldShowNavigationCountry() {
        return true;
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
    protected boolean shouldShowFilter(List<InducksPublicationWithPossession> publications) {
        return publications.size() > MIN_ITEM_NUMBER_FOR_FILTER;
    }

    @Override
    protected boolean hasDividers() {
        return true;
    }

    @Override
    protected ItemAdapter<InducksPublicationWithPossession> getItemAdapter() {
        return new PublicationAdapter(this, data);
    }

    @Override
    public void onBackPressed() {
        if (type.equals(CollectionType.COA.toString())) {
            onBackFromAddIssueActivity();
        }
        else {
            startActivity(new Intent(this, CountryList.class));
        }
    }
}
