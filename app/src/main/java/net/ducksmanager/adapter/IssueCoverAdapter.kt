package net.ducksmanager.adapter

import android.app.Activity
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.ducksmanager.activity.ItemList
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck

class IssueCoverAdapter internal constructor(
    itemList: ItemList<InducksIssueWithUserData>,
    private val recyclerView: RecyclerView
) : ItemAdapter<InducksIssueWithUserData>(itemList, R.layout.cell_cover) {
    override fun getViewHolder(v: View?) = ViewHolder(v)

    override val onClickListener: View.OnClickListener? = null

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserData>.ViewHolder(v!!) {
        val coverImage: ImageView = v!!.findViewById(R.id.coverimage)
    }

    override fun onBindViewHolder(holder: ItemAdapter<InducksIssueWithUserData>.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val itemHolder = holder as ViewHolder
        val item = getItem(position)

        Picasso
            .with(holder.itemView.context)
            .load(getEdgeUrl(item))
            .resize(0,
                (recyclerView.measuredWidth / (recyclerView.layoutManager as GridLayoutManager).spanCount)
            )
            .into(itemHolder.coverImage)
    }

    private fun getEdgeUrl(i: InducksIssueWithUserData): String {
        return String.format(
            "%s/%s",
            WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_COVERS_URL),
            i.issue.coverUrl
        )
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