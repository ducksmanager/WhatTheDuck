package net.ducksmanager.retrievetasks


import android.app.Activity

import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.RetrieveTask
import net.ducksmanager.whattheduck.WhatTheDuck

import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.net.URLEncoder

import net.ducksmanager.whattheduck.WhatTheDuck.trackEvent

abstract class CreatePurchase @Throws(UnsupportedEncodingException::class)
protected constructor(originActivityRef: WeakReference<Activity>, purchaseDateStr: String, purchaseName: String) : RetrieveTask("&ajouter_achat"
        + "&date_achat=" + purchaseDateStr
        + "&description_achat=" + URLEncoder.encode(purchaseName, "UTF-8"), originActivityRef) {

    init {
        CreatePurchase.originActivityRef = originActivityRef
    }

    override fun onPreExecute() {
        WhatTheDuck.wtd!!.toggleProgressbarLoading(originActivityRef, true)
        trackEvent("addpurchase/start")
    }

    override fun onPostExecute(response: String) {
        trackEvent("addpurchase/finish")
        if (response != "OK") {
            WhatTheDuck.wtd!!.alert(originActivityRef, R.string.internal_error, R.string.internal_error__purchase_creation_failed, "")
        }

        WhatTheDuck.wtd!!.toggleProgressbarLoading(originActivityRef, false)
        afterDataHandling()
    }

    protected abstract fun afterDataHandling()

    companion object {

        protected var originActivityRef: WeakReference<Activity>
    }
}
