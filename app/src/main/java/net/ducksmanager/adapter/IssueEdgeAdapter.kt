package net.ducksmanager.adapter

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.ducksmanager.activity.IssueList
import net.ducksmanager.activity.ItemList
import net.ducksmanager.activity.SendEdgePhoto
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData
import net.ducksmanager.persistence.models.dm.IssuePopularity
import net.ducksmanager.persistence.models.edge.Edge
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck

class IssueEdgeAdapter internal constructor(
    itemList: ItemList<InducksIssueWithUserData>,
    private val recyclerView: RecyclerView,
    private val orientation: Int
) : ItemAdapter<InducksIssueWithUserData>(itemList, R.layout.row_edge) {
    private var expectedEdgeHeight: Int? = null

    companion object {
        var existingEdges: List<Edge> = listOf()
    }
    var issuePopularities: List<IssuePopularity> = listOf()

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override val onClickListener: View.OnClickListener? = null

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserData>.ViewHolder(v!!) {
        val edgeImage: ImageView = v!!.findViewById(R.id.edgeimage)
    }

    override fun onBindViewHolder(holder: ItemAdapter<InducksIssueWithUserData>.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val itemHolder = holder as ViewHolder
        val item = getItem(position)

        if (expectedEdgeHeight == null) {
            expectedEdgeHeight = if (orientation == Configuration.ORIENTATION_LANDSCAPE) recyclerView.height else recyclerView.width
        }
        if (!existingEdges
                .filter { it.publicationcode == WhatTheDuck.selectedPublication!!.publicationCode }
                .map { it.issuenumber }
                .contains(item.issue.inducksIssueNumber.replace(" ", ""))
        ) {
            (originActivity as IssueList).binding.suggestionMessage.visibility = View.VISIBLE
            itemHolder.edgeImage.setOnClickListener {
                val publicationCode = WhatTheDuck.selectedPublication!!.publicationCode
                val intent = Intent(originActivity, SendEdgePhoto::class.java)
                    .putExtra("publicationCode", publicationCode)
                    .putExtra("issueNumber", item.issue.inducksIssueNumber)
                    .putExtra("popularity", issuePopularities.find { "${it.country}/${it.magazine}" == publicationCode && it.issueNumber == item.issue.inducksIssueNumber }?.popularity)
                originActivity.startActivity(intent)
            }
        }
        Picasso
            .with(holder.itemView.context)
            .load(getEdgeUrl(item))
            .resize(0, expectedEdgeHeight!!)
            .rotate(if (orientation == Configuration.ORIENTATION_LANDSCAPE) 0f else 90f)
            .into(itemHolder.edgeImage)
    }

    private fun getEdgeUrl(i: InducksIssueWithUserData): String {
        return String.format(
            "%s/edges/%s/gen/%s.%s.png",
            WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_EDGES_URL),
            WhatTheDuck.selectedCountry!!.countryCode,
            WhatTheDuck.selectedPublication!!.publicationCode
                .replaceFirst("[^/]+/".toRegex(), "")
                .replace(" ", ""),
            i.issue.inducksIssueNumber.replace(" ", ""))
    }

    override fun isPossessed(item: InducksIssueWithUserData): Boolean = item.userIssue != null

    override fun getCheckboxImageResource(i: InducksIssueWithUserData, activity: Activity): Int? = null

    override fun getPrefixImageResource(i: InducksIssueWithUserData, activity: Activity): Int? = null

    override fun getSuffixImageResource(i: InducksIssueWithUserData): Int? = null

    override fun getDescriptionText(i: InducksIssueWithUserData) : String? = null

    override fun getSuffixText(i: InducksIssueWithUserData): String? = null

    override fun getIdentifier(i: InducksIssueWithUserData): String? = null

    override fun getText(i: InducksIssueWithUserData): String = i.issue.inducksIssueNumber

    override fun getComparatorText(i: InducksIssueWithUserData): String = i.issue.inducksIssueNumber
}