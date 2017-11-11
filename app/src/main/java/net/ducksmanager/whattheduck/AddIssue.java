package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.util.SimpleCallback;

public class AddIssue extends RetrieveTask {

    private static Activity originActivity;
    private static String shortCountryAndPublication;
    private static Issue selectedIssue;

    private AddIssue(Activity il, String shortCountryAndPublication, Issue selectedIssue) {
        super(
            "&ajouter_numero"
            +"&pays_magazine="+shortCountryAndPublication
            +"&numero="+selectedIssue.getIssueNumber()
            +"&etat="+selectedIssue.getIssueConditionStr(),
                R.id.progressBarLoading
        );
        AddIssue.originActivity = il;
        AddIssue.shortCountryAndPublication = shortCountryAndPublication;
        AddIssue.selectedIssue = selectedIssue;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(AddIssue.originActivity, progressBarId, true);
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addissue/start");
    }

    @Override
    protected void onPostExecute(String response) {
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addissue/finish");
        if (response.equals("OK")) {
            WhatTheDuck.wtd.info(AddIssue.originActivity, R.string.confirmation_message__issue_inserted);
            WhatTheDuck.userCollection.addIssue(shortCountryAndPublication, selectedIssue);

            updateNamesAndGoToIssueList();
        }
        else {
            WhatTheDuck.wtd.alert(R.string.internal_error, R.string.internal_error__issue_insertion_failed);
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(AddIssue.originActivity, progressBarId, false);
    }

    static private void updateNamesAndGoToIssueList() {
        String country=shortCountryAndPublication.split("/")[0];
        if (PublicationListing.hasFullList(country)) {
            originActivity.startActivity(new Intent(originActivity, IssueList.class));
        }
        else {
            new PublicationListing(originActivity, country, new SimpleCallback() {
                @Override
                public void onDownloadFinished(Activity activity) {
                    if (CountryListing.hasFullList) {
                        originActivity.startActivity(new Intent(originActivity, IssueList.class));
                    }
                    else {
                        new CountryListing(originActivity, new SimpleCallback() {
                            @Override
                            public void onDownloadFinished(Activity activity) {
                                originActivity.startActivity(new Intent(originActivity, IssueList.class));
                            }
                        }).execute();
                    }
                }
            }).execute();
        }
    }

    static public void showAddIssueDialog(final Activity activity, final Issue selectedIssue) {
        final CharSequence[] items = {activity.getString(R.string.condition_bad), activity.getString(R.string.condition_notsogood), activity.getString(R.string.condition_good)};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
            .setView(R.layout.addissue)
            .setCancelable(true)
            .setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                if (selectedPosition == -1) {
                    WhatTheDuck.wtd.info(activity, R.string.input_error__select_condition);
                    return;
                }
                String condition = items[selectedPosition].toString();
                String DMcondition;
                if (condition.equals(activity.getString(R.string.condition_bad)))
                    DMcondition = Issue.BAD_CONDITION;
                else if (condition.equals(activity.getString(R.string.condition_notsogood)))
                    DMcondition = Issue.NOTSOGOOD_CONDITION;
                else
                    DMcondition = Issue.GOOD_CONDITION;
                selectedIssue.setIssueCondition(Issue.issueConditionStrToIssueCondition(DMcondition));
                new AddIssue(activity, WhatTheDuck.getSelectedPublication(), selectedIssue).execute();
                }
            })
            .setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        AlertDialog alert = builder.create();
        alert.show();

        ((TextView)alert.findViewById(R.id.addissue_title)).setText(activity.getString(R.string.insert_issue__confirm, selectedIssue.getIssueNumber()));
    }

}
