package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.res.Configuration
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueDetails

class IssueEdgeAdapter internal constructor(
    itemList: ItemList<InducksIssueWithUserIssueDetails>,
    items: List<InducksIssueWithUserIssueDetails>,
    private val recyclerView: RecyclerView,
    private val orientation: Int
) : ItemAdapter<InducksIssueWithUserIssueDetails>(itemList, R.layout.row_edge, items) {
    private var expectedEdgeHeight: Int? = null

    override fun getViewHolder(v: View?) = ViewHolder(v)


    override val onClickListener: View.OnClickListener?
        get() = null

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserIssueDetails>.ViewHolder(v!!) {
        val edgeImage: ImageView = v!!.findViewById(R.id.edgeimage)

    }

    override fun onBindViewHolder(holder: ItemAdapter<InducksIssueWithUserIssueDetails>.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val itemHolder = holder as ViewHolder
        val item = getItem(position)

        if (expectedEdgeHeight == null) {
            expectedEdgeHeight = if (orientation == Configuration.ORIENTATION_LANDSCAPE) recyclerView.height else recyclerView.width
        }
        Picasso
            .with(holder.itemView.getContext())
            .load(getEdgeUrl(item))
            .resize(0, expectedEdgeHeight!!)
            .rotate(if (orientation == Configuration.ORIENTATION_LANDSCAPE) 0f else 90f)
            .into(itemHolder.edgeImage)
    }

    private fun getEdgeUrl(i: InducksIssueWithUserIssueDetails): String {
        return String.format(
            "%s/edges/%s/gen/%s.%s.png",
            WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_EDGES_URL),
            WhatTheDuck.selectedCountry,
            WhatTheDuck.selectedPublication!!
                .replaceFirst("[^/]+/".toRegex(), "")
                .replace(" ".toRegex(), ""),
            i.issue.inducksIssueNumber.replace(" ".toRegex(), ""))
    }

    override fun isPossessed(item: InducksIssueWithUserIssueDetails): Boolean {
        return item.userIssue != null
    }

    override fun getPrefixImageResource(i: InducksIssueWithUserIssueDetails, activity: Activity): Int? {
        return null
    }

    override fun getSuffixImageResource(i: InducksIssueWithUserIssueDetails): Int? {
        return null
    }

    override fun getSuffixText(i: InducksIssueWithUserIssueDetails): String? {
        return null
    }

    override fun getIdentifier(i: InducksIssueWithUserIssueDetails): String? {
        return null
    }

    override fun getText(i: InducksIssueWithUserIssueDetails): String? {
        return null
    }

    override fun getComparatorText(i: InducksIssueWithUserIssueDetails): String? {
        return i.issue.inducksIssueNumber
    }

}