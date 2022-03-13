package net.ducksmanager.adapter

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.activity.ItemList
import net.ducksmanager.activity.ItemList.Companion.isCoaList
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData.Companion.issueConditionToResourceId
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.R.drawable.*
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedIssues
import java.lang.ref.WeakReference

class IssueAdapter internal constructor(
        itemList: ItemList<InducksIssueWithUserData>
) : ItemAdapter<InducksIssueWithUserData>(itemList, R.layout.row) {

    private var firstRangeIssueNumber: String? = null

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override fun hasEnoughItemsForFilter() = filteredItems.size > ItemList.MIN_ITEM_NUMBER_FOR_FILTER

    override val onClickListener = View.OnClickListener { view: View ->
        if (isCoaList()) {
            val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
            val clickedIssue = getItem(position)
            toggleSelectedIssue(clickedIssue.issue.inducksIssueNumber)
            this.notifyItemChanged(position)
        }
    }

    override fun onBindViewHolder(
        holder: ItemAdapter<InducksIssueWithUserData>.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        holder.itemView.background = null
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
                this.notifyItemRangeChanged(filteredItems.indexOfFirst { it.issue.inducksIssueNumber == firstRangeIssueNumber }, selectedIssues.size)
                null
            } else {
                val currentIssueNumber = getItem(position).issue.inducksIssueNumber
                toggleSelectedIssue(currentIssueNumber)
                WhatTheDuck.info(WeakReference(originActivity), R.string.long_tap_to_end_issue_range_selection, Toast.LENGTH_SHORT)
                this.notifyItemChanged(filteredItems.indexOfFirst { it.issue.inducksIssueNumber == currentIssueNumber })
                currentIssueNumber
            }
        }
        true
    }

    private fun toggleSelectedIssue(issueNumber: String) {
        val futureSelectedIssues = selectedIssues.toMutableList()
        if (futureSelectedIssues.contains(issueNumber)) {
            futureSelectedIssues.removeAll { it == issueNumber }
        } else futureSelectedIssues.addAll(filteredItems
            .filter { it.issue.inducksIssueNumber == issueNumber }
            .map { it.issue.inducksIssueNumber }
        )
        if (futureSelectedIssues.toSet().size > 1 && futureSelectedIssues.size > futureSelectedIssues.toSet().size) {
            WhatTheDuck.info(WeakReference(originActivity), R.string.error__cannot_edit_issues_with_and_without_copies_at_same_time, Toast.LENGTH_LONG)
        }
        else {
            selectedIssues = futureSelectedIssues.toMutableList()
        }
    }

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserData>.ViewHolder(v!!)

    override fun getCheckboxImageResource(i: InducksIssueWithUserData, activity: Activity): Int? {
        return when {
            !isCoaList() -> null
            selectedIssues.contains(i.issue.inducksIssueNumber) -> ic_checkbox_checked
            else -> ic_checkbox
        }
    }

    override fun getPrefixImageResource(i: InducksIssueWithUserData, activity: Activity): Int {
        return if (resourceToInflate == R.layout.row && i.userIssue != null) {
            issueConditionToResourceId(i.userIssue.condition) ?: android.R.color.transparent
        } else {
            android.R.color.transparent
        }
    }

    override fun getSuffixImageResource(i: InducksIssueWithUserData): Int? {
        return when {
            i.userPurchase != null -> ic_clock
            else -> null
        }
    }

    override fun getDescriptionText(i: InducksIssueWithUserData): String = i.issue.title

    override fun getSuffixText(i: InducksIssueWithUserData): String? {
        return when {
            i.userPurchase != null -> i.userPurchase.date
            else -> null
        }
    }

    override fun getIdentifier(i: InducksIssueWithUserData): String = i.issue.inducksIssueNumber

    override fun getText(i: InducksIssueWithUserData): String = i.issue.inducksIssueNumber

    override fun getComparatorText(i: InducksIssueWithUserData): String = getText(i)

    override fun isPossessed(item: InducksIssueWithUserData): Boolean = item.userIssue != null
}