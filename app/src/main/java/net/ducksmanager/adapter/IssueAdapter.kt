package net.ducksmanager.adapter

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.activity.ItemList
import net.ducksmanager.activity.ItemList.Companion.isCoaList
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToResourceId
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedIssues
import java.lang.ref.WeakReference

class IssueAdapter internal constructor(
    itemList: ItemList<InducksIssueWithUserIssueAndScore>,
    items: List<InducksIssueWithUserIssueAndScore>
) : ItemAdapter<InducksIssueWithUserIssueAndScore>(itemList, R.layout.row, items) {

    private var firstRangeIssueNumber: String? = null

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override val onClickListener = View.OnClickListener { view: View ->
        if (isCoaList()) {
            val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
            val clickedIssue = getItem(position)
            if (clickedIssue.userIssue != null) {
                WhatTheDuck.info(WeakReference(originActivity), R.string.input_error__issue_already_possessed, Toast.LENGTH_SHORT)
            } else {
                toggleSelectedIssue(clickedIssue.issue.inducksIssueNumber)
                this.notifyDataSetChanged()
            }
        }
    }

    override var onLongClickListener = View.OnLongClickListener { view: View ->
        if (isCoaList()) {
            val position = (view.parent as RecyclerView).getChildLayoutPosition(view)
            if (getItem(position).userIssue != null) {
                WhatTheDuck.info(WeakReference(originActivity), R.string.input_error__issue_already_possessed, Toast.LENGTH_SHORT)
            } else {
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
                    WhatTheDuck.info(WeakReference(originActivity), R.string.longTapToEndIssueRangeSelection, Toast.LENGTH_SHORT)
                    currentIssueNumber
                }
                this.notifyDataSetChanged()
            }
        }
        true
    }

    private fun toggleSelectedIssue(issueNumber: String) {
        if (selectedIssues.contains(issueNumber)) {
            selectedIssues.remove(issueNumber)
        } else selectedIssues.add(issueNumber)
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
        return when {
            i.userPurchase != null -> R.drawable.ic_clock
            i.userIssue != null -> null
            else -> when {
                i.suggestionScore > 0 -> R.drawable.ic_fire
                selectedIssues.contains(i.issue.inducksIssueNumber) -> R.drawable.ic_checkbox_checked
                else -> R.drawable.ic_checkbox
            }
        }
    }

    override fun getDescriptionText(i: InducksIssueWithUserIssueAndScore) : String? {
        return i.issue.title
    }

    override fun getSuffixText(i: InducksIssueWithUserIssueAndScore): String? {
        return when {
            i.userPurchase != null -> i.userPurchase.date
            i.userIssue == null && i.suggestionScore > 0 -> i.suggestionScore.toString()
            else -> null
        }
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