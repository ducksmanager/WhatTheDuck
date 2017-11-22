package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.igenius.customcheckbox.CustomCheckBox;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class PurchaseAdapter extends ItemAdapter<PurchaseAdapter.Purchase> {

    private final Calendar myCalendar = Calendar.getInstance();

    static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static class Purchase {
        Integer id = null;
        final Date purchaseDate;
        final String purchaseName;
        Boolean isNewPurchase = Boolean.FALSE;

        Purchase(Integer id, Date purchaseDate, String purchaseName) {
            this.id = id;
            this.purchaseDate = purchaseDate;
            this.purchaseName = purchaseName;
        }

        Purchase(Integer id, Date purchaseDate, String purchaseName, Boolean isNewPurchase) {
            this(id,purchaseDate,purchaseName);
            this.isNewPurchase = isNewPurchase;
        }

        public Integer getId() {
            return id;
        }

        Date getPurchaseDate() {
            return purchaseDate;
        }

        String getPurchaseName() {
            return purchaseName;
        }

        Boolean getIsNewPurchase() {
            return isNewPurchase;
        }
    }


    PurchaseAdapter(Activity activity, ArrayList<Purchase> items) {
        super(activity, R.layout.row_purchase, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        CustomCheckBox purchaseCheck = v.findViewById(R.id.purchasecheck);
        purchaseCheck.setOnCheckedChangeListener(AddIssue.purchaseDateCheckboxes.onCheckedListener);

        Purchase purchase = getItem(position);
        Boolean isNoPurchase = purchase == null;
        Boolean isNewPurchase = purchase != null && purchase.getIsNewPurchase();

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
            purchaseDateNew.setText(dateFormat.format(purchase.getPurchaseDate()));
            purchaseDateNew.setKeyListener(null);
            final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    ((EditText)AddIssue.dialogViewRef.get().findViewById(R.id.purchasedatenew))
                        .setText(dateFormat.format(myCalendar.getTime()));
                }

            };
            purchaseDateNew.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(PurchaseAdapter.this.getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            purchaseTitleNew.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    AddIssue.dialogViewRef.get().findViewById(R.id.itemtitlenew).getBackground().setColorFilter(null);
                    return false;
                }
            });

            purchaseCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View floatingButtonView) {
                    floatingButtonView.setEnabled(true);

                    EditText purchaseDateNew = AddIssue.dialogViewRef.get().findViewById(R.id.purchasedatenew);
                    EditText purchaseTitleNew = AddIssue.dialogViewRef.get().findViewById(R.id.itemtitlenew);
                    if (purchaseTitleNew.getText().toString().equals("")) {
                        purchaseTitleNew.getBackground().setColorFilter(getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                        return;
                    }

                    new CreatePurchase(AddIssue.originActivityRef, purchaseDateNew.getText().toString(), purchaseTitleNew.getText().toString()) {
                        @Override
                        protected void afterDataHandling() {
                            new GetPurchaseList() {
                                @Override
                                protected void afterDataHandling() {
                                    AddIssue.toggleAddPurchaseButton(true);
                                    AddIssue.purchases = WhatTheDuck.userCollection.getPurchaseListWithEmptyItem();
                                    AddIssue.updatePurchases();
                                }

                                @Override
                                protected WeakReference<Activity> getOriginActivity() {
                                    return AddIssue.originActivityRef;
                                }
                            }.execute();
                        }
                    }.execute();
                }
            });

            purchaseCreateCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View floatingButtonView) {
                    floatingButtonView.setEnabled(true);

                    AddIssue.purchases.remove(0);
                    AddIssue.toggleAddPurchaseButton(true);
                    AddIssue.updatePurchases();
                }
            });

            purchaseTitleNew.setText(purchase.getPurchaseName());
        }
        else {
            v.setMinimumHeight(40);
            if (isNoPurchase) {
                purchaseCheck.setContentDescription(null);
            }
            else {
                purchaseCheck.setContentDescription("" + purchase.getId());
                purchaseDate.setText(dateFormat.format(purchase.getPurchaseDate()));
            }
        }
        return v;
    }

    void setItems(ArrayList<Purchase> items) {
        this.items = items;
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
    protected Integer getImageResource(Purchase i, Activity activity) {
        return null;
    }

    @Override
    protected String getText(Purchase i) {
        return i == null
            ? ""
            : i.getPurchaseName();
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
        return i == null ? "_" : dateFormat.format(i.getPurchaseDate());
    }
}
