package net.ducksmanager.whattheduck;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import net.ducksmanager.util.MultipleCustomCheckboxes;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class AddIssue extends AppCompatActivity {

    static AddIssue instance;
    static HashMap<String,PurchaseAdapter.Purchase> purchases;

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

    protected void show() {
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

            PurchaseAdapter.Purchase selectedPurchase= purchases.get(selectedPurchaseHash);

            new net.ducksmanager.retrievetasks.AddIssue(
                new WeakReference<>(AddIssue.this),
                WhatTheDuck.getSelectedPublication(),
                new Issue(
                    WhatTheDuck.getSelectedIssue(),
                    DMcondition,
                    selectedPurchase instanceof PurchaseAdapter.PurchaseWithDate
                        ? (PurchaseAdapter.PurchaseWithDate) selectedPurchase
                        : null
                )
            ).execute();
        });

        this.findViewById(R.id.addissue_cancel).setOnClickListener(view ->
            AddIssue.this.finish()
        );

        this.findViewById(R.id.addpurchase).setOnClickListener(view -> {
            toggleAddPurchaseButton(false);

            PurchaseAdapter.SpecialPurchase newPurchase = new PurchaseAdapter.SpecialPurchase(false, true);
            purchases.put(newPurchase.toString(), newPurchase);
            showPurchases(false);
        });

        setTitle(getString(R.string.insert_issue__confirm, WhatTheDuck.getSelectedIssue()));
    }

    void toggleAddPurchaseButton(Boolean toggle) {
        instance.findViewById(R.id.addpurchase).setEnabled(toggle);
    }

    void showPurchases(final Boolean checkNoPurchaseItem) {
        final RecyclerView rv = this.findViewById(R.id.purchase_list);
        rv.setAdapter(new PurchaseAdapter(this, purchases));
        rv.setLayoutManager(new LinearLayoutManager(this));

        final MultipleCustomCheckboxes purchaseDateCheckboxes = new MultipleCustomCheckboxes(
            new WeakReference<>(rv),
            view ->
                selectedPurchaseHash = view.getContentDescription().toString(),
            view ->
                selectedPurchaseHash = null
        );
        rv.post(() -> {
            purchaseDateCheckboxes.initClickEvents();
            if (checkNoPurchaseItem) {
                purchaseDateCheckboxes.checkInitialCheckbox(checkbox -> checkbox.getContentDescription().toString().contains(PurchaseAdapter.SpecialPurchase.class.getSimpleName()));
            }
        });
    }
}
