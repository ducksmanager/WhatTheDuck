package net.ducksmanager.retrievetasks

import android.app.Activity
import android.content.Intent
import android.view.View

import com.koushikdutta.async.future.FutureCallback

import net.ducksmanager.util.CoverFlowActivity
import net.ducksmanager.whattheduck.IssueWithFullUrl
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.RetrieveTask
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuckApplication

import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.lang.ref.WeakReference
import java.util.ArrayList

import net.ducksmanager.whattheduck.WhatTheDuck.trackEvent

class CoverSearch(originActivityRef: WeakReference<Activity>, coverPicture: File) : RetrieveTask("/cover-id/search", false, futureCallback, uploadFileName, coverPicture) {

    init {
        CoverSearch.originActivityRef = originActivityRef
    }

    override fun onPreExecute() {
        super.onPreExecute()
        println("Starting cover search : " + System.currentTimeMillis())
        trackEvent("coversearch/start")
    }

    override fun onPostExecute(response: String) {
        super.onPostExecute(response)
        trackEvent("coversearch/finish")
        println("Ending cover search : " + System.currentTimeMillis())
    }

    companion object {

        var originActivityRef: WeakReference<Activity>
        val uploadTempDir = "Pictures"
        val uploadFileName = "wtd_jpg"

        private val futureCallback = FutureCallback<String> { e, result ->
            try {
                val originActivity = originActivityRef.get()

                originActivity.findViewById<View>(R.id.addToCollectionWrapper).visibility = View.VISIBLE
                originActivity.findViewById<View>(R.id.progressBar).visibility = View.GONE
                if (e != null)
                    throw e

                println("Success")
                val `object`: JSONObject
                try {
                    `object` = JSONObject(result)
                    if (`object`.has("issues")) {
                        val issues = `object`.getJSONObject("issues")
                        val resultCollection = ArrayList<IssueWithFullUrl>()
                        val issueIterator = issues.keys()
                        while (issueIterator.hasNext()) {
                            val issueCode = issueIterator.next()
                            val issue = issues.get(issueCode) as JSONObject
                            resultCollection.add(IssueWithFullUrl(
                                    issue.get("countrycode") as String,
                                    issue.get("publicationcode") as String,
                                    issue.get("publicationtitle") as String,
                                    issue.get("issuenumber") as String,
                                    WhatTheDuckApplication.config!!.getProperty(WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL) + "/cover-id/download/" + issue.get("coverid"))
                            )
                        }
                        val i = Intent(originActivity, CoverFlowActivity::class.java)
                        i.putExtra("resultCollection", resultCollection)
                        originActivity.startActivity(i)
                    } else {
                        if (`object`.has("type")) {
                            when (`object`.get("type") as String) {
                                "SEARCH_RESULTS" -> WhatTheDuck.wtd!!.alert(originActivityRef, R.string.add_cover_no_results)
                                else -> WhatTheDuck.wtd!!.alert(originActivityRef, `object`.get("type") as String)
                            }
                        }
                    }
                } catch (jsone: JSONException) {
                    if (result.contains("exceeds your upload")) {
                        WhatTheDuck.wtd!!.alert(originActivityRef, R.string.add_cover_error_file_too_big)
                    } else {
                        WhatTheDuck.wtd!!.alert(originActivityRef, R.string.internal_error)
                        jsone.printStackTrace()
                    }
                }

            } catch (ex: Exception) {
                WhatTheDuck.wtd!!.alert(originActivityRef, ex.message)
            }
        }
    }
}