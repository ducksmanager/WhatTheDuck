package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import net.ducksmanager.util.MultipleCustomCheckboxes;
import net.igenius.customcheckbox.CustomCheckBox;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

public class AddIssue extends Activity {

    static AddIssue instance;
    static ArrayList<PurchaseAdapter.Purchase> purchases;

    private static String selectedCondition = null;
    private static Integer selectedPurchaseId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        setContentView(R.layout.addissue);
        purchases = WhatTheDuck.userCollection.getPurchaseListWithEmptyItem();

        show();
    }

    private void show() {
        MultipleCustomCheckboxes conditionCheckboxes = new MultipleCustomCheckboxes(
            new WeakReference<>(this.findViewById(R.id.condition_selector)),
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedCondition = view.getContentDescription().toString();
                    ((TextView) AddIssue.this.findViewById(R.id.addissue_condition_text)).setText(selectedCondition);
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

        ListView lv = this.findViewById(R.id.purchase_list);

        final MultipleCustomCheckboxes purchaseDateCheckboxes = new MultipleCustomCheckboxes(
            new WeakReference<>((View)lv),
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
        lv.setAdapter(new PurchaseAdapter(this, purchases));
        lv.post(new Runnable() {
            @Override
            public void run() {
                purchaseDateCheckboxes.initClickEvents();
                purchaseDateCheckboxes.checkInitialCheckbox(new MultipleCustomCheckboxes.CheckboxFilter() {
                    @Override
                    public boolean isMatched(CustomCheckBox checkbox) {
                        return checkbox.getContentDescription() == null;
                    }
                });
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((CustomCheckBox)view.findViewById(R.id.purchasecheck)).setChecked(true);
            }
        });

        this.findViewById(R.id.addissue_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                new net.ducksmanager.retrievetasks.AddIssue(
                    new WeakReference<Activity>(AddIssue.this),
                    WhatTheDuck.getSelectedPublication(),
                    new Issue(
                        WhatTheDuck.getSelectedIssue(),
                        DMcondition,
                        WhatTheDuck.userCollection.getPurchase(selectedPurchaseId)
                    )
                ).execute();
            }
        });

        this.findViewById(R.id.addissue_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddIssue.this.finish();
            }
        });

        this.findViewById(R.id.addpurchase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAddPurchaseButton(false);

                purchases.add(0, new PurchaseAdapter.Purchase(null, new Date(), "", true));
                updatePurchases();
            }
        });

        setTitle(getString(R.string.insert_issue__confirm, WhatTheDuck.getSelectedIssue()));
    }

    void toggleAddPurchaseButton(Boolean toggle) {
        instance.findViewById(R.id.addpurchase).setEnabled(toggle);
    }

    void updatePurchases() {
        ListView listView = this.findViewById(R.id.purchase_list);
        PurchaseAdapter adapter = (PurchaseAdapter) listView.getAdapter();

        adapter.setItems(purchases);
        adapter.updateFilteredList("");
        adapter.notifyDataSetChanged();
        listView.setSelectionAfterHeaderView();
    }
}
