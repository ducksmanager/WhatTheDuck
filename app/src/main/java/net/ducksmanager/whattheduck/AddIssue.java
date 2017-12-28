package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;
import android.widget.TextView;

import net.ducksmanager.util.MultipleCustomCheckboxes;
import net.igenius.customcheckbox.CustomCheckBox;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class AddIssue extends Activity {

    static AddIssue instance;
    static HashMap<String,Purchase> purchases;

    private static String selectedCondition = null;
    static String selectedPurchaseHash = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        setContentView(R.layout.addissue);
        purchases = WhatTheDuck.userCollection.getPurchasesWithEmptyItem();

        show();
    }

    private void show() {
        MultipleCustomCheckboxes conditionCheckboxes = new MultipleCustomCheckboxes(
            new WeakReference<>(this.findViewById(R.id.condition_selector)),
            view -> {
                selectedCondition = view.getContentDescription().toString();
                ((TextView) AddIssue.this.findViewById(R.id.addissue_condition_text)).setText(selectedCondition);
            },
            view -> {
                selectedCondition = null;
                ((TextView) AddIssue.this.findViewById(R.id.addissue_condition_text)).setText("");
            }

        );
        conditionCheckboxes.initClickEvents();
        conditionCheckboxes.checkInitialCheckbox(checkbox ->
            checkbox.getId() == R.id.nocondition
        );

        showPurchases(true);

        this.findViewById(R.id.addissue_ok).setOnClickListener(view -> {
            final Context appContext = WhatTheDuck.wtd.getApplicationContext();
            String DMcondition;
            if (selectedCondition.equals(appContext.getString(R.string.condition_none)))
                DMcondition = Issue.NO_CONDITION;
            else if (selectedCondition.equals(appContext.getString(R.string.condition_bad)))
                DMcondition = Issue.BAD_CONDITION;
            else if (selectedCondition.equals(appContext.getString(R.string.condition_notsogood)))
                DMcondition = Issue.NOTSOGOOD_CONDITION;
            else
                DMcondition = Issue.GOOD_CONDITION;

            Purchase selectedPurchase= purchases.get(selectedPurchaseHash);

            new net.ducksmanager.retrievetasks.AddIssue(
                new WeakReference<>(AddIssue.this),
                WhatTheDuck.getSelectedPublication(),
                new Issue(
                    WhatTheDuck.getSelectedIssue(),
                    DMcondition,
                    selectedPurchase instanceof PurchaseWithDate
                        ? (PurchaseWithDate) selectedPurchase
                        : null
                )
            ).execute();
        });

        this.findViewById(R.id.addissue_cancel).setOnClickListener(view ->
            AddIssue.this.finish()
        );

        this.findViewById(R.id.addpurchase).setOnClickListener(view -> {
            toggleAddPurchaseButton(false);

            SpecialPurchase newPurchase = new SpecialPurchase(false, true);
            purchases.put(newPurchase.toString(), newPurchase);
            showPurchases(false);
        });

        setTitle(getString(R.string.insert_issue__confirm, WhatTheDuck.getSelectedIssue()));
    }

    void toggleAddPurchaseButton(Boolean toggle) {
        instance.findViewById(R.id.addpurchase).setEnabled(toggle);
    }

    void showPurchases(final Boolean checkNoPurchaseItem) {
        final ListView lv = this.findViewById(R.id.purchase_list);

        final MultipleCustomCheckboxes purchaseDateCheckboxes = new MultipleCustomCheckboxes(
            new WeakReference<>(lv),
            view ->
                selectedPurchaseHash = view.getContentDescription().toString(),
            view ->
                selectedPurchaseHash = null
        );
        lv.setAdapter(new PurchaseAdapter(this, purchases));
        lv.post(() -> {
            purchaseDateCheckboxes.initClickEvents();
            if (checkNoPurchaseItem) {
                purchaseDateCheckboxes.checkInitialCheckbox(checkbox -> checkbox.getContentDescription().toString().contains(SpecialPurchase.class.getSimpleName()));
            }
        });

        lv.setOnItemClickListener((adapterView, view, i, l) ->
            ((CustomCheckBox)view.findViewById(R.id.purchasecheck)).setChecked(true));
    }
}
