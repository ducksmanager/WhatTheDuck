package net.ducksmanager.adapter

import android.app.Activity
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.activity.ItemList
import net.ducksmanager.activity.ItemList.Companion.isCoaList
import net.ducksmanager.util.FilterTextOnChangeListener
import net.ducksmanager.whattheduck.R
import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator
import java.util.*
import kotlin.collections.ArrayList

abstract class ItemAdapter<Item> internal constructor(
    val originActivity: Activity,
    val resourceToInflate: Int
) : RecyclerView.Adapter<ItemAdapter<Item>.ViewHolder>() {

    var items = emptyList<Item>()
    protected var filteredItems = mutableListOf<Item>()

    open fun shouldShowFilter() = filteredItems.size > ItemList.MIN_ITEM_NUMBER_FOR_FILTER

    companion object {
        private var filterTextOnChangeListener: FilterTextOnChangeListener? = null
    }

    protected abstract fun isPossessed(item: Item): Boolean

    internal fun setItems(items: List<Item>) {
        this.items = items
        Collections.sort(this.items, comparator)
        filteredItems = ArrayList(items)
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(originActivity).inflate(resourceToInflate, parent, false)
        v.setOnClickListener(onClickListener)
        v.setOnLongClickListener(onLongClickListener)
        return getViewHolder(v)
    }

    protected abstract val onClickListener: View.OnClickListener?

    protected open val onLongClickListener: View.OnLongClickListener? = null

    protected abstract fun getViewHolder(v: View?): ViewHolder

    fun addOrReplaceFilterOnChangeListener(filterEditText: EditText) {
        if (filterTextOnChangeListener != null) {
            filterEditText.removeTextChangedListener(filterTextOnChangeListener)
        }
        filterTextOnChangeListener = FilterTextOnChangeListener(this)
        filterEditText.addTextChangedListener(filterTextOnChangeListener)
        filterEditText.visibility = EditText.VISIBLE
        filterEditText.setText("")
    }

    open inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val titleTextView: TextView? = v.findViewById(R.id.itemtitle)
        val prefixImage: ImageView? = v.findViewById(R.id.prefiximage)
        val descriptionText: TextView? = v.findViewById(R.id.itemdescription)
        val suffixImage: ImageView? = v.findViewById(R.id.suffiximage)
        val suffixText: TextView? = v.findViewById(R.id.suffixtext)
    }

    fun updateFilteredList(textFilter: String) {
        filteredItems = items
            .filter {
                (isCoaList() || isPossessed(it)) &&
                (textFilter == "" || getText(it)!!.toLowerCase(Locale.FRANCE).contains(textFilter.toLowerCase(Locale.getDefault())))
            }
            .toMutableList()
    }

    private val comparator: Comparator<Item>
        get() = Comparator { i1: Item, i2: Item ->
            val text1 = getComparatorText(i1)
            val text2 = getComparatorText(i2)
            when {
                text1 == null -> -1
                text2 == null -> 1
                else -> CaseInsensitiveSimpleNaturalComparator.getInstance<CharSequence>().compare(text1, text2)
            }
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val i = getItem(position)
        if (holder.titleTextView != null) {
            holder.titleTextView.text = getText(i)
            holder.titleTextView.tag = getIdentifier(i)
            holder.titleTextView.setTypeface(null, if (isHighlighted(i)) Typeface.BOLD else Typeface.NORMAL)
        }
        if (holder.prefixImage != null) {
            val imageResource = getPrefixImageResource(i, originActivity)
            if (imageResource == null) {
                holder.prefixImage.visibility = View.GONE
            } else {
                holder.prefixImage.visibility = View.VISIBLE
                holder.prefixImage.setImageResource(imageResource)
            }
        }
        if (holder.descriptionText != null) {
            val text = getDescriptionText(i)
            if (text == null) {
                holder.descriptionText.visibility = View.GONE
            } else {
                holder.descriptionText.visibility = View.VISIBLE
                holder.descriptionText.text = text
            }
        }
        if (holder.suffixImage != null) {
            val imageResource = getSuffixImageResource(i)
            if (imageResource == null) {
                holder.suffixImage.visibility = View.GONE
            } else {
                holder.suffixImage.visibility = View.VISIBLE
                holder.suffixImage.setImageResource(imageResource)
            }
        }
        if (holder.suffixText != null) {
            val text = getSuffixText(i)
            if (text == null) {
                holder.suffixText.visibility = View.GONE
            } else {
                holder.suffixText.visibility = View.VISIBLE
                holder.suffixText.text = text
            }
        }
    }

    protected abstract fun getPrefixImageResource(i: Item, activity: Activity): Int?
    protected abstract fun getSuffixImageResource(i: Item): Int?
    protected abstract fun getDescriptionText(i: Item): String?
    protected abstract fun getSuffixText(i: Item): String?
    protected abstract fun getComparatorText(i: Item): String?
    protected abstract fun getIdentifier(i: Item): String?
    protected abstract fun getText(i: Item): String?

    private fun isHighlighted(i: Item): Boolean = isPossessed(i)

    fun resetItems() {
        items = ArrayList()
        filteredItems = ArrayList()
    }

    override fun getItemCount() = filteredItems.size

    fun getItem(position: Int) = filteredItems[position]
}
