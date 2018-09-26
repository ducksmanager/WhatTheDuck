package net.ducksmanager.retrievetasks


import android.content.Intent
import android.text.TextUtils
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar

import net.ducksmanager.inducks.coa.CountryListing
import net.ducksmanager.inducks.coa.PublicationListing
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.Collection
import net.ducksmanager.whattheduck.CountryList
import net.ducksmanager.whattheduck.Issue
import net.ducksmanager.whattheduck.ItemList
import net.ducksmanager.whattheduck.PurchaseAdapter
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.RetrieveTask
import net.ducksmanager.whattheduck.WhatTheDuck

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.lang.ref.WeakReference
import java.text.ParseException
import java.util.Date

import net.ducksmanager.whattheduck.WhatTheDuck.trackEvent

class ConnectAndRetrieveList(private val fromUI: Boolean?) : RetrieveTask("", WeakReference<Activity>(WhatTheDuck.wtd)) {

    override fun onPreExecute() {
        WhatTheDuck.userCollection = Collection()
        val wtdActivity = originActivityRef.get() as WhatTheDuck

        trackEvent("retrievecollection/start")

        if (this.fromUI!!) {
            if (Settings.username == null
                    || Settings.username != (wtdActivity.findViewById<View>(R.id.username) as EditText).text.toString()
                    || Settings.encryptedPassword == null) {

                Settings.username = (wtdActivity.findViewById<View>(R.id.username) as EditText).text.toString()
                Settings.setPassword((wtdActivity.findViewById<View>(R.id.password) as EditText).text.toString())
                Settings.rememberCredentials = (wtdActivity.findViewById<View>(R.id.checkBoxRememberCredentials) as CheckBox).isChecked
            }
        }

        if (TextUtils.isEmpty(Settings.username) || TextUtils.isEmpty(Settings.password) && TextUtils.isEmpty(Settings.encryptedPassword)) {
            WhatTheDuck.wtd!!.alert(R.string.input_error,
                    R.string.input_error__empty_credentials)
            val mProgressBar = wtdActivity.findViewById<ProgressBar>(R.id.progressBar)
            mProgressBar.visibility = ProgressBar.INVISIBLE
            cancel(true)
            return
        }

        WhatTheDuck.wtd!!.toggleProgressbarLoading(true)
    }

    override fun onPostExecute(response: String?) {
        trackEvent("retrievecollection/finish")
        super.onPostExecute(response)
        if (super.hasFailed()) {
            return
        }

        val wtdActivity = originActivityRef.get() as WhatTheDuck

        try {
            if (response == null) {
                return
            }

            Settings.saveSettings()

            val `object` = JSONObject(response)
            try {
                if (`object`.has("numeros")) {
                    if (`object`.get("numeros").javaClass == JSONObject::class.java) {
                        val issues = `object`.getJSONObject("numeros")
                        val issueIterator = issues.keys()
                        while (issueIterator.hasNext()) {
                            val countryAndPublication = issueIterator.next()
                            val publicationIssues = issues.getJSONArray(countryAndPublication)
                            for (i in 0 until publicationIssues.length()) {
                                val issueObject = publicationIssues.getJSONObject(i)
                                val issueNumber = issueObject.getString("Numero")
                                val issueCondition = issueObject.getString("Etat")

                                val purchase: PurchaseAdapter.PurchaseWithDate?
                                if (issueObject.isNull("Acquisition")) {
                                    purchase = null
                                } else {
                                    val purchaseObject = issueObject.getJSONObject("Acquisition")
                                    val purchaseId = purchaseObject.getInt("ID_Acquisition")
                                    val purchaseDate = PurchaseAdapter.dateFormat.parse(purchaseObject.getString("Date_Acquisition"))
                                    val purchaseName = purchaseObject.getString("Description_Acquisition")
                                    purchase = PurchaseAdapter.PurchaseWithDate(purchaseId, purchaseDate, purchaseName)
                                }

                                WhatTheDuck.userCollection.addIssue(
                                        countryAndPublication,
                                        Issue(issueNumber, issueCondition, purchase)
                                )
                            }
                        }

                        CountryListing.hasFullList = false

                        CountryListing.addCountries(`object`)
                        PublicationListing.addPublications(`object`)
                    } else { // Empty list
                        CountryListing.hasFullList = false
                    }

                    ItemList.type = Collection.CollectionType.USER.toString()
                    wtdActivity.startActivity(Intent(wtdActivity, CountryList::class.java))
                } else {
                    throw JSONException("")
                }
            } catch (e: JSONException) {
                val issues = `object`.getJSONArray("numeros")
                if (issues.length() > 0)
                    throw e
            } catch (e: ParseException) {
                WhatTheDuck.wtd!!.alert(R.string.internal_error,
                        R.string.internal_error__malformed_list, " : " + e.message)
            } finally {
                wtdActivity.toggleProgressbarLoading(false)
            }
        } catch (e: JSONException) {
            WhatTheDuck.wtd!!.alert(R.string.internal_error,
                    R.string.internal_error__malformed_list, " : " + e.message)
        }

    }
}
