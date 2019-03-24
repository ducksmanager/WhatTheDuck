package net.ducksmanager.whattheduck;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.ducksmanager.apigateway.DmServer;
import net.ducksmanager.persistence.models.composite.IssueListToUpdate;
import net.ducksmanager.persistence.models.dm.Purchase;
import net.ducksmanager.util.MultipleCustomCheckboxes;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Response;

import static net.ducksmanager.whattheduck.WhatTheDuck.trackEvent;

public class AddIssue extends AppCompatActivity {

    private HashMap<Integer, Purchase> data;

    private static String selectedCondition = null;
    static Integer selectedPurchaseId = null;

    private final Calendar myCalendar = Calendar.getInstance();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.addissue);
        downloadPurchaseList();
    }

    protected void downloadPurchaseList() {
        this.findViewById(R.id.progressBar).setVisibility(ProgressBar.VISIBLE);
        DmServer.api.getUserPurchases().enqueue(new DmServer.Callback<List<Purchase>>(this.findViewById(R.id.progressBar)) {
            @Override
            public void onSuccessfulResponse(Response<List<Purchase>> response) {
                WhatTheDuck.appDB.purchaseDao().insertList(response.body());
                setData();
            }
        });
    }

    private void setData() {
        WhatTheDuck.appDB.purchaseDao().findAll().observe(this, purchases -> {
            this.data = new HashMap<>();
            for (Purchase purchase : purchases) {
                this.data.put(purchase.getId(), purchase);
            }
            this.show();
        });
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

            Purchase selectedPurchase = data.get(selectedPurchaseId);

            IssueListToUpdate issueListToUpdate = new IssueListToUpdate(
                WhatTheDuck.getSelectedPublication(),
                Collections.singletonList(WhatTheDuck.getSelectedIssue()),
                dmCondition,
                selectedPurchase == null ? null : selectedPurchase.getId()
            );

            this.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

            trackEvent("addissue/start");
            DmServer.api.createUserIssues(issueListToUpdate).enqueue(new DmServer.Callback<String>() {
                @Override
                public void onSuccessfulResponse(Response<String> response) {
                    trackEvent("addissue/finish");

                    WhatTheDuck.wtd.info(new WeakReference<>(AddIssue.this), R.string.confirmation_message__issue_inserted, Toast.LENGTH_SHORT);
                    WhatTheDuck.fetchCollection(new WeakReference<>(AddIssue.this), IssueList.class);
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

        setTitle(getString(R.string.insert_issue__confirm, WhatTheDuck.getSelectedIssue()));
    }

    private void toggleAddPurchaseButton(Boolean toggle) {
        findViewById(R.id.addpurchase).setEnabled(toggle);
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

            DmServer.api.createUserPurchase(new Purchase(purchaseDateNew.getText().toString(), purchaseTitleNew.getText().toString())).enqueue(new DmServer.Callback<String>(this.findViewById(R.id.progressBar)) {
                @Override
                public void onSuccessfulResponse(Response<String> response) {
                    downloadPurchaseList();
                    AddIssue.this.toggleAddPurchaseButton(true);
                    AddIssue.this.showPurchases(false);
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

    private void showPurchases(final Boolean checkNoPurchaseItem) {
        final RecyclerView rv = findViewById(R.id.purchase_list);
        rv.setAdapter(new PurchaseAdapter(this, data));
        rv.setLayoutManager(new LinearLayoutManager(this));

        final MultipleCustomCheckboxes purchaseDateCheckboxes = new MultipleCustomCheckboxes(
            new WeakReference<>(rv),
            view -> selectedPurchaseId = Integer.parseInt(view.getContentDescription().toString()),
            view -> selectedPurchaseId = null
        );
        rv.post(() -> {
            purchaseDateCheckboxes.initClickEvents();
            if (checkNoPurchaseItem) {
                purchaseDateCheckboxes.checkInitialCheckbox(checkbox -> checkbox.getContentDescription().toString().contains(PurchaseAdapter.SpecialPurchase.class.getSimpleName()));
            }
        });
    }
}
