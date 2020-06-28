package net.ducksmanager.adapter

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.activity.ItemList
import net.ducksmanager.activity.PublicationList
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck

class CountryAdapter internal constructor(
    itemList: ItemList<InducksCountryNameWithPossession>,
    items: List<InducksCountryNameWithPossession>
) : ItemAdapter<InducksCountryNameWithPossession>(itemList, R.layout.row, items) {

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override fun isPossessed(item: InducksCountryNameWithPossession): Boolean {
        return item.isPossessed
    }

    override val onClickListener = View.OnClickListener { view: View ->
        val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
        val selectedCountry = getItem(position)
        WhatTheDuck.selectedCountry = selectedCountry.country.countryCode
        val i = Intent(originActivity, PublicationList::class.java)
        originActivity.startActivity(i)
    }

    inner class ViewHolder(v: View?) : ItemAdapter<InducksCountryNameWithPossession>.ViewHolder(v!!)

    override fun getPrefixImageResource(i: InducksCountryNameWithPossession, activity: Activity): Int? {
        val uri = "@drawable/flags_" + i.country.countryCode
        var imageResource = activity.resources.getIdentifier(uri, null, activity.packageName)
        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown
        }
        return imageResource
    }

    override fun getSuffixImageResource(i: InducksCountryNameWithPossession): Int? {
        return null
    }

    override fun getSuffixText(i: InducksCountryNameWithPossession): String? {
        return null
    }

    override fun getText(i: InducksCountryNameWithPossession): String? {
        return i.country.countryName
    }

    override fun getIdentifier(i: InducksCountryNameWithPossession): String? {
        return i.country.countryCode
    }

    override fun getComparatorText(i: InducksCountryNameWithPossession): String? {
        return getText(i)
    }
}