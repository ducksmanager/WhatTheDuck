package net.ducksmanager.persistence.models.composite

import androidx.room.Embedded
import net.ducksmanager.persistence.models.coa.InducksIssueWithCoverUrl
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.whattheduck.R

class InducksIssueWithUserData(
    @Embedded
    val issue: InducksIssueWithCoverUrl,

    @Embedded
    val userIssue: Issue?,

    @Embedded
    val userPurchase: Purchase? = null,

    val suggestionScore: Int = 0
) {
    companion object {
        const val MISSING = "non_possede"
        const val BAD_CONDITION = "mauvais"
        const val NOTSOGOOD_CONDITION = "moyen"
        const val GOOD_CONDITION = "bon"
        const val NO_CONDITION = "indefini"

        val ALL_CONDITIONS: List<String>
            get() {
                return listOf(NO_CONDITION, BAD_CONDITION, NOTSOGOOD_CONDITION, GOOD_CONDITION)
            }

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

        fun issueConditionToStringId(issueCondition: String?): Int {
            return when (issueCondition) {
                MISSING -> R.string.condition_missing
                BAD_CONDITION -> R.string.condition_bad
                NOTSOGOOD_CONDITION -> R.string.condition_notsogood
                GOOD_CONDITION -> R.string.condition_good
                else -> R.string.condition_none
            }
        }
    }
}