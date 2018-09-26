package net.ducksmanager.inducks.coa

import android.app.Activity

import com.koushikdutta.async.future.FutureCallback

import net.ducksmanager.whattheduck.Issue
import net.ducksmanager.whattheduck.WhatTheDuck

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.HashSet

class IssueListing(activity: Activity, private val countryShortName: String, private val publicationCode: String, callback: FutureCallback<*>) : CoaListing(activity, CoaListing.ListType.ISSUE_LIST, callback) {

    protected override val urlSuffix: String
        get() = "/coa/list/issues/$publicationCode"

    @Throws(JSONException::class)
    override fun processData(response: String?) {
        if (response != null) {
            var issues: JSONArray? = null
            try {  // Legacy JSON structure
                val `object` = JSONObject(response)
                issues = `object`.getJSONObject("static").getJSONArray("numeros")
            } catch (e: JSONException) {
                issues = JSONArray(response)
            } finally {
                for (i in 0 until issues!!.length()) {
                    val issue = issues!!.get(i) as String
                    WhatTheDuck.coaCollection.addIssue(countryShortName, publicationCode, Issue(issue))
                }
                fullListPublications.add(publicationCode)
            }
        }
    }

    companion object {

        private val fullListPublications = HashSet<String>()

        fun hasFullList(publicationName: String): Boolean {
            return fullListPublications.contains(publicationName)
        }
    }
}
