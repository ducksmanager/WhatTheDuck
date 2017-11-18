package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PurchaseAdapter extends ItemAdapter<PurchaseAdapter.Purchase> {

    public static class Purchase {
        Integer id = null;
        final Date purchaseDate;
        final String purchaseName;

        public Purchase(Integer id, Date purchaseDate, String purchaseName) {
            this.id = id;
            this.purchaseDate = purchaseDate;
            this.purchaseName = purchaseName;
        }

        public Integer getId() {
            return id;
        }

        public Date getPurchaseDate() {
            return purchaseDate;
        }

        public String getPurchaseName() {
            return purchaseName;
        }
    }


    public PurchaseAdapter(Activity activity, ArrayList<Purchase> items) {
        super(activity, R.layout.row_purchase, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        Purchase purchase = getItem(position);
        Boolean isNoPurchase = purchase == null;

        TextView purchaseDate = v.findViewById(R.id.purchasedate);
        purchaseDate.setVisibility(isNoPurchase ? View.GONE : View.VISIBLE);
        purchaseDate.setText(isNoPurchase
            ? "" :
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(purchase.getPurchaseDate())
        );
        getTitleTextView(v).setTypeface(null, isNoPurchase ? Typeface.ITALIC : Typeface.NORMAL);

        return v;
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
}
