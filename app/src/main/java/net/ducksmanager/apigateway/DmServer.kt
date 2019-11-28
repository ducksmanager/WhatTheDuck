package net.ducksmanager.apigateway

import android.app.Activity
import android.view.View
import android.widget.ProgressBar
import com.google.gson.GsonBuilder
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.util.*

object DmServer {

    lateinit var api: DmServerApi

    @JvmField
    var apiDmUser: String? = null

    @JvmField
    var apiDmPassword: String? = null

    @JvmStatic
    fun getRequestHeaders(withUserCredentials: Boolean): HashMap<String, String?> {
        val headers = HashMap<String, String?>()
        headers["Authorization"] = Credentials.basic(WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_ROLE_NAME), WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_ROLE_PASSWORD))
        headers["x-dm-version"] = WhatTheDuck.applicationVersion
        if (withUserCredentials) {
            headers["x-dm-user"] = getApiDmUser()
            headers["x-dm-pass"] = getApiDmPassword()
        }
        return headers
    }

    @JvmStatic
    fun initApi() {
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create()
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient().newBuilder()
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val originalRequest = chain.request()
                val builder = originalRequest.newBuilder()
                val headers = getRequestHeaders(getApiDmUser() != null)
                for (headerKey in headers.keys) {
                    builder.header(headerKey, Objects.requireNonNull(headers[headerKey])!!)
                }
                val newRequest = builder.build()
                chain.proceed(newRequest)
            })
            .addInterceptor(interceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_API_ENDPOINT_URL))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        api = retrofit.create(DmServerApi::class.java)
    }

    private fun getApiDmUser(): String? {
        return apiDmUser
    }

    @JvmStatic
    fun setApiDmUser(apiDmUser: String?) {
        DmServer.apiDmUser = apiDmUser
    }

    private fun getApiDmPassword(): String? {
        return apiDmPassword
    }

    @JvmStatic
    fun setApiDmPassword(apiDmPassword: String?) {
        DmServer.apiDmPassword = apiDmPassword
    }

    abstract class Callback<T> protected constructor(
        private val eventName: String,
        originActivity: Activity,
        private val alertOnError: Boolean = true
    ) : retrofit2.Callback<T> {
        private val originActivityRef: WeakReference<Activity>

        abstract fun onSuccessfulResponse(response: Response<T>)

        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                onSuccessfulResponse(response)
            } else {
                if (alertOnError) {
                    when (response.code()) {
                        HttpURLConnection.HTTP_UNAUTHORIZED -> WhatTheDuck.alert(originActivityRef, R.string.input_error__invalid_credentials)
                        else -> WhatTheDuck.alert(originActivityRef, R.string.error)
                    }
                }
                onErrorResponse(response)
            }
            onFinished()
        }

        open fun onErrorResponse(response: Response<T>?) {}

        override fun onFailure(call: Call<T>, t: Throwable) {
            WhatTheDuck.alert(originActivityRef, t.message + " on " + eventName)
            System.err.println(t.message)
            onFinished()
        }

        private fun onFinished() {
            WhatTheDuck.trackEvent("$eventName/finish")
            originActivityRef.get()!!.findViewById<View?>(R.id.progressBar)?.visibility = ProgressBar.GONE
        }

        init {
            WhatTheDuck.trackEvent("$eventName/start")
            originActivityRef = WeakReference(originActivity)
            originActivityRef.get()!!.findViewById<View>(R.id.progressBar)?.visibility = ProgressBar.VISIBLE
        }
    }
}