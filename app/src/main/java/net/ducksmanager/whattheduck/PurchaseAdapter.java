package net.ducksmanager.whattheduck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import net.ducksmanager.persistence.models.dm.Purchase;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class PurchaseAdapter extends RecyclerView.Adapter<PurchaseAdapter.ViewHolder> {
    static Purchase selectedItem = null;
    private final List<Purchase> items;
    private final Context context;

    static class NoPurchase extends Purchase {
        NoPurchase() {}
    }

    PurchaseAdapter(Context context, List<Purchase> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public void onBindViewHolder(PurchaseAdapter.ViewHolder purchaseHolder, final int i) {

        Purchase purchase = items.get(i);
        if (purchase != null) {
            purchaseHolder.purchaseCheck.setChecked(purchase == selectedItem);
            boolean isNoPurchase = purchase instanceof NoPurchase;

            purchaseHolder.purchaseDate.setVisibility(!isNoPurchase ? View.VISIBLE : View.GONE);
            purchaseHolder.purchaseName.setVisibility(!isNoPurchase ? View.VISIBLE : View.GONE);
            purchaseHolder.purchaseCheck.setVisibility(View.VISIBLE);
            purchaseHolder.noPurchaseTitle.setVisibility(isNoPurchase ? View.VISIBLE : View.GONE);

            purchaseHolder.purchaseCheck.setContentDescription(purchase.toString());

            if (!isNoPurchase) {
                purchaseHolder.purchaseDate.setText(purchase.getDate());
                purchaseHolder.purchaseName.setText(purchase.getDescription());
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
