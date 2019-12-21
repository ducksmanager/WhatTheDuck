package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToResourceId
import java.lang.ref.WeakReference

class IssueAdapter internal constructor(
    itemList: ItemList<InducksIssueWithUserIssueAndScore>,
    items: List<InducksIssueWithUserIssueAndScore>
) : ItemAdapter<InducksIssueWithUserIssueAndScore>(itemList, R.layout.row, items) {

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override val onClickListener: View.OnClickListener?
        get() = View.OnClickListener { view: View ->
            val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
            if (ItemList.type == WhatTheDuck.CollectionType.COA.toString()) {
                val clickedIssue = getItem(position)
                if (clickedIssue.userIssue != null) {
                    WhatTheDuck.info(WeakReference(originActivity), R.string.input_error__issue_already_possessed, Toast.LENGTH_SHORT)
                } else {
                    WhatTheDuck.selectedIssue = clickedIssue.issue.inducksIssueNumber
                    originActivity.startActivity(Intent(originActivity, AddIssue::class.java))
                }
            }
        }

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserIssueAndScore>.ViewHolder(v!!)

    override fun getPrefixImageResource(i: InducksIssueWithUserIssueAndScore, activity: Activity): Int? {
        return if (resourceToInflate == R.layout.row && i.userIssue != null) {
            issueConditionToResourceId(i.userIssue.condition)
        } else {
            android.R.color.transparent
        }
    }

    override fun getSuffixImageResource(i: InducksIssueWithUserIssueAndScore): Int? {
        return if (i.userPurchase != null) R.drawable.ic_clock else (if (i.userIssue == null && i.suggestionScore > 0) R.drawable.ic_fire else null)
    }

    override fun getSuffixText(i: InducksIssueWithUserIssueAndScore): String? {
        return if (i.userPurchase != null) i.userPurchase.date else (if (i.userIssue == null && i.suggestionScore > 0) i.suggestionScore.toString() else null)
    }

    override fun getIdentifier(i: InducksIssueWithUserIssueAndScore): String? {
        return i.issue.inducksIssueNumber
    }

    override fun getText(i: InducksIssueWithUserIssueAndScore): String? {
        return i.issue.inducksIssueNumber
    }

    override fun getComparatorText(i: InducksIssueWithUserIssueAndScore): String? {
        return getText(i)
    }

    override fun isPossessed(item: InducksIssueWithUserIssueAndScore): Boolean {
        return item.userIssue != null
    }
}