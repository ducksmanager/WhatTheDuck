package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.res.Configuration
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView

import com.squareup.picasso.Picasso

import java.util.ArrayList

class IssueEdgeAdapter internal constructor(itemList: ItemList<*>, items: ArrayList<Issue>, private val recyclerView: RecyclerView, private val orientation: Int) : ItemAdapter<Issue>(itemList, R.layout.row_edge, items) {
    private var expectedEdgeHeight: Int? = null

    protected override val onClickListener: View.OnClickListener?
        get() = null

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    inner class ViewHolder internal constructor(v: View) : ItemAdapter.ViewHolder(v) {
        internal val edgeImage: ImageView

        init {
            edgeImage = v.findViewById(R.id.edgeimage)
        }
    }

    override fun onBindViewHolder(holder: ItemAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val itemHolder = holder as ViewHolder

        val i = getItem(position)
        if (i != null) {
            if (expectedEdgeHeight == null) {
                expectedEdgeHeight = if (orientation == Configuration.ORIENTATION_LANDSCAPE) recyclerView.height else recyclerView.width
            }

            Picasso
                    .with((holder as ViewHolder).itemView.getContext())
                    .load(getEdgeUrl(i))
                    .resize(0, expectedEdgeHeight!!)
                    .rotate(if (orientation == Configuration.ORIENTATION_LANDSCAPE) 0 else 90f)
                    .into(itemHolder.edgeImage)
        }
    }

    private fun getEdgeUrl(i: Issue): String {
        return String.format(
                "%s/edges/%s/gen/%s.%s.png",
                WhatTheDuckApplication.config!!.getProperty(WhatTheDuckApplication.CONFIG_KEY_EDGES_URL),
                WhatTheDuck.selectedCountry,
                WhatTheDuck.selectedPublication!!
                        .replaceFirst("[^/]+/".toRegex(), "")
                        .replace(" ".toRegex(), ""),
                i.cleanIssueNumber)
    }

    override fun isHighlighted(i: Issue): Boolean {
        return false
    }

    override fun getPrefixImageResource(i: Issue, activity: Activity): Int? {
        return null
    }

    override fun getSuffixImageResource(i: Issue): Int? {
        return null
    }

    override fun getSuffixText(i: Issue): String? {
        return null
    }

    override fun getIdentifier(i: Issue): String? {
        return null
    }

    override fun getText(i: Issue): String? {
        return null
    }

    override fun getComparatorText(i: Issue): String? {
        return i.issueNumber
    }
}
