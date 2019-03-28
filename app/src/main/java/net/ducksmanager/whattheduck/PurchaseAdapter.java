package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import net.ducksmanager.persistence.models.dm.Purchase;
import net.igenius.customcheckbox.CustomCheckBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.NonNull;

public class PurchaseAdapter extends ItemAdapter<Purchase> {

    static class SpecialPurchase extends Purchase {
        SpecialPurchase() {
            this.noPurchase= true;
        }
    }

    public static class PurchaseWithDate extends Purchase {
        private final Integer id;
        private final Date purchaseDate;
        private final String purchaseName;

        public PurchaseWithDate(Integer id, Date purchaseDate, String purchaseName) {
            this.id = id;
            this.purchaseDate = purchaseDate;
            this.purchaseName = purchaseName;
            this.noPurchase = Boolean.FALSE;
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
    }

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    PurchaseAdapter(Activity activity, HashMap<Integer, Purchase> items) {
        super(activity, R.layout.row_purchase, new ArrayList<>(items.values()));
    }

    @Override
    protected ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    protected boolean isPossessed(Purchase purchase) {
        return false;
    }

    @Override
    protected View.OnClickListener getOnClickListener() {
        return null;
    }

    class ViewHolder extends ItemAdapter.ViewHolder implements View.OnClickListener {
        final CustomCheckBox purchaseCheck;
        final TextView purchaseDate;
        final TextView purchaseTitle;
        final TextView noPurchaseTitle;

        ViewHolder(View v) {
            super(v);

            purchaseCheck = v.findViewById(R.id.purchasecheck);
            purchaseDate = v.findViewById(R.id.purchasedate);
            purchaseTitle = getTitleTextView(v);
            noPurchaseTitle = v.findViewById(R.id.nopurchase);
        }

        @Override
        public void onClick(View view) {
            ((CustomCheckBox)view.findViewById(R.id.purchasecheck)).setChecked(true);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        ViewHolder purchaseHolder = (ViewHolder) holder;

        Purchase purchase = getItem(position);
        if (purchase != null) {
            Boolean isNoPurchase = purchase.isNoPurchase();

            purchaseHolder.purchaseDate.setVisibility(!isNoPurchase ? View.VISIBLE : View.GONE);
            purchaseHolder.purchaseTitle.setVisibility(!isNoPurchase ? View.VISIBLE : View.GONE);
            purchaseHolder.purchaseCheck.setVisibility(View.VISIBLE);
            purchaseHolder.noPurchaseTitle.setVisibility(isNoPurchase ? View.VISIBLE : View.GONE);

            v.setMinimumHeight(40);
            purchaseHolder.purchaseCheck.setContentDescription(purchase.toString());

            purchaseHolder.purchaseCheck.setTag(R.id.check_by_user, Boolean.FALSE);
            purchaseHolder.purchaseCheck.setChecked(purchase.toString().equals(AddIssue.selectedPurchaseId));
            purchaseHolder.purchaseCheck.setTag(R.id.check_by_user, null);

            if (!isNoPurchase) {
                purchaseHolder.purchaseDate.setText(dateFormat.format(((PurchaseWithDate) purchase).getPurchaseDate()));
            }
        }
    }

    @Override
    protected boolean isHighlighted(Purchase p) {
        return false;
    }

    @Override
    protected Integer getPrefixImageResource(Purchase p, Activity activity) {
        return null;
    }

    @Override
    protected Integer getSuffixImageResource(Purchase p) {
        return null;
    }

    @Override
    protected String getSuffixText(Purchase p) {
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
    protected String getComparatorText(Purchase p) {
        return p.isNoPurchase()
            ? "^"
            : dateFormat.format(((PurchaseWithDate) p).getPurchaseDate());
    }

    @Override
    protected String getIdentifier(Purchase p) {
        return p.isNoPurchase()
            ? "no"
            : String.valueOf(((PurchaseWithDate) p).getId());
    }
}
