package net.ducksmanager.adapter

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.activity.IssueList
import net.ducksmanager.activity.ItemList
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedPublication

class PublicationAdapter internal constructor(
    itemList: ItemList<InducksPublicationWithPossession>
) : ItemAdapter<InducksPublicationWithPossession>(itemList, R.layout.row) {
    override fun getViewHolder(v: View?) = ViewHolder(v)

    inner class ViewHolder(v: View?) : ItemAdapter<InducksPublicationWithPossession>.ViewHolder(v!!)

    override fun isPossessed(item: InducksPublicationWithPossession): Boolean = item.possessedIssues > 0

    override val onClickListener = View.OnClickListener { view: View ->
        val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
        selectedPublication = getItem(position).publication

        originActivity.startActivity(Intent(originActivity, IssueList::class.java))
    }

    override fun getCheckboxImageResource(i: InducksPublicationWithPossession, activity: Activity): Int? = null

    override fun getPrefixImageResource(i: InducksPublicationWithPossession, activity: Activity): Int? = null

    override fun getSuffixImageResource(i: InducksPublicationWithPossession): Int? = null

    override fun getDescriptionText(i: InducksPublicationWithPossession) : String? = null

    override fun getIdentifier(i: InducksPublicationWithPossession): String = i.publication.publicationCode

    override fun getSuffixText(i: InducksPublicationWithPossession): String =
        when (WhatTheDuck.selectedFilter) {
            WhatTheDuck.applicationContext!!.getString(R.string.filter_to_read) -> String.format(
                WhatTheDuck.applicationContext!!.resources.getQuantityString(
                    R.plurals.issues_to_read,
                    i.possessedIssues,
                ), i.possessedIssues
            )
            else -> String.format("%d/%d", i.possessedIssues, i.referencedIssues)
        }

    override fun getText(i: InducksPublicationWithPossession): String = i.publication.title

    override fun getComparatorText(i: InducksPublicationWithPossession): String = getText(i)

    override fun getLineFill(i: InducksPublicationWithPossession): Float = (i.possessedIssues.toFloat() / i.referencedIssues.toFloat())
}