package net.ducksmanager.util;

import android.text.Editable;
import android.text.TextWatcher;

import net.ducksmanager.whattheduck.ItemAdapter;

public class FilterTextOnChangeListener implements TextWatcher {

    private final ItemAdapter itemAdapter;

    public FilterTextOnChangeListener(ItemAdapter itemAdapter) {
        this.itemAdapter = itemAdapter;
    }

    public void afterTextChanged(Editable s) { }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        itemAdapter.updateFilteredList(s.toString());
        itemAdapter.notifyDataSetChanged();
    }
}
