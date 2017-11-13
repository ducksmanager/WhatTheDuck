package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

public class PurchaseAdapter extends ItemAdapter<PurchaseAdapter.Purchase> {

    public static class Purchase {
        final Date purchaseDate;
        final String purchaseName;

        public Purchase(Date purchaseDate, String purchaseName) {
            this.purchaseDate = purchaseDate;
            this.purchaseName = purchaseName;
        }

        public Date getPurchaseDate() {
            return purchaseDate;
        }

        public String getPurchaseName() {
            return purchaseName;
        }
    }


    public PurchaseAdapter(Activity activity, ArrayList<Purchase> items) {
        super(activity, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        Purchase purchase = getItem(position);
        ((TextView)v.findViewById(R.id.purchasedate)).setText(purchase.getPurchaseDate().toString());

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
        return i.getPurchaseName();
    }
}
