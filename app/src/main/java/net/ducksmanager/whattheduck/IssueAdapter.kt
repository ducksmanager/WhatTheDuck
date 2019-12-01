package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueDetails
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueDetails.Companion.issueConditionToResourceId
import java.lang.ref.WeakReference

class IssueAdapter internal constructor(
    itemList: ItemList<InducksIssueWithUserIssueDetails>,
    items: List<InducksIssueWithUserIssueDetails>
) : ItemAdapter<InducksIssueWithUserIssueDetails>(itemList, R.layout.row, items) {

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

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserIssueDetails>.ViewHolder(v!!)

    override fun getPrefixImageResource(i: InducksIssueWithUserIssueDetails, activity: Activity): Int? {
        return if (resourceToInflate == R.layout.row && i.userIssue != null) {
            issueConditionToResourceId(i.userIssue.condition)
        } else {
            android.R.color.transparent
        }
    }

    override fun getSuffixImageResource(i: InducksIssueWithUserIssueDetails): Int? {
        return if (i.userPurchase != null) {
            R.drawable.ic_clock
        } else {
            null
        }
    }

    override fun getSuffixText(i: InducksIssueWithUserIssueDetails): String? {
        return if (i.userPurchase != null) {
            i.userPurchase.date
        } else {
            null
        }
    }

    override fun getIdentifier(i: InducksIssueWithUserIssueDetails): String? {
        return i.issue.inducksIssueNumber
    }

    override fun getText(i: InducksIssueWithUserIssueDetails): String? {
        return i.issue.inducksIssueNumber
    }

    override fun getComparatorText(i: InducksIssueWithUserIssueDetails): String? {
        return getText(i)
    }

    override fun isPossessed(item: InducksIssueWithUserIssueDetails): Boolean {
        return item.userIssue != null
    }
}