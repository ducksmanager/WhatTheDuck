package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.greypanther.natsort.SimpleNaturalComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public abstract class ItemAdapter<Item> extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{

    final int resourceToInflate;
    private List<Item> items;
    private ArrayList<Item> filteredItems;
    private ItemAdapter.ViewHolder viewHolder;
    final Activity originActivity;
    View v;

    ItemAdapter(Activity activity, int resource, List<Item> items) {
        this.originActivity = activity;
        this.items = items;
        this.resourceToInflate = resource;
        processItems();
    }

    private void processItems() {
        Collections.sort(this.items, getComparator());
        this.filteredItems = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        v = LayoutInflater.from(originActivity).inflate(resourceToInflate, parent,false);
        v.setOnClickListener(getOnClickListener());
        viewHolder = getViewHolder(v);
        return viewHolder;
    }

    protected abstract View.OnClickListener getOnClickListener();

    protected abstract ViewHolder getViewHolder(View v);

    Activity getOriginActivity() {
        return originActivity;
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;
        final ImageView prefixImage;
        final ImageView suffixImage;
        final TextView suffixText;

        ViewHolder(View v){
            super(v);
            titleTextView = getTitleTextView(v);
            prefixImage = v.findViewById(R.id.prefiximage);
            suffixImage = v.findViewById(R.id.suffiximage);
            suffixText = v.findViewById(R.id.suffixtext);
        }

        TextView getTitleTextView(View mainView) {
            return mainView.findViewById(R.id.itemtitle);
        }
    }

    void updateFilteredList(String textFilter) {
        filteredItems = new ArrayList<>();
        for (Item item : items)
            if (getText(item).toLowerCase(Locale.FRANCE).contains(textFilter.toLowerCase()))
                filteredItems.add(item);
    }

    Comparator<Item> getComparator() {
        return (i1, i2) ->
            SimpleNaturalComparator.getInstance().compare(
                ItemAdapter.this.getComparatorText(i1),
                ItemAdapter.this.getComparatorText(i2)
            );
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        Item i = getItem(position);
        if (holder.titleTextView != null) {
            holder.titleTextView.setText(getText(i));
            holder.titleTextView.setTag(getIdentifier(i));
            holder.titleTextView.setTypeface(null, isHighlighted(i) ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (holder.prefixImage != null) {
            Integer imageResource = getPrefixImageResource(i, originActivity);
            if (imageResource == null) {
                holder.prefixImage.setVisibility(View.GONE);
            } else {
                holder.prefixImage.setVisibility(View.VISIBLE);
                holder.prefixImage.setImageResource(imageResource);
            }
        }

        if (holder.suffixImage != null) {
            Integer imageResource = getSuffixImageResource(i);
            if (imageResource == null) {
                holder.suffixImage.setVisibility(View.GONE);
            } else {
                holder.suffixImage.setVisibility(View.VISIBLE);
                holder.suffixImage.setImageResource(imageResource);
            }
        }

        if (holder.suffixText != null) {
            String text = getSuffixText(i);
            if (text == null) {
                holder.suffixText.setVisibility(View.GONE);
            } else {
                holder.suffixText.setVisibility(View.VISIBLE);
                holder.suffixText.setText(text);
            }
        }
    }

    protected abstract boolean isHighlighted(Item i);

    protected abstract Integer getPrefixImageResource(Item i, Activity activity);

    protected abstract Integer getSuffixImageResource(Item i);

    protected abstract String getSuffixText(Item i);

    protected abstract String getComparatorText(Item i);

    protected abstract String getIdentifier(Item i);

    protected abstract String getText(Item i);

    List<Item> getItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    Item getItem(int position) {
        return filteredItems.get(position);
    }

}