package net.ducksmanager.api

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import com.google.gson.GsonBuilder
import net.ducksmanager.persistence.models.internal.Sync
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_API_ENDPOINT_URL
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_APPFOLLOW_API_ENDPOINT_URL
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_APPFOLLOW_API_USER
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_ROLE_NAME
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.CONFIG_KEY_ROLE_PASSWORD
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.alert
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.applicationVersion
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.config
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
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
import java.time.Instant
import java.util.*

class DmServer {
    companion object {
        const val EVENT_RETRIEVE_COLLECTION = "retrieveCollection"
        const val EVENT_RETRIEVE_ALL_PUBLICATIONS = "retrieveAllPublications"
        const val EVENT_RETRIEVE_ISSUE_COUNT = "retrieveIssueCount"
        const val EVENT_GET_PURCHASES = "getPurchases"
        const val EVENT_GET_SUGGESTED_ISSUES = "getSuggestedIssues"
        const val EVENT_GET_SUGGESTED_ISSUES_BY_RELEASE_DATE = "getSuggestedIssuesByReleaseDate"
        const val EVENT_GET_USER_NOTIFICATION_COUNTRIES = "getUserNotificationCountries"
        const val EVENT_RETRIEVE_ALL_COUNTRIES = "getInducksCountries"

        const val EVENT_RETRIEVE_LATEST_APP_VERSION = "retrieveLatestAppVersion"

        lateinit var completedSyncs: HashMap<String, Boolean>

        lateinit var api: DmServerApi
        var appFollowApi: AppFollowApi? = null

        var apiDmUser: String? = null
        var apiDmPassword: String? = null

        fun getRequestHeaders(withUserCredentials: Boolean): Map<String, String> {
            val filledUserCredentials = withUserCredentials && apiDmUser != null && apiDmPassword != null
            return hashMapOf(
                "Authorization" to Credentials.basic(
                    config.getProperty(CONFIG_KEY_ROLE_NAME),
                    config.getProperty(CONFIG_KEY_ROLE_PASSWORD)
                ),
                "x-dm-version" to applicationVersion,
                "x-dm-user" to if (filledUserCredentials) apiDmUser!! else "",
                "x-dm-pass" to if (filledUserCredentials) apiDmPassword!! else ""
            ).toMap()
        }

        fun initApi() {
            completedSyncs = hashMapOf(
                EVENT_RETRIEVE_COLLECTION to false,
                EVENT_RETRIEVE_ALL_PUBLICATIONS to false,
                EVENT_GET_PURCHASES to false,
                EVENT_GET_SUGGESTED_ISSUES to false,
                EVENT_GET_USER_NOTIFICATION_COUNTRIES to false,
                EVENT_RETRIEVE_ALL_COUNTRIES to false,
                EVENT_RETRIEVE_LATEST_APP_VERSION to false,
            )
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
                .baseUrl(config.getProperty(CONFIG_KEY_API_ENDPOINT_URL))
                .client(okHttpClientDmApi)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            api = retrofit.create(DmServerApi::class.java)

            val okHttpClientAppFollowApi = OkHttpClient().newBuilder()
                .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                    chain.proceed(chain.request().newBuilder().headers(
                        hashMapOf(
                            "Authorization" to Credentials.basic(
                                config.getProperty(CONFIG_KEY_APPFOLLOW_API_USER),
                                ""
                            )
                        ).toHeaders())
                        .build()
                    )
                })
                .addInterceptor(interceptor)
                .build()

            val appFollowApiUrl = config.getProperty(CONFIG_KEY_APPFOLLOW_API_ENDPOINT_URL)
            if (!appFollowApiUrl.isNullOrEmpty()) {
                val retrofitAppFollow = Retrofit.Builder()
                    .baseUrl(appFollowApiUrl)
                    .client(okHttpClientAppFollowApi)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                appFollowApi = retrofitAppFollow.create(AppFollowApi::class.java)
            }
        }
    }

    abstract class Callback<T> protected constructor(
        private val eventName: String,
        originActivity: Activity,
        private val alertOnError: Boolean
    ) : retrofit2.Callback<T> {
        private val originActivityRef: WeakReference<Activity>

        init {
            println("API call start : $eventName")
            trackEvent("$eventName/start")
            originActivityRef = WeakReference(originActivity)
            originActivityRef.get()!!.findViewById<View>(R.id.progressBar)?.visibility = ProgressBar.VISIBLE
        }

        abstract fun onSuccessfulResponse(response: Response<T>)

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                isOfflineMode = false
                onSuccessfulResponse(response)
                logSync()
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
                    isOfflineMode = true
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
            isOfflineMode = true
            onFailureFailover()
            originActivityRef.get()!!.findViewById<View?>(R.id.progressBar)?.visibility = ProgressBar.GONE
            onFinished()
        }

        private fun onFinished() {
            println("API call end : $eventName")
            trackEvent("$eventName/finish")
        }

        private fun logSync() {
            if (completedSyncs.containsKey(eventName)) {
                completedSyncs[eventName] = true
            }
            if (completedSyncs.values.all { it }) {
                appDB!!.syncDao().insert(Sync(Instant.now(), applicationVersion))
            }
        }
    }
}