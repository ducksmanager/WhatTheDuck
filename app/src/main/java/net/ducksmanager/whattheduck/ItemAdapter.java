package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.ducksmanager.util.NaturalOrderComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

abstract class ItemAdapter<Item> extends ArrayAdapter<Item> {

    private final ArrayList<Item> items;
    private ArrayList<Item> filteredItems;

    ItemAdapter(Context context, ArrayList<Item> items) {
        super(context, R.layout.row, items);
        this.items = items;
        Collections.sort(this.items, getComparator());

        this.filteredItems = new ArrayList<>(items);
    }

    void updateFilteredList(String textFilter) {
        filteredItems = new ArrayList<>();
        for (Item item : items)
            if (getText(item).toLowerCase(Locale.FRANCE).contains(textFilter.toLowerCase()))
                filteredItems.add(item);
    }

    private NaturalOrderComparator<Item> getComparator() {
        return new NaturalOrderComparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                return super.compareObject(getText(i1), getText(i2));
            }
        };
    }

    protected int getResourceToInflate() {
        return R.layout.row;
    }

    protected TextView getTitleTextView(View mainView) {
        return mainView.findViewById(R.id.itemtitle);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(getResourceToInflate(), null);
        }
        Item i = getItem(position);
        if (i != null) {
            getTitleTextView(v).setText(getText(i));
            getTitleTextView(v).setTypeface(null, isHighlighted(i) ? Typeface.BOLD : Typeface.NORMAL);

            ImageView prefixImage = v.findViewById(R.id.prefiximage);
            if (prefixImage != null) {
                Integer imageResource = getImageResource(i, (Activity) this.getContext());
                if (imageResource == null) {
                    prefixImage.setVisibility(View.GONE);
                } else {
                    prefixImage.setVisibility(View.VISIBLE);
                    prefixImage.setImageResource(imageResource);
                }
            }
        }
        return v;
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public Item getItem(int position) {
        return filteredItems.get(position);
    }

    protected abstract boolean isHighlighted(Item i);

    protected abstract Integer getImageResource(Item i, Activity activity);

    protected abstract String getText(Item i);

    public ArrayList<Item> getItems() {
        return items;
    }
}