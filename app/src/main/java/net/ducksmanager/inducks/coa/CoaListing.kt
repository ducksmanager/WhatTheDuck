package net.ducksmanager.inducks.coa


import android.app.Activity

import com.koushikdutta.async.future.FutureCallback
import net.ducksmanager.whattheduck.R.string.result

import net.ducksmanager.whattheduck.RetrieveTask
import net.ducksmanager.whattheduck.WhatTheDuck

import org.json.JSONException

import java.lang.ref.WeakReference
import java.util.Locale

abstract class CoaListing internal constructor(activity: Activity, type: ListType, private val afterProcessCallback: FutureCallback<*>?) {
    private val activityRef: WeakReference<Activity> = WeakReference(activity)

    protected abstract val urlSuffix: String

    enum class ListType {
        COUNTRY_LIST, PUBLICATION_LIST, ISSUE_LIST
    }

    init {
        WhatTheDuck.trackEvent("list/coa/" + type.name.toLowerCase(Locale.FRANCE))
    }

    @Throws(JSONException::class)
    protected abstract fun processData(result: String)

    fun fetch() {
        try {
            WhatTheDuck.wtd!!.toggleProgressbarLoading(activityRef, true)
            WhatTheDuck.wtd!!.retrieveOrFailDmServer(urlSuffix, FutureCallback { e, result ->
                if (e != null) {
                    RetrieveTask.handleResultExceptionOnActivity(e, activityRef)
                } else {
                    try {
                        processData(result)
                    } catch (jsonException: JSONException) {
                        RetrieveTask.handleResultExceptionOnActivity(jsonException, activityRef)
                    }

                    afterProcessCallback?.onCompleted(null, result)
                    WhatTheDuck.wtd!!.toggleProgressbarLoading(activityRef, false)
                }
            }, null, null)
        } catch (e: Exception) {
            RetrieveTask.handleResultExceptionOnActivity(e, activityRef)
        }

    }
}
