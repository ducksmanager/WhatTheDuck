package net.ducksmanager.api

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import com.google.gson.GsonBuilder
import net.ducksmanager.api.DmServer.Companion.apiDmPassword
import net.ducksmanager.api.DmServer.Companion.apiDmUser
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_EDGECREATOR_URL
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.alert
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.applicationVersion
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.config
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.trackEvent
import okhttp3.Credentials
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

class EdgeCreator {
    companion object {
        const val EVENT_UPLOAD_PHOTO = "uploadPhoto"
        const val EVENT_SAVE_EDGE_MODEL = "saveEdgeModel"

        lateinit var api: EdgeCreatorApi

        private fun getRequestHeaders(withUserCredentials: Boolean): Map<String, String> {
            val filledUserCredentials = withUserCredentials && apiDmUser != null && apiDmPassword != null
            return hashMapOf(
                "Authorization" to Credentials.basic(
                    config.getProperty(WhatTheDuck.CONFIG_KEY_ROLE_EDGECREATOR_NAME),
                    config.getProperty(WhatTheDuck.CONFIG_KEY_ROLE_EDGECREATOR_PASSWORD)
                ),
                "x-dm-version" to applicationVersion,
                "x-dm-user" to if (filledUserCredentials) apiDmUser!! else "",
                "x-dm-pass" to if (filledUserCredentials) apiDmPassword!! else "",
                "Cookie" to "x-dm-user=${if (filledUserCredentials) apiDmUser!! else ""};x-dm-pass=${if (filledUserCredentials) apiDmPassword!! else ""}"
            ).toMap()
        }

        fun initApi() {
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create()
            val interceptor = HttpLoggingInterceptor {}
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            val okHttpClientDmApi = OkHttpClient().newBuilder()
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    chain.proceed(chain.request().newBuilder().headers(
                        getRequestHeaders(apiDmUser != null).toHeaders())
                        .build()
                    )
                })
                .addInterceptor(interceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(config.getProperty(CONFIG_KEY_EDGECREATOR_URL))
                .client(okHttpClientDmApi)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            api = retrofit.create(EdgeCreatorApi::class.java)
        }
    }

    abstract class Callback<T> protected constructor(
        private val eventName: String,
        originActivity: Activity,
        private val alertOnError: Boolean
    ) : retrofit2.Callback<T> {
        private val originActivityRef: WeakReference<Activity>

        init {
            println("EdgeCreator API call start : $eventName")
            trackEvent("$eventName/start")
            originActivityRef = WeakReference(originActivity)
            originActivityRef.get()!!.findViewById<View>(R.id.progressBar)?.visibility = ProgressBar.VISIBLE
        }

        abstract fun onSuccessfulResponse(response: Response<T>)

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                onSuccessfulResponse(response)
            } else {
                if (alertOnError) {
                    when (response.code()) {
                        HTTP_UNAUTHORIZED -> alert(originActivityRef, R.string.input_error__invalid_credentials)
                        HTTP_NOT_FOUND -> alert(originActivityRef, R.string.offline_mode_cannot_login)
                        else -> alert(originActivityRef, R.string.error)
                    }
                }
                onErrorResponse(response)
                if (response.code() == HTTP_NOT_FOUND) {
                    onFailureFailover()
                    onFinished()
                }
                originActivityRef.get()!!.findViewById<View?>(R.id.progressBar)?.visibility = ProgressBar.GONE
            }
            onFinished()
        }

        open fun onErrorResponse(response: Response<T>?) {}

        open fun onFailureFailover() {}

        override fun onFailure(call: Call<T>, t: Throwable) {
            onFailureFailover()
            originActivityRef.get()!!.findViewById<View?>(R.id.progressBar)?.visibility = ProgressBar.GONE
            onFinished()
        }

        private fun onFinished() {
            println("EdgeCreator API call end : $eventName")
            trackEvent("$eventName/finish")
        }
    }
}