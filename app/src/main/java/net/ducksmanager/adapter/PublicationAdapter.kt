package net.ducksmanager.adapter

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.activity.IssueList
import net.ducksmanager.activity.ItemList
import net.ducksmanager.activity.ItemList.Companion.COUNTRY_NAME_INTENT_EXTRA
import net.ducksmanager.activity.ItemList.Companion.PUBLICATION_TITLE_INTENT_EXTRA
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck

class PublicationAdapter internal constructor(
    itemList: ItemList<InducksPublicationWithPossession>
) : ItemAdapter<InducksPublicationWithPossession>(itemList, R.layout.row) {
    override fun getViewHolder(v: View?) = ViewHolder(v)

    inner class ViewHolder(v: View?) : ItemAdapter<InducksPublicationWithPossession>.ViewHolder(v!!)

    override fun isPossessed(item: InducksPublicationWithPossession): Boolean = item.isPossessed

    override val onClickListener = View.OnClickListener { view: View ->
        val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
        val selectedPublication = getItem(position).publication
        WhatTheDuck.selectedPublication = selectedPublication.publicationCode

        itemList.goToView(IssueList::class.java)

        val intent = Intent(originActivity, IssueList::class.java)
        intent.putExtra(COUNTRY_NAME_INTENT_EXTRA, itemList.intent.getStringExtra(COUNTRY_NAME_INTENT_EXTRA))
        intent.putExtra(PUBLICATION_TITLE_INTENT_EXTRA, selectedPublication.title)
        originActivity.startActivity(intent)
    }

    override fun getPrefixImageResource(i: InducksPublicationWithPossession, activity: Activity): Int? = null

    override fun getSuffixImageResource(i: InducksPublicationWithPossession): Int? = null

    override fun getDescriptionText(i: InducksPublicationWithPossession) : String? = null

    override fun getIdentifier(i: InducksPublicationWithPossession): String? = i.publication.publicationCode

    override fun getSuffixText(i: InducksPublicationWithPossession): String? = null

    override fun getText(i: InducksPublicationWithPossession): String? = i.publication.title

    override fun getComparatorText(i: InducksPublicationWithPossession): String? = getText(i)
}