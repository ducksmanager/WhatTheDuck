package net.ducksmanager.persistence.models.composite

import androidx.room.Embedded
import net.ducksmanager.persistence.models.coa.InducksIssue
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.whattheduck.R

class InducksIssueWithUserIssueDetails(
    @Embedded
    val issue: InducksIssue,

    @Embedded
    val userIssue: Issue?,

    @Embedded
    val userPurchase: Purchase?
) {
    companion object {
        const val BAD_CONDITION = "mauvais"
        const val NOTSOGOOD_CONDITION = "moyen"
        const val GOOD_CONDITION = "bon"
        const val NO_CONDITION = "indefini"
        @JvmStatic
        fun issueConditionToResourceId(issueCondition: String?): Int {
            return if (issueCondition == null) {
                R.drawable.condition_none
            } else when (issueCondition) {
                BAD_CONDITION -> R.drawable.condition_bad
                NOTSOGOOD_CONDITION -> R.drawable.condition_notsogood
                GOOD_CONDITION -> R.drawable.condition_good
                else -> R.drawable.condition_none
            }
        }

        @JvmStatic
        fun issueConditionToStringId(issueCondition: String?): Int {
            return when (issueCondition) {
                BAD_CONDITION -> R.string.condition_bad
                NOTSOGOOD_CONDITION -> R.string.condition_notsogood
                GOOD_CONDITION -> R.string.condition_good
                else -> -1
            }
        }
    }
}