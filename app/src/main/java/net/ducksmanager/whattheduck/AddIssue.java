package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.util.SimpleCallback;

import java.lang.ref.WeakReference;

public class AddIssue extends RetrieveTask {

    private static WeakReference<Activity> originActivityRef;
    private static String shortCountryAndPublication;
    private static Issue selectedIssue;

    private AddIssue(WeakReference<Activity> originActivityRef, String shortCountryAndPublication, Issue selectedIssue) {
        super(
            "&ajouter_numero"
            +"&pays_magazine="+shortCountryAndPublication
            +"&numero="+selectedIssue.getIssueNumber()
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
            new PublicationListing(callbackActivity, country, new SimpleCallback() {
                @Override
                public void onDownloadFinished(WeakReference<Activity> activityRef) {
                    Activity callbackActivity = originActivityRef.get();

                    if (CountryListing.hasFullList) {
                        callbackActivity.startActivity(new Intent(callbackActivity, IssueList.class));
                    }
                    else {
                        new CountryListing(callbackActivity, new SimpleCallback() {
                            @Override
                            public void onDownloadFinished(WeakReference<Activity> activityRef) {
                                Activity callbackActivity = originActivityRef.get();
                                callbackActivity.startActivity(new Intent(callbackActivity, IssueList.class));
                            }
                        }).execute();
                    }
                }
            }).execute();
        }
    }

    static public void showAddIssueDialog(final WeakReference<Activity> activityRef, final Issue selectedIssue) {
        final Context appContext = WhatTheDuck.wtd.getApplicationContext();

        final CharSequence[] items = {appContext.getString(R.string.condition_bad), appContext.getString(R.string.condition_notsogood), appContext.getString(R.string.condition_good)};
        AlertDialog.Builder builder = new AlertDialog.Builder(activityRef.get());
        builder
            .setTitle(appContext.getString(R.string.insert_issue__confirm, selectedIssue.getIssueNumber()))
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {}
            })
            .setCancelable(true)
            .setPositiveButton(appContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                if (selectedPosition == -1) {
                    WhatTheDuck.wtd.info(activityRef, R.string.input_error__select_condition);
                    return;
                }
                String condition = items[selectedPosition].toString();
                String DMcondition;
                if (condition.equals(appContext.getString(R.string.condition_bad)))
                    DMcondition = Issue.BAD_CONDITION;
                else if (condition.equals(appContext.getString(R.string.condition_notsogood)))
                    DMcondition = Issue.NOTSOGOOD_CONDITION;
                else
                    DMcondition = Issue.GOOD_CONDITION;
                selectedIssue.setIssueCondition(Issue.issueConditionStrToIssueCondition(DMcondition));
                new AddIssue(activityRef, WhatTheDuck.getSelectedPublication(), selectedIssue).execute();
                }
            })
            .setNegativeButton(appContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        builder.create().show();
    }

}
