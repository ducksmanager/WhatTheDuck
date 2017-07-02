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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public abstract class ItemAdapter<Item> extends ArrayAdapter<Item> {

    private final ArrayList<Item> items;

    ItemAdapter(List list, ArrayList<Item> items) {
        super(list, R.layout.row, items);
        this.items = items;
        this.sort(getComparator());
    }

    ArrayList<Item> getFilteredList(String textFilter) {
        ArrayList<Item> filteredItems = new ArrayList<>();
        for (Item item : items)
            if (getText(item).toLowerCase(Locale.FRANCE).contains(textFilter.toLowerCase()))
                filteredItems.add(item);
        return filteredItems;
    }

    private Comparator<? super Item> getComparator() {
        return new Comparator<Item>() {
            @Override
            public int compare(Item item1, Item item2) {
                return getText(item1).compareTo(getText(item2));
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

            if (isHighlighted(i)) {
                itemTitle.setTypeface(null, Typeface.BOLD);
            }

            ImageView imageCondition = (ImageView) v.findViewById(R.id.issuecondition);
            if (imageCondition != null) {
                Integer imageResource = getImageResource(i, (Activity) this.getContext());
                if (imageResource != null) {
                    imageCondition.setImageResource(imageResource);
                }
            }
        }
        return v;
    }

    protected ArrayList<Item> getItems() {
        return this.items;
    }

    protected abstract boolean isHighlighted(Item i);

    protected abstract Integer getImageResource(Item i, Activity activity);

    protected abstract String getText(Item i);
}