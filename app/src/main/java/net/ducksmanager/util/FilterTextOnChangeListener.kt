package net.ducksmanager.util

import android.text.Editable
import android.text.TextWatcher
import net.ducksmanager.adapter.ItemAdapter

class FilterTextOnChangeListener(private val itemAdapter: ItemAdapter<*>) : TextWatcher {
    override fun afterTextChanged(s: Editable) {}
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        itemAdapter.updateFilteredList(s.toString())
        itemAdapter.notifyDataSetChanged()
    }

}