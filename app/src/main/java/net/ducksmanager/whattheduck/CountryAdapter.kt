package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View

import java.util.ArrayList

class CountryAdapter internal constructor(itemList: ItemList<*>, items: ArrayList<Country>) : ItemAdapter<CountryAdapter.Country>(itemList, R.layout.row, items) {

    protected override val onClickListener: View.OnClickListener
        get() = { view ->
            val position = (view.getParent() as RecyclerView).getChildLayoutPosition(view)
            val selectedCountry = this@CountryAdapter.getItem(position)
            WhatTheDuck.selectedCountry = selectedCountry.shortName

            val i = Intent(originActivity, PublicationList::class.java)
            originActivity.startActivity(i)
        }

    internal class Country(val shortName: String, val fullName: String)


    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    protected inner class ViewHolder internal constructor(v: View) : ItemAdapter.ViewHolder(v)

    override fun isHighlighted(i: Country): Boolean {
        return WhatTheDuck.userCollection.hasCountry(i.shortName)
    }

    override fun getPrefixImageResource(i: Country, a: Activity): Int? {
        val uri = "@drawable/flags_" + i.shortName
        var imageResource = a.resources.getIdentifier(uri, null, a.packageName)

        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown
        }
        return imageResource
    }

    override fun getSuffixImageResource(i: Country): Int? {
        return null
    }

    override fun getSuffixText(i: Country): String? {
        return null
    }

    override fun getText(i: Country): String {
        return i.fullName
    }

    override fun getIdentifier(i: Country): String {
        return i.shortName
    }

    override fun getComparatorText(i: Country): String? {
        return getText(i)
    }
}
