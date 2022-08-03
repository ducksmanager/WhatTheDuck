package net.ducksmanager.adapter

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.activity.ItemList
import net.ducksmanager.activity.PublicationList
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.applicationContext
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedCountry
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedFilter
import java.math.RoundingMode

class CountryAdapter internal constructor(
    itemList: ItemList<InducksCountryNameWithPossession>
) : ItemAdapter<InducksCountryNameWithPossession>(itemList, R.layout.row) {

    override val comparator: Comparator<InducksCountryNameWithPossession>? = null

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

    override fun getSuffixText(i: InducksCountryNameWithPossession): String =
        when(selectedFilter) {
            applicationContext!!.getString(R.string.filter_to_read) -> String.format(
                applicationContext!!.resources.getQuantityString(
                    R.plurals.issues_to_read,
                    i.possessedIssues,
                ), i.possessedIssues
            )
            else -> {
                val possessedRatio = 100 * i.possessedIssues.toDouble() / i.referencedIssues
                when {
                    possessedRatio == 0.0 -> ""
                    possessedRatio < 0.1 -> String.format("%d (< %3.1f %%)", i.possessedIssues, 0.1f)
                    else -> String.format("%d (%3.1f %%)", i.possessedIssues, possessedRatio.toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toFloat())
                }
            }
        }

    override fun getText(i: InducksCountryNameWithPossession): String = i.country.countryName

    override fun getIdentifier(i: InducksCountryNameWithPossession): String = i.country.countryCode

    override fun getComparatorText(i: InducksCountryNameWithPossession): String = getText(i)

    override fun getLineFill(i: InducksCountryNameWithPossession): Float = (i.possessedIssues.toFloat() / i.referencedIssues.toFloat())
}