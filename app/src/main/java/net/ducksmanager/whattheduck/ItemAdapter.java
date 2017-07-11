package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
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

public abstract class ItemAdapter<Item> extends ArrayAdapter<Item> {

    private final ArrayList<Item> items;

    ItemAdapter(List list, ArrayList<Item> items) {
        super(list, R.layout.row, items);
        this.items = items;
        Collections.sort(this.items, getComparator());
    }

    ArrayList<Item> getFilteredList(String textFilter) {
        ArrayList<Item> filteredItems = new ArrayList<>();
        for (Item item : items)
            if (getText(item).toLowerCase(Locale.FRANCE).contains(textFilter.toLowerCase()))
                filteredItems.add(item);
        return filteredItems;
    }

    private NaturalOrderComparator<Item> getComparator() {
        return new NaturalOrderComparator<Item>() {
            @Override
            public int compare(Item i1, Item i2) {
                return super.compareObject(getText(i1), getText(i2));
            }
        };
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.row, null);
        }
        Item i = items.get(position);
        if (i != null) {
            TextView itemTitle = (TextView) v.findViewById(R.id.itemtitle);
            itemTitle.setText(getText(i));

            itemTitle.setTypeface(null, isHighlighted(i) ? Typeface.BOLD: Typeface.NORMAL);

            ImageView imageCondition = (ImageView) v.findViewById(R.id.issuecondition);
            if (imageCondition != null) {
                Integer imageResource = getImageResource(i, (Activity) this.getContext());
                if (imageResource == null) {
                    imageCondition.setVisibility(View.GONE);
                }
                else {
                    imageCondition.setVisibility(View.VISIBLE);
                    imageCondition.setImageResource(imageResource);
                }
            }
        }
        return v;
    }

    ArrayList<Item> getItems() {
        return this.items;
    }

    protected abstract boolean isHighlighted(Item i);

    protected abstract Integer getImageResource(Item i, Activity activity);

    protected abstract String getText(Item i);
}