package net.ducksmanager.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pusher.pushnotifications.auth.AuthData
import com.pusher.pushnotifications.auth.AuthDataGetter
import com.pusher.pushnotifications.auth.BeamsTokenProvider
import net.ducksmanager.api.DmServer.Callback
import net.ducksmanager.api.DmServer.Companion.EVENT_GET_PURCHASES
import net.ducksmanager.api.DmServer.Companion.EVENT_GET_SUGGESTED_ISSUES
import net.ducksmanager.api.DmServer.Companion.EVENT_GET_SUGGESTED_ISSUES_BY_RELEASE_DATE
import net.ducksmanager.api.DmServer.Companion.EVENT_RETRIEVE_ALL_PUBLICATIONS
import net.ducksmanager.api.DmServer.Companion.EVENT_RETRIEVE_COLLECTION
import net.ducksmanager.api.DmServer.Companion.EVENT_RETRIEVE_ISSUE_COUNT
import net.ducksmanager.api.DmServer.Companion.EVENT_RETRIEVE_LATEST_APP_VERSION
import net.ducksmanager.api.DmServer.Companion.api
import net.ducksmanager.api.DmServer.Companion.apiDmPassword
import net.ducksmanager.api.DmServer.Companion.apiDmUser
import net.ducksmanager.api.DmServer.Companion.appFollowApi
import net.ducksmanager.api.DmServer.Companion.getRequestHeaders
import net.ducksmanager.persistence.dao.SuggestedIssueByReleaseDateDao
import net.ducksmanager.persistence.dao.SuggestedIssueDao
import net.ducksmanager.persistence.models.appfollow.Apps
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.composite.InducksIssueCount
import net.ducksmanager.persistence.models.composite.SuggestionList
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.persistence.models.dm.User
import net.ducksmanager.persistence.models.internal.Sync
import net.ducksmanager.util.Settings.Companion.loadNotificationCountries
import net.ducksmanager.util.Settings.Companion.toSHA1
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.applicationVersion
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
import net.ducksmanager.whattheduck.databinding.LoginBinding
import retrofit2.Response
import java.lang.ref.WeakReference
import java.time.Instant
import java.util.*

class Login : AppCompatActivity() {
    private lateinit var binding: LoginBinding

    companion object {
        fun isObsoleteSync(latestSync: Sync?): Boolean {
            return latestSync == null || Instant.now().epochSecond - latestSync.timestamp.epochSecond > 12 * 60 * 60
        }

        fun fetchCollection(activityRef: WeakReference<Activity>, alertIfError: Boolean?) {
            ItemList.type = WhatTheDuck.CollectionType.USER.toString()

            val originActivity = activityRef.get()!!
            val targetClass = CountryList::class.java

            val latestSync = appDB!!.syncDao().findLatest(applicationVersion)

            api.userIssues.enqueue(object : Callback<List<Issue>>(EVENT_RETRIEVE_COLLECTION, originActivity, alertIfError!!) {
                override fun onFailureFailover() {
                    if (latestSync == null) {
                        isOfflineMode = true
                        activityRef.get()!!.findViewById<TextView>(R.id.warningMessage).visibility = VISIBLE
                    }
                    else {
                        originActivity.startActivity(Intent(activityRef.get(), targetClass))
                    }
                }

                override fun onErrorResponse(response: Response<List<Issue>>?) {}

                override fun onSuccessfulResponse(response: Response<List<Issue>>) {
                    val user = User(apiDmUser!!, apiDmPassword!!)
                    if (user.username !== WhatTheDuck.currentUser?.username) {
                        WhatTheDuck.currentUser = user
                        appDB!!.userDao().insert(user)
                    }

                    appDB!!.issueDao().deleteAll()
                    appDB!!.issueDao().insertList(response.body()!!)

                    originActivity.startActivity(Intent(activityRef.get(), targetClass))

                    api.userPurchases.enqueue(object : Callback<List<Purchase>>(EVENT_GET_PURCHASES, originActivity, true) {
                        override fun onSuccessfulResponse(response: Response<List<Purchase>>) {
                            appDB!!.purchaseDao().deleteAll()
                            appDB!!.purchaseDao().insertList(response.body()!!)
                        }
                    })

                    api.suggestedIssues.enqueue(object : Callback<SuggestionList>(EVENT_GET_SUGGESTED_ISSUES, originActivity, true) {
                        override fun onSuccessfulResponse(response: Response<SuggestionList>) {
                            Suggestions.loadSuggestions(response.body()!!, SuggestedIssueDao::class.java)
                        }
                    })

                    api.suggestedIssuesByReleaseDate.enqueue(object : Callback<SuggestionList>(EVENT_GET_SUGGESTED_ISSUES_BY_RELEASE_DATE, originActivity, true) {
                        override fun onSuccessfulResponse(response: Response<SuggestionList>) {
                            Suggestions.loadSuggestions(response.body()!!, SuggestedIssueByReleaseDateDao::class.java)
                        }
                    })

                    loadNotificationCountries(originActivity)

                    if (isObsoleteSync(latestSync)) {
                        appFollowApi?.getAppVersion()?.enqueue(object : Callback<Apps>(EVENT_RETRIEVE_LATEST_APP_VERSION, originActivity, true) {
                            override fun onSuccessfulResponse(response: Response<Apps>) {
                                appDB!!.appVersionDao().deleteAll()
                                appDB!!.appVersionDao().insert(response.body()!!.apps.first().app)
                            }
                        })
                        api.publications.enqueue(object : Callback<HashMap<String, String>>(EVENT_RETRIEVE_ALL_PUBLICATIONS, originActivity, true) {
                            override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                                appDB!!.inducksPublicationDao().deleteAll()
                                appDB!!.inducksPublicationDao().insertList(response.body()!!.keys.map { publicationCode ->
                                    InducksPublication(publicationCode, response.body()!![publicationCode]!!)
                                })
                            }
                        })
                        api.issueCount.enqueue(object : Callback<HashMap<String, Int>>(EVENT_RETRIEVE_ISSUE_COUNT, originActivity, true) {
                            override fun onSuccessfulResponse(response: Response<HashMap<String, Int>>) {
                                appDB!!.inducksIssueCountDao().deleteAll()
                                appDB!!.inducksIssueCountDao().insertList(response.body()!!.keys.map { publicationCode ->
                                    InducksIssueCount(publicationCode, response.body()!![publicationCode]!!)
                                })
                                appDB!!.inducksIssueCountDao().createCountryCountsFromPublicationCounts()
                            }
                        })
                    }

                    registerForNotifications(activityRef)
                }
            })
        }

        private fun registerForNotifications(activityRef: WeakReference<Activity>) {
            val apiEndpointUrl: String = WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_API_ENDPOINT_URL)

            WhatTheDuck.tokenProvider = BeamsTokenProvider(
                "$apiEndpointUrl/collection/notification_token",
                object : AuthDataGetter {
                    override fun getAuthData(): AuthData = AuthData(getRequestHeaders(true), HashMap())
                }
            )
            WhatTheDuck.registerForNotifications(activityRef, false)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuck).setup()

        binding = LoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isOfflineMode) {
            showLoginForm()
            return
        }

        appDB!!.userDao().currentUser.observe(this, { user ->
            if (user == null) {
                showLoginForm()
            }
            else if (WhatTheDuck.currentUser == null) {
                apiDmUser = user.username
                apiDmPassword = user.password

                binding.progressBar.visibility = VISIBLE
                fetchCollection(WeakReference(this@Login), false)
            }
        })
    }

    private fun showLoginForm() {
        binding.loginForm.visibility = VISIBLE
        if (isOfflineMode) {
            binding.warningMessage.visibility = VISIBLE
        }
        binding.endSignup.setOnClickListener {
            startActivity(Intent(this, Signup::class.java)
                .putExtra("username", binding.username.text.toString())
            )
        }

        binding.revealPassword.setOnClickListener {
            if (binding.password.transformationMethod === null) {
                binding.password.transformationMethod = PasswordTransformationMethod()
            }
            else {
                binding.password.transformationMethod = null
            }
        }

        binding.forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }

        binding.login.setOnClickListener { view: View? ->
            hideKeyboard(view)
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            apiDmUser = username
            apiDmPassword = toSHA1(password, WeakReference(this))

            val activityRef = WeakReference<Activity>(this)

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                WhatTheDuck.alert(activityRef, R.string.input_error, R.string.input_error__empty_credentials)
                binding.progressBar.visibility = INVISIBLE
            } else {
                binding.progressBar.visibility = VISIBLE
                fetchCollection(activityRef, true)
            }
        }

        binding.linkToDM.setOnClickListener { view: View? ->
            hideKeyboard(view)
            this@Login.startActivity(
                Intent(Intent.ACTION_VIEW).setData(Uri.parse(WhatTheDuck.dmUrl))
            )
        }
    }

    private fun hideKeyboard(view: View?) {
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}