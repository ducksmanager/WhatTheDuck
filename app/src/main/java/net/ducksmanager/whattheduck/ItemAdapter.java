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
import java.util.Comparator;
import java.util.Locale;

abstract class ItemAdapter<Item> extends ArrayAdapter<Item> {

    int resourceToInflate;
    private final ArrayList<Item> items;
    private ArrayList<Item> filteredItems;

    ItemAdapter(Context context, int resource, ArrayList<Item> items) {
        super(context, resource, items);
        this.items = items;
        processItems(resource, items);
    }

    ItemAdapter(Context context, int resource, int textResource, ArrayList<Item> items) {
        super(context, resource, textResource, items);
        this.items = items;
        processItems(resource, items);
    }

    private void processItems(int resourceToInflate, ArrayList<Item> items) {
        this.resourceToInflate = resourceToInflate;
        Collections.sort(this.items, getComparator());
        this.filteredItems = new ArrayList<>(this.items);
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

    TextView getTitleTextView(View mainView) {
        return mainView.findViewById(R.id.itemtitle);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(resourceToInflate, null);
        }
        Item i = getItem(position);
        if (i != null) {
            getTitleTextView(v).setText(getText(i));
            getTitleTextView(v).setTypeface(null, isHighlighted(i) ? Typeface.BOLD : Typeface.NORMAL);

            ImageView prefixImage = v.findViewById(R.id.prefiximage);
            if (prefixImage != null) {
                Integer imageResource = getPrefixImageResource(i, (Activity) this.getContext());
                if (imageResource == null) {
                    prefixImage.setVisibility(View.GONE);
                } else {
                    prefixImage.setVisibility(View.VISIBLE);
                    prefixImage.setImageResource(imageResource);
                }
            }

            ImageView suffixImage = v.findViewById(R.id.suffiximage);
            if (suffixImage != null) {
                Integer imageResource = getSuffixImageResource(i);
                if (imageResource == null) {
                    suffixImage.setVisibility(View.GONE);
                } else {
                    suffixImage.setVisibility(View.VISIBLE);
                    suffixImage.setImageResource(imageResource);
                }
            }

            TextView suffixText = v.findViewById(R.id.suffixtext);
            if (suffixText != null) {
                String text = getSuffixText(i);
                if (text == null) {
                    suffixText.setVisibility(View.GONE);
                } else {
                    suffixText.setVisibility(View.VISIBLE);
                    suffixText.setText(text);
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

    protected abstract Integer getPrefixImageResource(Item i, Activity activity);

    protected abstract Integer getSuffixImageResource(Item i);

    protected abstract String getSuffixText(Item i);

    protected abstract String getComparatorText(Item i);

    protected abstract String getText(Item i);

    public ArrayList<Item> getItems() {
        return items;
    }
}