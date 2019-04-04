package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import net.ducksmanager.retrievetasks.CreatePurchase;
import net.ducksmanager.retrievetasks.GetPurchaseList;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AddIssue extends AppCompatActivity implements View.OnClickListener {

    private List<PurchaseAdapter.Purchase> purchases;

    private static final Calendar myCalendar = Calendar.getInstance();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.addissue);
        setPurchases();
        show();
    }

    protected void setPurchases() {
        purchases = new ArrayList<>(Collections.singletonList(new PurchaseAdapter.NoPurchase()));
        purchases.addAll(WhatTheDuck.userCollection.getPurchases());
    }

    private void show() {
        findViewById(R.id.noCondition).setOnClickListener(this);
        findViewById(R.id.noCondition).performClick();
        findViewById(R.id.badCondition).setOnClickListener(this);
        findViewById(R.id.notSoGoodCondition).setOnClickListener(this);
        findViewById(R.id.goodCondition).setOnClickListener(this);

        showPurchases();
        findViewById(R.id.noCondition).performClick();

        findViewById(R.id.addissue_ok).setOnClickListener(view -> {
            String dmCondition;
            RadioGroup r = findViewById(R.id.condition);
            switch(r.getCheckedRadioButtonId()) {
                case R.id.noCondition: dmCondition = Issue.NO_CONDITION; break;
                case R.id.badCondition: dmCondition = Issue.BAD_CONDITION; break;
                case R.id.notSoGoodCondition: dmCondition = Issue.NOTSOGOOD_CONDITION; break;
                case R.id.goodCondition: dmCondition = Issue.GOOD_CONDITION; break;
                default: dmCondition = Issue.NO_CONDITION; break;
            }

            new net.ducksmanager.retrievetasks.AddIssue(
                new WeakReference<>(AddIssue.this),
                WhatTheDuck.getSelectedPublication(),
                new Issue(
                    WhatTheDuck.getSelectedIssue(),
                    dmCondition,
                    PurchaseAdapter.selectedItem instanceof PurchaseAdapter.PurchaseWithDate
                        ? (PurchaseAdapter.PurchaseWithDate) PurchaseAdapter.selectedItem
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

        ((TextView)findViewById(R.id.addIssueTitle)).setText(getString(R.string.insert_issue__confirm, WhatTheDuck.getSelectedIssue()));
    }

    private void toggleAddPurchaseButton(Boolean toggle) {
        findViewById(R.id.addpurchase).setVisibility(toggle ? View.VISIBLE : View.GONE);
    }
    
    private void showNewPurchase() {
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
                        new GetPurchaseList(originActivityRef) {
                            @Override
                            protected void afterDataHandling() {
                                setPurchases();
                                AddIssue.this.toggleAddPurchaseButton(true);
                                AddIssue.this.showPurchases();
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

    private void showPurchases() {
        PurchaseAdapter.selectedItem = purchases.get(0);
        final RecyclerView rv = findViewById(R.id.purchase_list);
        rv.setAdapter(new PurchaseAdapter(this, purchases));
        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.noCondition:
            case R.id.badCondition:
            case R.id.notSoGoodCondition:
            case R.id.goodCondition:
                ((TextView) findViewById(R.id.addissue_condition_text)).setText(view.getContentDescription().toString());
            break;
        }
    }
}
