package net.ducksmanager.retrievetasks;

import android.app.Activity;
import android.content.Intent;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.whattheduck.Issue;
import net.ducksmanager.whattheduck.IssueList;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import java.lang.ref.WeakReference;

public class AddIssue extends RetrieveTask {

    private static WeakReference<Activity> originActivityRef;
    private static String shortCountryAndPublication;
    private static Issue selectedIssue;

    public AddIssue(WeakReference<Activity> originActivityRef, String shortCountryAndPublication, Issue selectedIssue) {
        super(
            "&ajouter_numero"
            +"&pays_magazine="+shortCountryAndPublication
            +"&numero="+selectedIssue.getIssueNumber()
            +"&id_acquisition="+(selectedIssue.getPurchase() == null ? "-2" : selectedIssue.getPurchase().getId())
            +"&etat="+selectedIssue.getIssueConditionStr(),
                R.id.progressBarLoading
        );
        AddIssue.originActivityRef = originActivityRef;
        AddIssue.shortCountryAndPublication = shortCountryAndPublication;
        AddIssue.selectedIssue = selectedIssue;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(originActivityRef, progressBarId, true);
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addissue/start");
    }

    @Override
    protected void onPostExecute(String response) {
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addissue/finish");
        if (response.equals("OK")) {
            WhatTheDuck.wtd.info(originActivityRef, R.string.confirmation_message__issue_inserted);
            WhatTheDuck.userCollection.addIssue(shortCountryAndPublication, selectedIssue);

            updateNamesAndGoToIssueList();
        }
        else {
            WhatTheDuck.wtd.alert(R.string.internal_error, R.string.internal_error__issue_insertion_failed);
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(originActivityRef, progressBarId, false);
    }

    static private void updateNamesAndGoToIssueList() {
        Activity callbackActivity = originActivityRef.get();

        String country=shortCountryAndPublication.split("/")[0];
        if (PublicationListing.hasFullList(country)) {
            callbackActivity.startActivity(new Intent(callbackActivity, IssueList.class));
        }
        else {
            new PublicationListing(callbackActivity, country, (e, result) -> {
                if (CountryListing.hasFullList) {
                    callbackActivity.startActivity(new Intent(callbackActivity, IssueList.class));
                } else {
                    new CountryListing(callbackActivity, (e2, result2) ->
                        callbackActivity.startActivity(new Intent(callbackActivity, IssueList.class))
                    ).fetch();
                }
            }).fetch();
        }
    }
}
