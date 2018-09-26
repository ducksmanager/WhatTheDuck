package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View

import java.util.ArrayList

class PublicationAdapter internal constructor(itemList: ItemList<*>, items: ArrayList<Publication>) : ItemAdapter<PublicationAdapter.Publication>(itemList, R.layout.row, items) {

    protected override val onClickListener: View.OnClickListener
        get() = { view ->
            val position = (view.getParent() as RecyclerView).getChildLayoutPosition(view)
            val selectedPublication = this@PublicationAdapter.getItem(position)
            WhatTheDuck.selectedPublication = selectedPublication.publicationCode

            val i = Intent(originActivity, IssueList::class.java)
            originActivity.startActivity(i)
        }

    internal class Publication(val publicationCode: String, val publicationTitle: String)

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    internal inner class ViewHolder(v: View) : ItemAdapter.ViewHolder(v)

    override fun isHighlighted(i: Publication): Boolean {
        return WhatTheDuck.userCollection.hasPublication(WhatTheDuck.selectedCountry, i.publicationCode)
    }

    override fun getPrefixImageResource(i: Publication, activity: Activity): Int? {
        return null
    }


    override fun getSuffixImageResource(i: Publication): Int? {
        return null
    }

    override fun getIdentifier(i: Publication): String {
        return i.publicationCode
    }

    override fun getSuffixText(i: Publication): String? {
        return null
    }

    override fun getText(i: Publication): String {
        return i.publicationTitle
    }

    override fun getComparatorText(i: Publication): String? {
        return getText(i)
    }
}
