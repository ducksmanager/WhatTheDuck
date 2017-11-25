package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
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
        super(activity, R.layout.row_purchase, new ArrayList<>(items.values()));
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull final ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        Purchase purchase = getItem(position);
        if (purchase == null) {
            return v;
        }
        Boolean isNoPurchase = purchase.isNoPurchase();
        Boolean isNewPurchase = purchase.isNewPurchase();

        CustomCheckBox purchaseCheck = v.findViewById(R.id.purchasecheck);
        TextView purchaseDate = v.findViewById(R.id.purchasedate);
        TextView purchaseTitle = getTitleTextView(v);
        TextView noPurchaseTitle = v.findViewById(R.id.nopurchase);

        LinearLayout newPurchaseSection = v.findViewById(R.id.newpurchase);
        EditText purchaseDateNew = v.findViewById(R.id.purchasedatenew);
        EditText purchaseTitleNew = v.findViewById(R.id.itemtitlenew);
        Button purchaseCreate = v.findViewById(R.id.createpurchase);
        Button purchaseCreateCancel = v.findViewById(R.id.createpurchasecancel);

        purchaseDate.setVisibility(!isNoPurchase && !isNewPurchase ? View.VISIBLE : View.GONE);
        purchaseTitle.setVisibility(!isNoPurchase && !isNewPurchase ? View.VISIBLE : View.GONE);
        purchaseCheck.setVisibility(isNewPurchase ? View.GONE : View.VISIBLE);
        noPurchaseTitle.setVisibility(isNoPurchase ? View.VISIBLE : View.GONE);
        newPurchaseSection.setVisibility(isNewPurchase ? View.VISIBLE : View.GONE);

        if (isNewPurchase) {
            v.setMinimumHeight(80);
            purchaseDateNew.requestFocus();
            purchaseDateNew.setText(dateFormat.format(new Date()));
            purchaseDateNew.setKeyListener(null);
            final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    ((EditText)parent.findViewById(R.id.purchasedatenew)).setText(dateFormat.format(myCalendar.getTime()));
                }

            };
            purchaseDateNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideKeyboard(v);
                    new DatePickerDialog(PurchaseAdapter.this.getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            purchaseTitleNew.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    parent.findViewById(R.id.itemtitlenew).getBackground().setColorFilter(null);
                    return false;
                }
            });

            purchaseCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View floatingButtonView) {
                    EditText purchaseDateNew = AddIssue.instance.findViewById(R.id.purchasedatenew);
                    EditText purchaseTitleNew = AddIssue.instance.findViewById(R.id.itemtitlenew);
                    if (purchaseTitleNew.getText().toString().equals("")) {
                        purchaseTitleNew.getBackground().setColorFilter(getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                        return;
                    }

                    hideKeyboard(floatingButtonView);

                    new CreatePurchase(new WeakReference<Activity>(AddIssue.instance), purchaseDateNew.getText().toString(), purchaseTitleNew.getText().toString()) {
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
                                    return new WeakReference<Activity>(AddIssue.instance);
                                }
                            }.execute();
                        }
                    }.execute();
                }
            });

            purchaseCreateCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View floatingButtonView) {
                    hideKeyboard(floatingButtonView);

                    for (Purchase purchase : new ArrayList<>(AddIssue.purchases.values())) {
                        if (purchase.isNewPurchase()) {
                            AddIssue.purchases.remove(purchase.toString());
                        }
                    }
                    AddIssue.instance.toggleAddPurchaseButton(true);
                    AddIssue.instance.showPurchases(false);
                }
            });
        }
        else {
            v.setMinimumHeight(40);
            purchaseCheck.setContentDescription(purchase.toString());

            purchaseCheck.setTag(R.id.check_by_user, Boolean.FALSE);
            purchaseCheck.setChecked(purchase.toString().equals(AddIssue.selectedPurchaseHash));
            purchaseCheck.setTag(R.id.check_by_user, null);

            if (!isNoPurchase) {
                purchaseDate.setText(dateFormat.format(((PurchaseWithDate)purchase).getPurchaseDate()));
            }
        }
        return v;
    }

    private static void hideKeyboard(View floatingButtonView) {
        InputMethodManager imm = (InputMethodManager) AddIssue.instance.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(floatingButtonView.getWindowToken(), 0);
    }

    @Override
    protected int getResourceToInflate() {
        return R.layout.row_purchase;
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
    protected Integer getSuffixImageResource(Purchase i, Activity activity) {
        return null;
    }

    @Override
    protected String getSuffixText(Purchase i, Activity activity) {
        return null;
    }

    @Override
    protected String getText(Purchase p) {
        return p instanceof PurchaseWithDate ? ((PurchaseWithDate)p).getPurchaseName() : "";
    }

    protected Comparator<Purchase> getComparator() {
        return new Comparator<Purchase>() {
            @Override
            public int compare(Purchase purchase1, Purchase purchase2) {
                return PurchaseAdapter.this.getComparatorText(purchase2).compareTo(PurchaseAdapter.this.getComparatorText(purchase1));
            }
        };
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
