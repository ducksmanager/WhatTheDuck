package net.ducksmanager.adapter

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.activity.IssueList
import net.ducksmanager.activity.ItemList
import net.ducksmanager.activity.ItemList.Companion.isCoaList
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToResourceId
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.R.drawable.*
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedIssues
import java.lang.ref.WeakReference

class IssueAdapter internal constructor(
        itemList: ItemList<InducksIssueWithUserIssueAndScore>
) : ItemAdapter<InducksIssueWithUserIssueAndScore>(itemList, R.layout.row) {

    private var firstRangeIssueNumber: String? = null

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override fun shouldShowFilter() = filteredItems.size > ItemList.MIN_ITEM_NUMBER_FOR_FILTER && IssueList.viewType == IssueList.ViewType.LIST_VIEW

    override val onClickListener = View.OnClickListener { view: View ->
        if (isCoaList()) {
            val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
            val clickedIssue = getItem(position)
            toggleSelectedIssue(clickedIssue.issue.inducksIssueNumber)
            this.notifyDataSetChanged()
        }
    }

    override var onLongClickListener = View.OnLongClickListener { view: View ->
        if (isCoaList()) {
            val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
            firstRangeIssueNumber = if (firstRangeIssueNumber != null) {
                var hasReachedFirstRangeIssueNumber = false
                for (filteredItem in filteredItems) {
                    if (hasReachedFirstRangeIssueNumber && filteredItem.userIssue == null) {
                        toggleSelectedIssue(filteredItem.issue.inducksIssueNumber)
                        if (filteredItem.issue.inducksIssueNumber === getItem(position).issue.inducksIssueNumber) {
                            break
                        }
                    }
                    if (filteredItem.issue.inducksIssueNumber == firstRangeIssueNumber) {
                        hasReachedFirstRangeIssueNumber = true
                    }
                }
                null
            } else {
                val currentIssueNumber = getItem(position).issue.inducksIssueNumber
                toggleSelectedIssue(currentIssueNumber)
                WhatTheDuck.info(WeakReference(originActivity), R.string.long_tap_to_end_issue_range_selection, Toast.LENGTH_SHORT)
                currentIssueNumber
            }
            this.notifyDataSetChanged()
        }
        true
    }

    private fun toggleSelectedIssue(issueNumber: String) {
        if (selectedIssues.contains(issueNumber)) {
            selectedIssues.remove(issueNumber)
        } else selectedIssues.add(issueNumber)
    }

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserIssueAndScore>.ViewHolder(v!!)

    override fun getCheckboxImageResource(i: InducksIssueWithUserIssueAndScore, activity: Activity): Int? {
        return when {
            !isCoaList() -> null
            selectedIssues.contains(i.issue.inducksIssueNumber) -> ic_checkbox_checked
            else -> ic_checkbox
        }
    }

    override fun getPrefixImageResource(i: InducksIssueWithUserIssueAndScore, activity: Activity): Int {
        return if (resourceToInflate == R.layout.row && i.userIssue != null) {
            issueConditionToResourceId(i.userIssue.condition) ?: android.R.color.transparent
        } else {
            android.R.color.transparent
        }
    }

    override fun getSuffixImageResource(i: InducksIssueWithUserIssueAndScore): Int? {
        return when {
            i.userPurchase != null -> ic_clock
            else -> null
        }
    }

    override fun getDescriptionText(i: InducksIssueWithUserIssueAndScore): String = i.issue.title

    override fun getSuffixText(i: InducksIssueWithUserIssueAndScore): String? {
        return when {
            i.userPurchase != null -> i.userPurchase.date
            else -> null
        }
    }

    override fun getIdentifier(i: InducksIssueWithUserIssueAndScore): String = i.issue.inducksIssueNumber

    override fun getText(i: InducksIssueWithUserIssueAndScore): String = i.issue.inducksIssueNumber

    override fun getComparatorText(i: InducksIssueWithUserIssueAndScore): String = getText(i)

    override fun isPossessed(item: InducksIssueWithUserIssueAndScore): Boolean = item.userIssue != null
}