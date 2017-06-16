package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class AddIssue extends RetrieveTask {

    private static Activity originActivity;
    private static String shortCountryAndPublication;
    private static Issue selectedIssue;

    public AddIssue(Activity il, String shortCountryAndPublication, Issue selectedIssue) {
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
    }

    @Override
    protected void onPostExecute(String response) {
        if (response.equals("OK")) {
            WhatTheDuck.wtd.info(AddIssue.originActivity, R.string.confirmation_message__issue_inserted);
            WhatTheDuck.userCollection.addIssue(shortCountryAndPublication, selectedIssue);
//            originActivity.show();
        }
        else {
            WhatTheDuck.wtd.alert(R.string.internal_error, R.string.internal_error__issue_insertion_failed);
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(AddIssue.originActivity, progressBarId, false);
    }

    static public void showAddIssueDialog(final Activity activity, final Issue selectedIssue) {
        final CharSequence[] items = {activity.getString(R.string.condition_bad), activity.getString(R.string.condition_notsogood), activity.getString(R.string.condition_good)};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
            .setTitle(activity.getString(R.string.insert_issue__confirm, selectedIssue.getIssueNumber()))
            .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {}
            })
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
        builder.create().show();
    }

}
