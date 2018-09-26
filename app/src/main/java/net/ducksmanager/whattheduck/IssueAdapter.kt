package net.ducksmanager.whattheduck

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast

import net.ducksmanager.retrievetasks.GetPurchaseList

import java.lang.ref.WeakReference
import java.util.ArrayList

class IssueAdapter internal constructor(itemList: ItemList<*>, items: ArrayList<Issue>) : ItemAdapter<Issue>(itemList, R.layout.row, items) {

    protected override val onClickListener: View.OnClickListener
        get() = { view ->
            val position = (view.getParent() as RecyclerView).getChildLayoutPosition(view)
            if (ItemList.type == Collection.CollectionType.COA.toString()) {
                val selectedIssue = this@IssueAdapter.getItem(position)
                if (WhatTheDuck.userCollection.getIssue(WhatTheDuck.selectedCountry, WhatTheDuck.selectedPublication, selectedIssue.issueNumber) != null) {
                    WhatTheDuck.wtd!!.info(WeakReference(this@IssueAdapter.originActivity), R.string.input_error__issue_already_possessed, Toast.LENGTH_SHORT)
                } else {
                    WhatTheDuck.wtd!!.toggleProgressbarLoading(WeakReference(this@IssueAdapter.originActivity), true)
                    WhatTheDuck.selectedIssue = selectedIssue.issueNumber
                    GetPurchaseList.initAndShowAddIssue(this@IssueAdapter.originActivity)
                }
            }
        }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    internal inner class ViewHolder(v: View) : ItemAdapter.ViewHolder(v)

    override fun isHighlighted(i: Issue): Boolean {
        return i.issueCondition != null
    }

    override fun getPrefixImageResource(i: Issue, activity: Activity): Int? {
        return if (this.resourceToInflate == R.layout.row && i.issueCondition != null) {
            Issue.issueConditionToResourceId(i.issueCondition)
        } else {
            android.R.color.transparent
        }
    }

    override fun getSuffixImageResource(i: Issue): Int? {
        return if (this.resourceToInflate == R.layout.row && i.purchase != null) {
            R.drawable.ic_clock
        } else {
            null
        }
    }

    override fun getSuffixText(i: Issue): String? {
        return if (this.resourceToInflate == R.layout.row && i.purchase != null) {
            PurchaseAdapter.dateFormat.format(i.purchase!!.purchaseDate)
        } else {
            null
        }
    }

    override fun getIdentifier(i: Issue): String {
        return i.issueNumber
    }

    override fun getText(i: Issue): String {
        return i.issueNumber
    }

    override fun getComparatorText(i: Issue): String? {
        return getText(i)
    }
}
