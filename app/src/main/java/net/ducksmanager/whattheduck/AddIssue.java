package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.util.MultipleCustomCheckboxes;
import net.ducksmanager.util.SimpleCallback;
import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;
import java.util.Date;

public class AddIssue extends RetrieveTask {

    public static Activity originActivity;
    private static String shortCountryAndPublication;
    private static Issue selectedIssue;
    public static View dialogView;
    static MultipleCustomCheckboxes purchaseDateCheckboxes;
    public static ArrayList<PurchaseAdapter.Purchase> purchases;

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
        WhatTheDuck.wtd.toggleProgressbarLoading(originActivity, progressBarId, true);
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addissue/start");
    }

    @Override
    protected void onPostExecute(String response) {
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addissue/finish");
        if (response.equals("OK")) {
            WhatTheDuck.wtd.info(originActivity, R.string.confirmation_message__issue_inserted);
            WhatTheDuck.userCollection.addIssue(shortCountryAndPublication, selectedIssue);

            updateNamesAndGoToIssueList();
        }
        else {
            WhatTheDuck.wtd.alert(R.string.internal_error, R.string.internal_error__issue_insertion_failed);
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(originActivity, progressBarId, false);
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

    static void toggleAddPurchaseButton(Boolean toggle) {
        dialogView.findViewById(R.id.addpurchase).setEnabled(toggle);
    }

    static void updatePurchases() {
        ListView listView = dialogView.findViewById(R.id.purchase_list);
        PurchaseAdapter adapter = (PurchaseAdapter) listView.getAdapter();

        adapter.setItems(purchases);
        adapter.updateFilteredList("");
        adapter.notifyDataSetInvalidated();
        listView.setSelectionAfterHeaderView();
    }

    static public void showAddIssueDialog(final Activity activity, final Issue selectedIssue) {
        originActivity=activity;

        final CharSequence[] items = {activity.getString(R.string.condition_bad), activity.getString(R.string.condition_notsogood), activity.getString(R.string.condition_good)};
        purchases = WhatTheDuck.userCollection.getPurchaseListWithEmptyItem();

        LayoutInflater inflater = activity.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.addissue, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
            .setView(dialogView)
            .setCancelable(true)
            .setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
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

        ((TextView) dialogView.findViewById(R.id.addissue_title)).setText(activity.getString(R.string.insert_issue__confirm, selectedIssue.getIssueNumber()));

        MultipleCustomCheckboxes conditionCheckboxes = new MultipleCustomCheckboxes(
            (TextView) dialogView.findViewById(R.id.addissue_condition_text),
            dialogView,
            R.id.condition_selector

        );
        conditionCheckboxes.initClickEvents();
        conditionCheckboxes.checkInitialCheckbox(new MultipleCustomCheckboxes.CheckboxFilter() {
            @Override
            public boolean isMatched(CustomCheckBox checkbox) {
                return checkbox.getId() == R.id.nocondition;
            }
        });

        purchaseDateCheckboxes = new MultipleCustomCheckboxes(null, dialogView, R.id.purchase_list);

        ListView lv = dialogView.findViewById(R.id.purchase_list);

        lv.setAdapter(new PurchaseAdapter(activity, purchases));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((CustomCheckBox)view.findViewById(R.id.purchasecheck)).setChecked(true);
            }
        });

        dialogView.findViewById(R.id.addpurchase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAddPurchaseButton(false);

                purchases.add(0, new PurchaseAdapter.Purchase(null, new Date(), "", true));
                updatePurchases();
            }
        });

        final AlertDialog alert = builder.create();
        alert.show();
    }
}
