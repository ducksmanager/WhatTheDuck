package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.ducksmanager.retrievetasks.CreatePurchase;
import net.ducksmanager.retrievetasks.GetPurchaseList;
import net.igenius.customcheckbox.CustomCheckBox;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PurchaseAdapter extends ItemAdapter<Purchase> {

    private final Calendar myCalendar = Calendar.getInstance();

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    PurchaseAdapter(Activity activity, HashMap<String,Purchase> items) {
        super(activity, R.layout.row_purchase, R.id.itemtitle, new ArrayList<>(items.values()));
    }

    @Override
    protected ItemAdapter<Purchase>.ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return null;
    }

    class ViewHolder extends ItemAdapter.ViewHolder implements View.OnClickListener {
        CustomCheckBox purchaseCheck;
        TextView purchaseDate;
        TextView purchaseTitle;
        TextView noPurchaseTitle;
        LinearLayout newPurchaseSection;
        EditText purchaseDateNew;
        EditText purchaseTitleNew;
        Button purchaseCreate;
        Button purchaseCreateCancel;

        ViewHolder(View v) {
            super(v);

            purchaseCheck = v.findViewById(R.id.purchasecheck);
            purchaseDate = v.findViewById(R.id.purchasedate);
            purchaseTitle = getTitleTextView(v);
            noPurchaseTitle = v.findViewById(R.id.nopurchase);

            newPurchaseSection = v.findViewById(R.id.newpurchase);
            purchaseDateNew = v.findViewById(R.id.purchasedatenew);
            purchaseTitleNew = v.findViewById(R.id.itemtitlenew);
            purchaseCreate = v.findViewById(R.id.createpurchase);
            purchaseCreateCancel = v.findViewById(R.id.createpurchasecancel);
        }

        @Override
        public void onClick(View view) {
            ((CustomCheckBox)view.findViewById(R.id.purchasecheck)).setChecked(true);
        }
    }

    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        v = LayoutInflater.from(context).inflate(resourceToInflate, parent,false);
        viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder purchaseHolder = (ViewHolder) holder;

        Purchase purchase = getItem(position);
        if (purchase != null) {
            Boolean isNoPurchase = purchase.isNoPurchase();
            Boolean isNewPurchase = purchase.isNewPurchase();

            purchaseHolder.purchaseDate.setVisibility(!isNoPurchase && !isNewPurchase ? View.VISIBLE : View.GONE);
            purchaseHolder.purchaseTitle.setVisibility(!isNoPurchase && !isNewPurchase ? View.VISIBLE : View.GONE);
            purchaseHolder.purchaseCheck.setVisibility(isNewPurchase ? View.GONE : View.VISIBLE);
            purchaseHolder.noPurchaseTitle.setVisibility(isNoPurchase ? View.VISIBLE : View.GONE);
            purchaseHolder.newPurchaseSection.setVisibility(isNewPurchase ? View.VISIBLE : View.GONE);

            if (isNewPurchase) {
                v.setMinimumHeight(80);
                purchaseHolder.purchaseDateNew.requestFocus();
                purchaseHolder.purchaseDateNew.setText(dateFormat.format(new Date()));
                purchaseHolder.purchaseDateNew.setKeyListener(null);
                final DatePickerDialog.OnDateSetListener date = (datePicker, year, monthOfYear, dayOfMonth) -> {
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    ((EditText) ((ViewGroup)v.getParent()).findViewById(R.id.purchasedatenew)).setText(dateFormat.format(myCalendar.getTime()));
                };
                purchaseHolder.purchaseDateNew.setOnClickListener(v1 -> {
                    hideKeyboard(v1);
                    new DatePickerDialog(context, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                });

                purchaseHolder.purchaseTitleNew.setOnKeyListener((view, i, keyEvent) -> {
                    ((ViewGroup)v.getParent()).findViewById(R.id.itemtitlenew).getBackground().setColorFilter(null);
                    return false;
                });

                purchaseHolder.purchaseCreate.setOnClickListener(floatingButtonView -> {
                    EditText purchaseDateNew1 = AddIssue.instance.findViewById(R.id.purchasedatenew);
                    EditText purchaseTitleNew1 = AddIssue.instance.findViewById(R.id.itemtitlenew);
                    if (purchaseTitleNew1.getText().toString().equals("")) {
                        purchaseTitleNew1.getBackground().setColorFilter(context.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                        return;
                    }

                    hideKeyboard(floatingButtonView);

                    new CreatePurchase(new WeakReference<>(AddIssue.instance), purchaseDateNew1.getText().toString(), purchaseTitleNew1.getText().toString()) {
                        @Override
                        protected void afterDataHandling() {
                            new GetPurchaseList() {
                                @Override
                                protected void afterDataHandling() {
                                    AddIssue.instance.toggleAddPurchaseButton(true);
                                    AddIssue.purchases = WhatTheDuck.userCollection.getPurchasesWithEmptyItem();
                                    AddIssue.instance.showPurchases(false);
                                }

                                @Override
                                protected WeakReference<Activity> getOriginActivity() {
                                    return new WeakReference<>(AddIssue.instance);
                                }
                            }.execute();
                        }
                    }.execute();
                });

                purchaseHolder.purchaseCreateCancel.setOnClickListener(floatingButtonView -> {
                    hideKeyboard(floatingButtonView);

                    for (Purchase purchase1 : new ArrayList<>(AddIssue.purchases.values())) {
                        if (purchase1.isNewPurchase()) {
                            AddIssue.purchases.remove(purchase1.toString());
                        }
                    }
                    AddIssue.instance.toggleAddPurchaseButton(true);
                    AddIssue.instance.showPurchases(false);
                });
            } else {
                v.setMinimumHeight(40);
                purchaseHolder.purchaseCheck.setContentDescription(purchase.toString());

                purchaseHolder.purchaseCheck.setTag(R.id.check_by_user, Boolean.FALSE);
                purchaseHolder.purchaseCheck.setChecked(purchase.toString().equals(AddIssue.selectedPurchaseHash));
                purchaseHolder.purchaseCheck.setTag(R.id.check_by_user, null);

                if (!isNoPurchase) {
                    purchaseHolder.purchaseDate.setText(dateFormat.format(((PurchaseWithDate) purchase).getPurchaseDate()));
                }
            }
        }
    }

    private static void hideKeyboard(View floatingButtonView) {
        InputMethodManager imm = (InputMethodManager) AddIssue.instance.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(floatingButtonView.getWindowToken(), 0);
    }

    @Override
    protected boolean isHighlighted(Purchase i) {
        return false;
    }

    @Override
    protected Integer getPrefixImageResource(Purchase i, Activity activity) {
        return null;
    }

    @Override
    protected Integer getSuffixImageResource(Purchase i) {
        return null;
    }

    @Override
    protected String getSuffixText(Purchase i) {
        return null;
    }

    @Override
    protected String getText(Purchase p) {
        return p instanceof PurchaseWithDate ? ((PurchaseWithDate)p).getPurchaseName() : "";
    }

    protected Comparator<Purchase> getComparator() {
        return (purchase1, purchase2) ->
            PurchaseAdapter.this.getComparatorText(purchase2)
                .compareTo(PurchaseAdapter.this.getComparatorText(purchase1));
    }

    @Override
    protected String getComparatorText(Purchase i) {
        return i.isNewPurchase()
            ? "_"
            : i.isNoPurchase()
                ? "^"
                : dateFormat.format(((PurchaseWithDate) i).getPurchaseDate());
    }
}
