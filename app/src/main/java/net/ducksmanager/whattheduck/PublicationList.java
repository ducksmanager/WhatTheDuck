package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import net.ducksmanager.apigateway.DmServer;
import net.ducksmanager.persistence.models.coa.InducksPublication;
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Response;

public class PublicationList extends ItemList<InducksPublicationWithPossession> {

    @Override
    protected boolean hasList() {
        return false; // FIXME
    }

    @Override
    protected void downloadList() {
        this.findViewById(R.id.progressBar).setVisibility(ProgressBar.VISIBLE);
        DmServer.api.getPublications(WhatTheDuck.getSelectedCountry()).enqueue(new DmServer.Callback<HashMap<String, String>>(this) {
            @Override
            public void onSuccessfulResponse(Response<HashMap<String, String>> response) {
                List<InducksPublication> publications = new ArrayList<>();
                for(String publicationCode : response.body().keySet()) {
                    publications.add(new InducksPublication(publicationCode, response.body().get(publicationCode)));
                }
                WhatTheDuck.appDB.inducksPublicationDao().insertList(publications);
                setData();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WhatTheDuck.setSelectedPublication(null);
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
        WhatTheDuck.appDB.inducksPublicationDao().findByCountry(WhatTheDuck.getSelectedCountry()).observe(this, this::storeItemList);
    }

    @Override
    protected boolean shouldShow() {
        return WhatTheDuck.getSelectedCountry() != null;
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
        if (type.equals(WhatTheDuck.CollectionType.COA.toString())) {
            onBackFromAddIssueActivity();
        }
        else {
            startActivity(new Intent(WhatTheDuck.wtd, CountryList.class));
        }
    }
}
