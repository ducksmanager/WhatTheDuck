package net.ducksmanager.persistence.models.composite

import androidx.room.Embedded
import net.ducksmanager.persistence.models.coa.InducksIssue
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.whattheduck.R

class InducksIssueWithUserIssueAndScore(
    @Embedded
    val issue: InducksIssue,

    @Embedded
    val userIssue: Issue?,

    @Embedded
    val userPurchase: Purchase?,

    val suggestionScore: Int = 0
) {
    companion object {
        const val MISSING = "non_possede"
        const val BAD_CONDITION = "mauvais"
        const val NOTSOGOOD_CONDITION = "moyen"
        const val GOOD_CONDITION = "bon"
        const val NO_CONDITION = "indefini"
        @JvmStatic
        fun issueConditionToResourceId(issueCondition: String?): Int? {
            return if (issueCondition == null) {
                R.drawable.condition_none
            } else when (issueCondition) {
                MISSING -> null
                BAD_CONDITION -> R.drawable.condition_bad
                NOTSOGOOD_CONDITION -> R.drawable.condition_notsogood
                GOOD_CONDITION -> R.drawable.condition_good
                else -> R.drawable.condition_none
            }
        }

        @JvmStatic
        fun issueConditionToStringId(issueCondition: String?): Int {
            return when (issueCondition) {
                MISSING -> R.string.condition_missing
                BAD_CONDITION -> R.string.condition_bad
                NOTSOGOOD_CONDITION -> R.string.condition_notsogood
                GOOD_CONDITION -> R.string.condition_good
                NO_CONDITION -> R.string.condition_none
                else -> -1
            }
        }
    }
}