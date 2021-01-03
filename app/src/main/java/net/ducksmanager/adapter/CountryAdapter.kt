package net.ducksmanager.adapter

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.activity.ItemList
import net.ducksmanager.activity.PublicationList
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedCountry
import kotlin.math.roundToInt

class CountryAdapter internal constructor(
    itemList: ItemList<InducksCountryNameWithPossession>
) : ItemAdapter<InducksCountryNameWithPossession>(itemList, R.layout.row) {

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override fun isPossessed(item: InducksCountryNameWithPossession) = item.possessedIssues > 0

    override val onClickListener = View.OnClickListener { view: View ->
        val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
        selectedCountry = getItem(position).country

        originActivity.startActivity(Intent(originActivity, PublicationList::class.java))
    }

    inner class ViewHolder(v: View?) : ItemAdapter<InducksCountryNameWithPossession>.ViewHolder(v!!)

    override fun getCheckboxImageResource(i: InducksCountryNameWithPossession, activity: Activity): Int? = null

    override fun getPrefixImageResource(i: InducksCountryNameWithPossession, activity: Activity): Int {
        val uri = "@drawable/flags_" + i.country.countryCode
        var imageResource = activity.resources.getIdentifier(uri, null, activity.packageName)
        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown
        }
        return imageResource
    }

    override fun getSuffixImageResource(i: InducksCountryNameWithPossession): Int? = null

    override fun getDescriptionText(i: InducksCountryNameWithPossession) : String? = null

    override fun getSuffixText(i: InducksCountryNameWithPossession): String {
        val possessedRatio = 100 * i.possessedIssues.toDouble() / i.referencedIssues
        return when {
            possessedRatio == 0.0 -> ""
            possessedRatio < 1 -> "< 1 %"
            else -> String.format("%d %%", possessedRatio.roundToInt())
        }
    }

    override fun getText(i: InducksCountryNameWithPossession): String = i.country.countryName

    override fun getIdentifier(i: InducksCountryNameWithPossession): String = i.country.countryCode

    override fun getComparatorText(i: InducksCountryNameWithPossession): String = getText(i)

    override fun getLineFill(i: InducksCountryNameWithPossession): Float = (i.possessedIssues.toFloat() / i.referencedIssues.toFloat())
}