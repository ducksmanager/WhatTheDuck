package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.ducksmanager.retrievetasks.CreatePurchase;
import net.ducksmanager.retrievetasks.GetPurchaseList;
import net.ducksmanager.util.MultipleCustomCheckboxes;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class AddIssue extends AppCompatActivity {

    static HashMap<String,PurchaseAdapter.Purchase> purchases;

    private static String selectedCondition = null;
    static String selectedPurchaseHash = null;


    private final Calendar myCalendar = Calendar.getInstance();
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.addissue);

        purchases = WhatTheDuck.userCollection.getPurchasesWithEmptyItem();
        show();
    }

    private void show() {
        MultipleCustomCheckboxes conditionCheckboxes = new MultipleCustomCheckboxes(
            new WeakReference<>(findViewById(R.id.condition_selector)),
            view -> {
                selectedCondition = view.getContentDescription().toString();
                ((TextView) findViewById(R.id.addissue_condition_text)).setText(selectedCondition);
            },
            view -> {
                selectedCondition = null;
                ((TextView) findViewById(R.id.addissue_condition_text)).setText("");
            }

        );
        conditionCheckboxes.initClickEvents();
        conditionCheckboxes.checkInitialCheckbox(checkbox ->
            checkbox.getId() == R.id.nocondition
        );

        showPurchases(true);

        findViewById(R.id.addissue_ok).setOnClickListener(view -> {
            final Context appContext = WhatTheDuck.wtd.getApplicationContext();
            String dmCondition;
            if (selectedCondition.equals(appContext.getString(R.string.condition_none)))
                dmCondition = Issue.NO_CONDITION;
            else if (selectedCondition.equals(appContext.getString(R.string.condition_bad)))
                dmCondition = Issue.BAD_CONDITION;
            else if (selectedCondition.equals(appContext.getString(R.string.condition_notsogood)))
                dmCondition = Issue.NOTSOGOOD_CONDITION;
            else
                dmCondition = Issue.GOOD_CONDITION;

            PurchaseAdapter.Purchase selectedPurchase= purchases.get(selectedPurchaseHash);

            new net.ducksmanager.retrievetasks.AddIssue(
                new WeakReference<>(AddIssue.this),
                WhatTheDuck.getSelectedPublication(),
                new Issue(
                    WhatTheDuck.getSelectedIssue(),
                    dmCondition,
                    selectedPurchase instanceof PurchaseAdapter.PurchaseWithDate
                        ? (PurchaseAdapter.PurchaseWithDate) selectedPurchase
                        : null
                )
            ).execute();
        });

        findViewById(R.id.addissue_cancel).setOnClickListener(view ->
            finish()
        );

        findViewById(R.id.addpurchase).setOnClickListener(view -> {
            toggleAddPurchaseButton(false);
            showNewPurchase();
        });

        setTitle(getString(R.string.insert_issue__confirm, WhatTheDuck.getSelectedIssue()));
    }

    void toggleAddPurchaseButton(Boolean toggle) {
        findViewById(R.id.addpurchase).setEnabled(toggle);
    }
    
    void showNewPurchase() {
        View newPurchaseSection = findViewById(R.id.newpurchase);
        EditText purchaseDateNew = findViewById(R.id.purchasedatenew);
        EditText purchaseTitleNew = findViewById(R.id.purchasetitlenew);
        Button purchaseCreate = findViewById(R.id.createpurchase);
        Button purchaseCreateCancel = findViewById(R.id.createpurchasecancel);

        newPurchaseSection.setVisibility(View.VISIBLE);
        purchaseDateNew.requestFocus();
        purchaseDateNew.setText(dateFormat.format(new Date()));
        purchaseDateNew.setKeyListener(null);

        final DatePickerDialog.OnDateSetListener date = (datePicker, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(year, monthOfYear, dayOfMonth);
            purchaseDateNew.setText(dateFormat.format(myCalendar.getTime()));
        };
        purchaseDateNew.setOnClickListener(v1 -> {
            hideKeyboard(v1);
            new DatePickerDialog(this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        purchaseTitleNew.setOnKeyListener((view, i, keyEvent) -> {
            findViewById(R.id.purchasetitlenew).getBackground().setColorFilter(null);
            return false;
        });

        purchaseCreate.setOnClickListener(floatingButtonView -> {
            if (purchaseDateNew.getText().toString().equals("")) {
                purchaseDateNew.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                return;
            }

            hideKeyboard(floatingButtonView);

            try {
                new CreatePurchase(new WeakReference<>(this), purchaseDateNew.getText().toString(), purchaseTitleNew.getText().toString()) {
                    @Override
                    protected void afterDataHandling() {
                        new GetPurchaseList() {
                            @Override
                            protected void afterDataHandling() {
                                AddIssue.purchases = WhatTheDuck.userCollection.getPurchasesWithEmptyItem();
                                AddIssue.this.toggleAddPurchaseButton(true);
                                AddIssue.this.showPurchases(false);
                                hideKeyboard(floatingButtonView);
                                newPurchaseSection.setVisibility(View.GONE);
                            }

                            @Override
                            protected WeakReference<Activity> getOriginActivity() {
                                return new WeakReference<>(AddIssue.this);
                            }
                        }.execute();
                    }
                }.execute();
            }
            catch(UnsupportedEncodingException e) {
                WhatTheDuck.wtd.alert(new WeakReference<>(AddIssue.this), R.string.internal_error, R.string.internal_error__purchase_creation_failed, "");
            }

        });

        purchaseCreateCancel.setOnClickListener(floatingButtonView -> {
            hideKeyboard(floatingButtonView);

            newPurchaseSection.setVisibility(View.GONE);
            toggleAddPurchaseButton(true);
        });
    }

    private void hideKeyboard(View floatingButtonView) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(floatingButtonView.getWindowToken(), 0);
        }
    }

    void showPurchases(final Boolean checkNoPurchaseItem) {
        final RecyclerView rv = findViewById(R.id.purchase_list);
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
