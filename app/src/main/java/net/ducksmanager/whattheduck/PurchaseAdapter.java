package net.ducksmanager.whattheduck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {
    static Purchase selectedItem = null;
    private List<Purchase> items;
    private Context context;

    public abstract static class Purchase {
        Boolean noPurchase;
        Boolean isNoPurchase() {
            return noPurchase;
        }
    }

    static class NoPurchase extends Purchase {
        NoPurchase() {
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

    PurchaseAdapter(Context context, List<Purchase> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onBindViewHolder(PurchaseAdapter.ViewHolder purchaseHolder, final int i) {

        Purchase purchase = items.get(i);
        if (purchase != null) {
            purchaseHolder.purchaseCheck.setChecked(purchase == selectedItem);
            Boolean isNoPurchase = purchase.isNoPurchase();

            purchaseHolder.purchaseDate.setVisibility(!isNoPurchase ? View.VISIBLE : View.GONE);
            purchaseHolder.purchaseName.setVisibility(!isNoPurchase ? View.VISIBLE : View.GONE);
            purchaseHolder.purchaseCheck.setVisibility(View.VISIBLE);
            purchaseHolder.noPurchaseTitle.setVisibility(isNoPurchase ? View.VISIBLE : View.GONE);

            purchaseHolder.purchaseCheck.setContentDescription(purchase.toString());

            if (!isNoPurchase) {
                purchaseHolder.purchaseDate.setText(dateFormat.format(((PurchaseWithDate) purchase).getPurchaseDate()));
                purchaseHolder.purchaseName.setText(((PurchaseWithDate) purchase).getPurchaseName());
            }
        }
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.row_purchase, viewGroup, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final RadioButton purchaseCheck;
        final TextView purchaseDate;
        final TextView purchaseName;
        final TextView noPurchaseTitle;

        ViewHolder(View v) {
            super(v);

            purchaseCheck = v.findViewById(R.id.purchasecheck);
            purchaseDate = v.findViewById(R.id.purchasedate);
            purchaseName = v.findViewById(R.id.purchasename);
            noPurchaseTitle = v.findViewById(R.id.nopurchase);

            View.OnClickListener clickListener = v1 -> {
                selectedItem = items.get(getAdapterPosition());
                notifyItemRangeChanged(0, items.size());
            };
            itemView.setOnClickListener(clickListener);
            purchaseCheck.setOnClickListener(clickListener);
        }
    }
}
