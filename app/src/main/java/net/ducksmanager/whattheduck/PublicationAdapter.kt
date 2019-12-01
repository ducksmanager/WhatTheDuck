package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession

class PublicationAdapter internal constructor(
    itemList: ItemList<InducksPublicationWithPossession>,
    items: List<InducksPublicationWithPossession>
) : ItemAdapter<InducksPublicationWithPossession>(itemList, R.layout.row, items) {
    override fun getViewHolder(v: View?) = ViewHolder(v)

    inner class ViewHolder(v: View?) : ItemAdapter<InducksPublicationWithPossession>.ViewHolder(v!!)

    override fun isPossessed(item: InducksPublicationWithPossession): Boolean {
        return item.isPossessed
    }

    override val onClickListener: View.OnClickListener?
        get() = View.OnClickListener { view: View ->
            val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
            WhatTheDuck.selectedPublication = getItem(position).publication.publicationCode
            originActivity.startActivity(Intent(originActivity, IssueList::class.java))
        }

    override fun getPrefixImageResource(i: InducksPublicationWithPossession, activity: Activity): Int? {
        return null
    }

    override fun getSuffixImageResource(i: InducksPublicationWithPossession): Int? {
        return null
    }

    override fun getIdentifier(i: InducksPublicationWithPossession): String? {
        return i.publication.publicationCode
    }

    override fun getSuffixText(i: InducksPublicationWithPossession): String? {
        return null
    }

    override fun getText(i: InducksPublicationWithPossession): String? {
        return i.publication.title
    }

    override fun getComparatorText(i: InducksPublicationWithPossession): String? {
        return getText(i)
    }
}