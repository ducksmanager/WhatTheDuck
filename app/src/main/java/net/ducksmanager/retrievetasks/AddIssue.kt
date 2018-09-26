package net.ducksmanager.retrievetasks

import android.app.Activity
import android.content.Intent
import android.widget.Toast

import net.ducksmanager.inducks.coa.CountryListing
import net.ducksmanager.inducks.coa.PublicationListing
import net.ducksmanager.whattheduck.Issue
import net.ducksmanager.whattheduck.IssueList
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.RetrieveTask
import net.ducksmanager.whattheduck.WhatTheDuck

import java.lang.ref.WeakReference

import net.ducksmanager.whattheduck.WhatTheDuck.trackEvent

class AddIssue(originActivityRef: WeakReference<Activity>, shortCountryAndPublication: String, selectedIssue: Issue) : RetrieveTask("&ajouter_numero"
        + "&pays_magazine=" + shortCountryAndPublication
        + "&numero=" + selectedIssue.issueNumber
        + "&id_acquisition=" + (if (selectedIssue.purchase == null) "-2" else selectedIssue.purchase!!.id)
        + "&etat=" + selectedIssue.issueConditionStr, originActivityRef) {

    init {
        AddIssue.originActivityRef = originActivityRef
        AddIssue.shortCountryAndPublication = shortCountryAndPublication
        AddIssue.selectedIssue = selectedIssue
    }

    override fun onPreExecute() {
        WhatTheDuck.wtd!!.toggleProgressbarLoading(originActivityRef, true)
        trackEvent("addissue/start")
    }

    override fun onPostExecute(response: String) {
        trackEvent("addissue/finish")
        if (response == "OK") {
            WhatTheDuck.wtd!!.info(originActivityRef, R.string.confirmation_message__issue_inserted, Toast.LENGTH_SHORT)
            WhatTheDuck.userCollection.addIssue(shortCountryAndPublication, selectedIssue)

            updateNamesAndGoToIssueList()
        } else {
            WhatTheDuck.wtd!!.alert(R.string.internal_error, R.string.internal_error__issue_insertion_failed)
        }

        WhatTheDuck.wtd!!.toggleProgressbarLoading(originActivityRef, false)
    }

    companion object {

        private var originActivityRef: WeakReference<Activity>? = null
        private var shortCountryAndPublication: String? = null
        private var selectedIssue: Issue? = null

        private fun updateNamesAndGoToIssueList() {
            val callbackActivity = originActivityRef!!.get()

            val country = shortCountryAndPublication!!.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0]
            if (PublicationListing.hasFullList(country)) {
                callbackActivity.startActivity(Intent(callbackActivity, IssueList::class.java))
            } else {
                PublicationListing(callbackActivity, country) { e, result ->
                    if (CountryListing.hasFullList) {
                        callbackActivity.startActivity(Intent(callbackActivity, IssueList::class.java))
                    } else {
                        CountryListing(callbackActivity
                        ) { e2, result2 -> callbackActivity.startActivity(Intent(callbackActivity, IssueList::class.java)) }.fetch()
                    }
                }.fetch()
            }
        }
    }
}
