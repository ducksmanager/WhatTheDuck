package net.ducksmanager.adapter

import android.app.Activity
import android.content.res.Configuration
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.ducksmanager.activity.ItemList
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck

class IssueEdgeAdapter internal constructor(
    itemList: ItemList<InducksIssueWithUserIssueAndScore>,
    private val recyclerView: RecyclerView,
    private val orientation: Int
) : ItemAdapter<InducksIssueWithUserIssueAndScore>(itemList, R.layout.row_edge) {
    private var expectedEdgeHeight: Int? = null

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override val onClickListener: View.OnClickListener? = null

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserIssueAndScore>.ViewHolder(v!!) {
        val edgeImage: ImageView = v!!.findViewById(R.id.edgeimage)
    }

    override fun onBindViewHolder(holder: ItemAdapter<InducksIssueWithUserIssueAndScore>.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val itemHolder = holder as ViewHolder
        val item = getItem(position)

        if (expectedEdgeHeight == null) {
            expectedEdgeHeight = if (orientation == Configuration.ORIENTATION_LANDSCAPE) recyclerView.height else recyclerView.width
        }
        Picasso
            .with(holder.itemView.context)
            .load(getEdgeUrl(item))
            .resize(0, expectedEdgeHeight!!)
            .rotate(if (orientation == Configuration.ORIENTATION_LANDSCAPE) 0f else 90f)
            .into(itemHolder.edgeImage)
    }

    private fun getEdgeUrl(i: InducksIssueWithUserIssueAndScore): String {
        return String.format(
            "%s/edges/%s/gen/%s.%s.png",
            WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_EDGES_URL),
            WhatTheDuck.selectedCountry!!.countryCode,
            WhatTheDuck.selectedPublication!!.publicationCode
                .replaceFirst("[^/]+/".toRegex(), "")
                .replace(" ".toRegex(), ""),
            i.issue.inducksIssueNumber.replace(" ".toRegex(), ""))
    }

    override fun isPossessed(item: InducksIssueWithUserIssueAndScore): Boolean = item.userIssue != null

    override fun getPrefixImageResource(i: InducksIssueWithUserIssueAndScore, activity: Activity): Int? = null

    override fun getSuffixImageResource(i: InducksIssueWithUserIssueAndScore): Int? = null

    override fun getDescriptionText(i: InducksIssueWithUserIssueAndScore) : String? = null

    override fun getSuffixText(i: InducksIssueWithUserIssueAndScore): String? = null

    override fun getIdentifier(i: InducksIssueWithUserIssueAndScore): String? = null

    override fun getText(i: InducksIssueWithUserIssueAndScore): String? = null

    override fun getComparatorText(i: InducksIssueWithUserIssueAndScore): String? = i.issue.inducksIssueNumber

    override fun getLineFill(i: InducksIssueWithUserIssueAndScore): Float = 0.0F
}