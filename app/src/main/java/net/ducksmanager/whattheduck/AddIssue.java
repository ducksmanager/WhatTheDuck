package net.ducksmanager.whattheduck;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.ducksmanager.apigateway.DmServer;
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueDetails;
import net.ducksmanager.persistence.models.composite.IssueListToUpdate;
import net.ducksmanager.persistence.models.dm.Purchase;

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
import retrofit2.Response;

import static net.ducksmanager.whattheduck.WhatTheDuck.appDB;
import static net.ducksmanager.whattheduck.WhatTheDuck.info;
import static net.ducksmanager.whattheduck.WhatTheDuck.selectedIssue;
import static net.ducksmanager.whattheduck.WhatTheDuck.selectedPublication;

public class AddIssue extends AppCompatActivity implements View.OnClickListener {

    private List<Purchase> purchases;

    private static final Calendar myCalendar = Calendar.getInstance();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.addissue);
        downloadPurchaseList();
    }

    private void downloadPurchaseList() {
        DmServer.api.getUserPurchases().enqueue(new DmServer.Callback<List<Purchase>>("getpurchases", this, true) {
            @Override
            public void onSuccessfulResponse(Response<List<Purchase>> response) {
                appDB.purchaseDao().deleteAll();
                appDB.purchaseDao().insertList(response.body());
                setData();
            }
        });
    }

    private void setData() {
        appDB.purchaseDao().findAll().observe(this, purchases -> {
            this.purchases = new ArrayList<>();
            this.purchases.add(new PurchaseAdapter.NoPurchase());
            this.purchases.addAll(purchases);
            this.show();
        });
    }

    private void show() {
        findViewById(R.id.noCondition).setOnClickListener(this);
        findViewById(R.id.badCondition).setOnClickListener(this);
        findViewById(R.id.notSoGoodCondition).setOnClickListener(this);
        findViewById(R.id.goodCondition).setOnClickListener(this);

        showPurchases();
        findViewById(R.id.noCondition).performClick();
        PurchaseAdapter.selectedItem = purchases.get(0);

        findViewById(R.id.addissue_ok).setOnClickListener(view -> {
            String dmCondition;
            RadioGroup r = findViewById(R.id.condition);
            switch(r.getCheckedRadioButtonId()) {
                case R.id.badCondition: dmCondition = InducksIssueWithUserIssueDetails.BAD_CONDITION; break;
                case R.id.notSoGoodCondition: dmCondition = InducksIssueWithUserIssueDetails.NOTSOGOOD_CONDITION; break;
                case R.id.goodCondition: dmCondition = InducksIssueWithUserIssueDetails.GOOD_CONDITION; break;
                default: dmCondition = InducksIssueWithUserIssueDetails.NO_CONDITION; break;
            }

            IssueListToUpdate issueListToUpdate = new IssueListToUpdate(
                selectedPublication,
                Collections.singletonList(selectedIssue),
                dmCondition,
                PurchaseAdapter.selectedItem instanceof PurchaseAdapter.NoPurchase
                    ? null
                    : PurchaseAdapter.selectedItem.getId()
            );

            DmServer.api.createUserIssues(issueListToUpdate).enqueue(new DmServer.Callback<Object>("addissue", AddIssue.this, true) {
                @Override
                public void onSuccessfulResponse(Response<Object> response) {
                    finish();
                    info(new WeakReference<>(AddIssue.this), R.string.confirmation_message__issue_inserted, Toast.LENGTH_SHORT);
                    startActivity(new Intent(AddIssue.this, Login.class));
                }
            });
        });

        findViewById(R.id.addissue_cancel).setOnClickListener(view ->
            finish()
        );

        findViewById(R.id.addpurchase).setOnClickListener(view -> {
            toggleAddPurchaseButton(false);
            showNewPurchase();
        });

        ((TextView)findViewById(R.id.addIssueTitle)).setText(getString(R.string.insert_issue__confirm, selectedIssue));
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

            Purchase newPurchase = new Purchase(purchaseDateNew.getText().toString(), purchaseTitleNew.getText().toString());
            DmServer.api.createUserPurchase(newPurchase).enqueue(new DmServer.Callback<Void>("createPurchase", this, true) {
                @Override
                public void onSuccessfulResponse(Response<Void> response) {
                    downloadPurchaseList();
                    AddIssue.this.toggleAddPurchaseButton(true);
                    AddIssue.this.showPurchases();
                    newPurchaseSection.setVisibility(View.GONE);
                }
            });

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
