package net.ducksmanager.adapter

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import net.ducksmanager.activity.ItemList
import net.ducksmanager.persistence.models.coa.InducksIssueWithCoverUrl
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import kotlin.math.sqrt

class IssueCoverAdapter internal constructor(
    itemList: ItemList<InducksIssueWithUserData>,
    private val recyclerView: RecyclerView
) : ItemAdapter<InducksIssueWithUserData>(itemList, R.layout.cell_cover) {

    companion object {
        public fun getCoverUrl(i: InducksIssueWithCoverUrl): String {
            return String.format(
                "%s/%s",
                WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_COVERS_URL),
                i.coverUrl
            )
        }
    }

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override val onClickListener: View.OnClickListener? = null

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserData>.ViewHolder(v!!) {
        val coverImage: ImageView = v!!.findViewById(R.id.coverimage)
        val defaultCover: TextView = v!!.findViewById(R.id.defaultcover)
    }

    override fun onBindViewHolder(holder: ItemAdapter<InducksIssueWithUserData>.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val itemHolder = holder as ViewHolder
        itemHolder.itemView.background = null

        val item = getItem(position)
        val itemWidth = (recyclerView.measuredWidth / (recyclerView.layoutManager as GridLayoutManager).spanCount)

        Picasso
            .with(holder.itemView.context)
            .load(getCoverUrl(item.issue))
            .resize(0, itemWidth)
            .into(itemHolder.coverImage, object: Callback {
                override fun onSuccess() {
                    itemHolder.defaultCover.visibility = View.GONE
                }

                override fun onError() {
                    itemHolder.defaultCover.minHeight = (itemWidth * sqrt(2.0)).toInt()
                    itemHolder.defaultCover.text = recyclerView.context.getString(R.string.issue_no_cover).format(item.issue.inducksIssueNumber)
                    itemHolder.defaultCover.visibility = View.VISIBLE
                }
            })
    }

    override fun isPossessed(item: InducksIssueWithUserData) = item.userIssue != null

    override fun getCheckboxImageResource(i: InducksIssueWithUserData, activity: Activity): Int? = null

    override fun getPrefixImageResource(i: InducksIssueWithUserData, activity: Activity): Int? = null

    override fun getSuffixImageResource(i: InducksIssueWithUserData): Int? = null

    override fun getDescriptionText(i: InducksIssueWithUserData) : String? = null

    override fun getSuffixText(i: InducksIssueWithUserData): String? = null

    override fun getIdentifier(i: InducksIssueWithUserData): String? = null

    override fun getText(i: InducksIssueWithUserData): String = i.issue.inducksIssueNumber

    override fun getComparatorText(i: InducksIssueWithUserData): String = i.issue.inducksIssueNumber
}