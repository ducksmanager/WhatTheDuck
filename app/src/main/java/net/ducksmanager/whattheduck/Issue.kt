package net.ducksmanager.whattheduck


import java.io.Serializable

class Issue : Serializable {

    val issueNumber: String
    var issueCondition = issueConditionStrToIssueCondition(Issue.NO_CONDITION)
        private set
    var purchase: PurchaseAdapter.PurchaseWithDate? = null
        private set

    internal val cleanIssueNumber: String
        get() = issueNumber.replace(" ".toRegex(), "")

    val issueConditionStr: String
        get() = issueConditionToIssueConditionStr(issueCondition)

    enum class IssueCondition {
        BAD_CONDITION, NOTSOGOOD_CONDITION, GOOD_CONDITION, NO_CONDITION
    }

    constructor(issuenumber: String) : super() {
        this.issueNumber = issuenumber
    }

    constructor(issuenumber: String, issueCondition: String, purchase: PurchaseAdapter.PurchaseWithDate) : super() {
        this.issueNumber = issuenumber
        this.issueCondition = issueConditionStrToIssueCondition(issueCondition)
        this.purchase = purchase
    }

    constructor(issuenumber: String, issueCondition: IssueCondition) : super() {
        this.issueNumber = issuenumber
        this.issueCondition = issueCondition
    }

    constructor(issuenumber: String, issueCondition: IssueCondition, purchase: PurchaseAdapter.PurchaseWithDate) : super() {
        this.issueNumber = issuenumber
        this.issueCondition = issueCondition
        this.purchase = purchase
    }

    companion object {
        val BAD_CONDITION = "mauvais"
        val NOTSOGOOD_CONDITION = "moyen"
        val GOOD_CONDITION = "bon"
        val NO_CONDITION = "indefini"

        private fun issueConditionStrToIssueCondition(issueConditionStr: String?): IssueCondition {
            if (issueConditionStr == null || issueConditionStr == NO_CONDITION)
                return IssueCondition.NO_CONDITION
            else if (issueConditionStr == BAD_CONDITION)
                return IssueCondition.BAD_CONDITION
            else if (issueConditionStr == NOTSOGOOD_CONDITION)
                return IssueCondition.NOTSOGOOD_CONDITION
            else if (issueConditionStr == GOOD_CONDITION)
                return IssueCondition.GOOD_CONDITION
            return IssueCondition.NO_CONDITION
        }


        private fun issueConditionToIssueConditionStr(issueCondition: IssueCondition?): String {
            if (issueCondition == null || issueCondition == IssueCondition.NO_CONDITION)
                return NO_CONDITION
            else if (issueCondition == IssueCondition.BAD_CONDITION)
                return BAD_CONDITION
            else if (issueCondition == IssueCondition.NOTSOGOOD_CONDITION)
                return NOTSOGOOD_CONDITION
            else if (issueCondition == IssueCondition.GOOD_CONDITION)
                return GOOD_CONDITION
            return NO_CONDITION
        }

        fun issueConditionToResourceId(issueCondition: IssueCondition?): Int {
            if (issueCondition == null || issueCondition == IssueCondition.NO_CONDITION)
                return R.drawable.condition_none
            else if (issueCondition == IssueCondition.BAD_CONDITION)
                return R.drawable.condition_bad
            else if (issueCondition == IssueCondition.NOTSOGOOD_CONDITION)
                return R.drawable.condition_notsogood
            else if (issueCondition == IssueCondition.GOOD_CONDITION)
                return R.drawable.condition_good
            return R.drawable.condition_none
        }

        fun issueConditionToStringId(issueCondition: IssueCondition): Int {
            if (issueCondition == IssueCondition.BAD_CONDITION)
                return R.string.condition_bad
            else if (issueCondition == IssueCondition.NOTSOGOOD_CONDITION)
                return R.string.condition_notsogood
            else if (issueCondition == IssueCondition.GOOD_CONDITION)
                return R.string.condition_good
            return -1
        }
    }

}
