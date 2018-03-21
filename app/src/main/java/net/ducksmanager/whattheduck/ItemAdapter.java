package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.ducksmanager.util.NaturalOrderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

abstract class ItemAdapter<Item> extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{

    int resourceToInflate;
    private final ArrayList<Item> items;
    private ArrayList<Item> filteredItems;
    private ViewHolder viewHolder;
    Activity originActivity;
    View v;

    ItemAdapter(Activity activity, int resource, ArrayList<Item> items) {
        this.originActivity = activity;
        this.items = items;
        this.resourceToInflate = resource;
        processItems();
    }

    ItemAdapter(Activity activity, int resource, int textResource, ArrayList<Item> items) {
        this.originActivity = activity;
        this.items = items;
        this.resourceToInflate = resource;
        processItems();
    }

    private void processItems() {
        Collections.sort(this.items, getComparator());
        this.filteredItems = new ArrayList<>(this.items);
    }

    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView prefixImage;
        ImageView suffixImage;
        TextView suffixText;

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
        return new NaturalOrderComparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                return super.compareObject(getComparatorText(i1), getComparatorText(i2));
            }
        };
    }

    @Override
    public void onBindViewHolder(ItemAdapter.ViewHolder holder, int position) {
        Item i = getItem(position);
        if (holder.titleTextView != null) {
            holder.titleTextView.setText(getText(i));
            holder.titleTextView.setTypeface(null, isHighlighted(i) ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (holder.prefixImage != null) {
            Integer imageResource = getPrefixImageResource(i, (Activity) originActivity);
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

    protected abstract String getText(Item i);

    ArrayList<Item> getItems() {
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