package net.ducksmanager.whattheduck

import android.app.Activity
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import net.ducksmanager.util.FilterTextOnChangeListener
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Locale

abstract class ItemAdapter<Item> internal constructor(internal val originActivity: Activity, internal val resourceToInflate: Int, items: List<Item>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {
    internal var items: List<Item>? = null
        private set
    private var filteredItems: ArrayList<Item>? = null
    internal var v: View

    protected abstract val onClickListener: View.OnClickListener

    internal val comparator: Comparator<Item>
        get() = { i1, i2 ->
            val text1 = this@ItemAdapter.getComparatorText(i1)
            val text2 = this@ItemAdapter.getComparatorText(i2)
            if (text1 == null)
                -1
            else if (text2 == null)
                1
            else
                CaseInsensitiveSimpleNaturalComparator.getInstance<CharSequence>().compare(
                        text1,
                        text2
                )
        }

    init {
        this.items = items
        processItems()
    }

    private fun processItems() {
        Collections.sort(this.items, comparator)
        this.filteredItems = ArrayList(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAdapter.ViewHolder {
        v = LayoutInflater.from(originActivity).inflate(resourceToInflate, parent, false)
        v.setOnClickListener(onClickListener)
        return getViewHolder(v)
    }

    protected abstract fun getViewHolder(v: View): ViewHolder

    fun addOrReplaceFilterOnChangeListener(filterEditText: EditText) {
        if (filterTextOnChangeListener != null) {
            filterEditText.removeTextChangedListener(filterTextOnChangeListener)
        }
        filterTextOnChangeListener = FilterTextOnChangeListener(this)

        filterEditText.addTextChangedListener(filterTextOnChangeListener)
        filterEditText.visibility = EditText.VISIBLE
        filterEditText.setText("")
    }

    abstract inner class ViewHolder internal constructor(v: View) : RecyclerView.ViewHolder(v) {
        val titleTextView: TextView?
        internal val prefixImage: ImageView?
        internal val suffixImage: ImageView?
        internal val suffixText: TextView?

        init {
            titleTextView = getTitleTextView(v)
            prefixImage = v.findViewById(R.id.prefiximage)
            suffixImage = v.findViewById(R.id.suffiximage)
            suffixText = v.findViewById(R.id.suffixtext)
        }

        internal fun getTitleTextView(mainView: View): TextView {
            return mainView.findViewById(R.id.itemtitle)
        }
    }

    fun updateFilteredList(textFilter: String) {
        filteredItems = ArrayList()
        for (item in items!!) {
            if (textFilter == "" || getText(item).toLowerCase(Locale.FRANCE).contains(textFilter.toLowerCase()))
                filteredItems!!.add(item)
        }
    }

    override fun onBindViewHolder(holder: ItemAdapter.ViewHolder, position: Int) {
        val i = getItem(position)
        if (holder.titleTextView != null) {
            holder.titleTextView!!.setText(getText(i))
            holder.titleTextView!!.setTag(getIdentifier(i))
            holder.titleTextView!!.setTypeface(null, if (isHighlighted(i)) Typeface.BOLD else Typeface.NORMAL)
        }

        if (holder.prefixImage != null) {
            val imageResource = getPrefixImageResource(i, originActivity)
            if (imageResource == null) {
                holder.prefixImage!!.setVisibility(View.GONE)
            } else {
                holder.prefixImage!!.setVisibility(View.VISIBLE)
                holder.prefixImage!!.setImageResource(imageResource)
            }
        }

        if (holder.suffixImage != null) {
            val imageResource = getSuffixImageResource(i)
            if (imageResource == null) {
                holder.suffixImage!!.setVisibility(View.GONE)
            } else {
                holder.suffixImage!!.setVisibility(View.VISIBLE)
                holder.suffixImage!!.setImageResource(imageResource)
            }
        }

        if (holder.suffixText != null) {
            val text = getSuffixText(i)
            if (text == null) {
                holder.suffixText!!.setVisibility(View.GONE)
            } else {
                holder.suffixText!!.setVisibility(View.VISIBLE)
                holder.suffixText!!.setText(text)
            }
        }
    }

    protected abstract fun isHighlighted(i: Item): Boolean

    protected abstract fun getPrefixImageResource(i: Item, activity: Activity): Int?

    protected abstract fun getSuffixImageResource(i: Item): Int?

    protected abstract fun getSuffixText(i: Item): String?

    protected abstract fun getComparatorText(i: Item): String?

    protected abstract fun getIdentifier(i: Item): String

    protected abstract fun getText(i: Item): String

    internal fun resetItems() {
        items = ArrayList()
        filteredItems = ArrayList()
    }

    override fun getItemCount(): Int {
        return filteredItems!!.size
    }

    internal fun getItem(position: Int): Item {
        return filteredItems!![position]
    }

    companion object {

        private var filterTextOnChangeListener: FilterTextOnChangeListener? = null
    }

}