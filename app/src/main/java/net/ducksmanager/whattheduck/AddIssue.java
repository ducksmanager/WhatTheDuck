package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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

import java.lang.ref.WeakReference;

public class AddIssue extends RetrieveTask {

    static WeakReference<Activity> originActivityRef;
    private static String shortCountryAndPublication;
    private static Issue selectedIssue;

    static WeakReference<View> dialogViewRef;
    static MultipleCustomCheckboxes purchaseDateCheckboxes;
    static ArrayList<PurchaseAdapter.Purchase> purchases;

    private static String selectedCondition = null;
    private static Integer selectedPurchaseId = null;

    private AddIssue(WeakReference<Activity> originActivityRef, String shortCountryAndPublication, Issue selectedIssue) {
        super(
            "&ajouter_numero"
            +"&pays_magazine="+shortCountryAndPublication
            +"&numero="+selectedIssue.getIssueNumber()
            +"&id_acquisition="+selectedIssue.getPurchaseId()
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

    static void toggleAddPurchaseButton(Boolean toggle) {
        dialogViewRef.get().findViewById(R.id.addpurchase).setEnabled(toggle);
    }

    static void updatePurchases() {
        ListView listView = dialogViewRef.get().findViewById(R.id.purchase_list);
        PurchaseAdapter adapter = (PurchaseAdapter) listView.getAdapter();

        adapter.setItems(purchases);
        adapter.updateFilteredList("");
        adapter.notifyDataSetChanged();
        listView.setSelectionAfterHeaderView();
    }

    static public void showAddIssueDialog(final WeakReference<Activity> activityRef, final Issue selectedIssue) {
        final Context appContext = WhatTheDuck.wtd.getApplicationContext();

        purchases = WhatTheDuck.userCollection.getPurchaseListWithEmptyItem();

        LayoutInflater inflater = activityRef.get().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.addissue, null);
        dialogViewRef = new WeakReference<>(dialogView);

        AlertDialog.Builder builder = new AlertDialog.Builder(activityRef.get());
        builder
            .setView(dialogView)
            .setCancelable(true)
            .setPositiveButton(appContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    String DMcondition;
                    if (selectedCondition.equals(appContext.getString(R.string.condition_none)))
                        DMcondition = Issue.NO_CONDITION;
                    else if (selectedCondition.equals(appContext.getString(R.string.condition_bad)))
                        DMcondition = Issue.BAD_CONDITION;
                    else if (selectedCondition.equals(appContext.getString(R.string.condition_notsogood)))
                        DMcondition = Issue.NOTSOGOOD_CONDITION;
                    else
                        DMcondition = Issue.GOOD_CONDITION;
                    selectedIssue.setIssueCondition(Issue.issueConditionStrToIssueCondition(DMcondition));
                    selectedIssue.setPurchaseId(selectedPurchaseId == null ? -2 : selectedPurchaseId);
                    new AddIssue(activityRef, WhatTheDuck.getSelectedPublication(), selectedIssue).execute();
                }
            })
            .setNegativeButton(appContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        ((TextView) dialogView.findViewById(R.id.addissue_title)).setText(appContext.getString(R.string.insert_issue__confirm, selectedIssue.getIssueNumber()));

        MultipleCustomCheckboxes conditionCheckboxes = new MultipleCustomCheckboxes(
            dialogViewRef,
            R.id.condition_selector,
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedCondition = view.getContentDescription().toString();
                    ((TextView) dialogViewRef.get().findViewById(R.id.addissue_condition_text)).setText(selectedCondition);
                }
            }

        );
        conditionCheckboxes.initClickEvents();
        conditionCheckboxes.checkInitialCheckbox(new MultipleCustomCheckboxes.CheckboxFilter() {
            @Override
            public boolean isMatched(CustomCheckBox checkbox) {
                return checkbox.getId() == R.id.nocondition;
            }
        });

        purchaseDateCheckboxes = new MultipleCustomCheckboxes(
            dialogViewRef,
            R.id.purchase_list,
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CharSequence contentDescription = view.getContentDescription();
                    if (contentDescription == null) {
                        selectedPurchaseId = null;
                    }
                    else {
                        selectedPurchaseId = Integer.parseInt(contentDescription.toString());
                    }
                }
            }
        );

        ListView lv = dialogView.findViewById(R.id.purchase_list);

        lv.setAdapter(new PurchaseAdapter(activityRef.get(), purchases));
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
