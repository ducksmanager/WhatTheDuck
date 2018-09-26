package net.ducksmanager.retrievetasks

import android.app.Activity
import android.content.Intent

import net.ducksmanager.whattheduck.PurchaseAdapter
import net.ducksmanager.whattheduck.RetrieveTask
import net.ducksmanager.whattheduck.WhatTheDuck

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.lang.ref.WeakReference
import java.text.ParseException
import java.util.HashMap

import net.ducksmanager.whattheduck.WhatTheDuck.trackEvent

abstract class GetPurchaseList protected constructor(originActivityRef: WeakReference<Activity>) : RetrieveTask("&get_achats=true", originActivityRef) {

    protected abstract val originActivity: WeakReference<Activity>

    override fun onPreExecute() {
        WhatTheDuck.wtd!!.toggleProgressbarLoading(this.originActivity, true)
        trackEvent("getpurchases/start")
    }

    override fun onPostExecute(response: String?) {
        trackEvent("getpurchases/finish")
        super.onPostExecute(response)
        if (super.hasFailed()) {
            return
        }

        try {
            if (response == null) {
                return
            }

            val `object` = JSONObject(response)
            if (`object`.has("achats")) {
                val purchases = HashMap<Int, PurchaseAdapter.Purchase>()

                val purchaseObjects = `object`.getJSONArray("achats")
                for (i in 0 until purchaseObjects.length()) {
                    val purchaseObject = purchaseObjects.get(i) as JSONObject
                    try {
                        val purchaseId = Integer.parseInt(purchaseObject.get("ID_Acquisition") as String)
                        purchases[purchaseId] = PurchaseAdapter.PurchaseWithDate(
                                purchaseId,
                                PurchaseAdapter.dateFormat.parse(purchaseObject.get("Date") as String),
                                purchaseObject.get("Description") as String
                        )
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }

                }
                WhatTheDuck.userCollection.purchases = purchases
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        afterDataHandling()

        WhatTheDuck.wtd!!.toggleProgressbarLoading(this.originActivity, false)
    }

    protected abstract fun afterDataHandling()

    companion object {

        fun initAndShowAddIssue(originActivity: Activity) {
            object : GetPurchaseList(WeakReference(originActivity)) {

                override val originActivity: WeakReference<Activity>
                    get() = WeakReference(originActivity)

                override fun afterDataHandling() {
                    val i = Intent(originActivity, net.ducksmanager.whattheduck.AddIssue::class.java)
                    originActivity.startActivity(i)
                }
            }.execute()
        }
    }
}
