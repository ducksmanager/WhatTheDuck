package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.pm.PackageManager
import android.os.AsyncTask

import com.koushikdutta.async.future.FutureCallback

import net.ducksmanager.util.Settings

import org.json.JSONException

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.net.MalformedURLException
import java.net.URL

open class RetrieveTask : AsyncTask<Any, Any, String> {

    private val legacyServer = true
    private val urlSuffix: String
    protected var originActivityRef: WeakReference<Activity>

    private var thrownException: Exception? = null
    private val fileName: String
    private val file: File
    private val futureCallback: FutureCallback<*>

    interface DownloadHandler {
        fun getPage(url: String): String
    }

    private inner class DefaultDownloadHandler : DownloadHandler {
        override fun getPage(url: String): String {
            val response = StringBuilder()
            try {
                val userCollectionURL = URL(url)
                val `in` = BufferedReader(InputStreamReader(userCollectionURL.openStream()))

                var inputLine: String
                while ((inputLine = `in`.readLine()) != null)
                    response.append(inputLine)
                `in`.close()
            } catch (e: MalformedURLException) {
                WhatTheDuck.wtd!!.alert(R.string.error, R.string.error__malformed_url)
            } catch (e: IOException) {
                WhatTheDuck.wtd!!.alert(R.string.network_error, R.string.network_error__no_connection)
            }

            return response.toString()
        }
    }

    protected constructor(urlSuffix: String, originActivityRef: WeakReference<Activity>) {
        this.urlSuffix = urlSuffix
        this.originActivityRef = originActivityRef
    }

    protected constructor(urlSuffix: String, legacyServer: Boolean, futureCallback: FutureCallback<*>, fileName: String, file: File) {
        this.urlSuffix = urlSuffix
        this.legacyServer = legacyServer
        this.futureCallback = futureCallback
        this.fileName = fileName
        this.file = file
    }

    override fun doInBackground(objects: Array<Any>): String? {
        try {
            if (legacyServer) {
                return WhatTheDuck.wtd!!.retrieveOrFail(DefaultDownloadHandler(), this.urlSuffix)
            } else {
                WhatTheDuck.wtd!!.retrieveOrFailDmServer(this.urlSuffix, this.futureCallback, this.fileName, this.file)
            }

        } catch (e: Exception) {
            this.thrownException = e
        }

        return null
    }

    override fun onPostExecute(response: String) {
        if (this.thrownException != null) {
            handleResultException(this.thrownException)
        }
    }

    protected fun hasFailed(): Boolean {
        return this.thrownException != null
    }

    private fun handleResultException(e: Exception) {
        handleResultExceptionOnActivity(e, originActivityRef)
    }

    companion object {

        fun handleResultExceptionOnActivity(e: Exception, activityRef: WeakReference<Activity>) {
            WhatTheDuck.wtd!!.initUI()
            WhatTheDuck.wtd!!.toggleProgressbarLoading(activityRef, false)

            if (e is SecurityException) {
                WhatTheDuck.wtd!!.alert(
                        R.string.input_error,
                        R.string.input_error__invalid_credentials, "")
                Settings.username = ""
                Settings.setPassword("")
            } else if (e is PackageManager.NameNotFoundException) {
                e.printStackTrace()
            } else if (e is JSONException) {
                WhatTheDuck.wtd!!.alert(activityRef,
                        R.string.internal_error,
                        R.string.internal_error__malformed_list, " : " + e.message)
            } else {
                if (e.message != null && e.message == R.string.network_error.toString() + "") {
                    WhatTheDuck.wtd!!.alert(
                            R.string.network_error,
                            R.string.network_error__no_connection)
                } else {
                    WhatTheDuck.wtd!!.alert(activityRef, e.message)
                }
            }
        }
    }
}
