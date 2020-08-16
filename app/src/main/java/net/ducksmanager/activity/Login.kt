package net.ducksmanager.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.pusher.pushnotifications.auth.AuthData
import com.pusher.pushnotifications.auth.AuthDataGetter
import com.pusher.pushnotifications.auth.BeamsTokenProvider
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.coa.InducksIssue
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.composite.SuggestionList
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.persistence.models.dm.User
import net.ducksmanager.util.Settings.toSHA1
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
import net.ducksmanager.whattheduck.databinding.LoginBinding
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.*

class Login : AppCompatActivity() {
    private lateinit var binding: LoginBinding

    companion object {
        fun fetchCollection(activityRef: WeakReference<Activity>, alertIfError: Boolean?) {
            ItemList.type = WhatTheDuck.CollectionType.USER.toString()

            val originActivity = activityRef.get()!!
            val targetClass = CountryList::class.java

            DmServer.api.userIssues.enqueue(object : DmServer.Callback<List<Issue>>("retrieveCollection", originActivity, alertIfError!!) {
                override val isFailureAllowed = true

                override fun onFailureFailover() {
                    isOfflineMode = true
                    originActivity.startActivity(Intent(activityRef.get(), targetClass))
                }

                override fun onErrorResponse(response: Response<List<Issue>>?) {
                    if (!alertIfError!!) {
                        originActivity.startActivity(Intent(activityRef.get(), Login::class.java))
                    }
                }

                override fun onSuccessfulResponse(response: Response<List<Issue>>) {
                    val user = User(DmServer.apiDmUser!!, DmServer.apiDmPassword!!)
                    appDB!!.userDao().insert(user)

                    appDB!!.issueDao().deleteAll()
                    appDB!!.issueDao().insertList(response.body()!!)

                    DmServer.api.getCountries(WhatTheDuck.locale).enqueue(object : DmServer.Callback<HashMap<String, String>>("getInducksCountries", originActivity) {
                        override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                            appDB!!.inducksCountryDao().deleteAll()
                            appDB!!.inducksCountryDao().insertList( response.body()!!.keys.map { countryCode ->
                                InducksCountryName(countryCode, response.body()!![countryCode]!!)
                            })
                            originActivity.startActivity(Intent(activityRef.get(), targetClass))
                        }
                    })

                    DmServer.api.publications.enqueue(object : DmServer.Callback<HashMap<String, String>>("retrieveAllPublications", originActivity, true) {
                        override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                            appDB!!.inducksPublicationDao().deleteAll()
                            appDB!!.inducksPublicationDao().insertList(response.body()!!.keys.map { publicationCode ->
                                InducksPublication(publicationCode, response.body()!![publicationCode]!!)
                            })
                        }
                    })

                    DmServer.api.issues.enqueue(object : DmServer.Callback<HashMap<String, HashMap<String, String>>>("retrieveAllIssues", originActivity, alertIfError!!) {
                        override fun onSuccessfulResponse(response: Response<HashMap<String, HashMap<String, String>>>) {
                            val issues = arrayListOf<InducksIssue>()
                            response.body()!!.forEach { (publicationCode, publicationIssues) ->
                                issues.addAll(publicationIssues.map { (issueNumber, title) ->
                                    InducksIssue(publicationCode, issueNumber, title)
                                })
                            }
                            appDB!!.inducksIssueDao().deleteAll()
                            appDB!!.inducksIssueDao().insertList(issues)
                        }
                    })

                    DmServer.api.userPurchases.enqueue(object : DmServer.Callback<List<Purchase>>("getPurchases", originActivity, true) {
                        override fun onSuccessfulResponse(response: Response<List<Purchase>>) {
                            appDB!!.purchaseDao().deleteAll()
                            appDB!!.purchaseDao().insertList(response.body()!!)
                        }
                    })

                    DmServer.api.suggestedIssues.enqueue(object : DmServer.Callback<SuggestionList>("getSuggestedIssues", originActivity) {
                        override fun onSuccessfulResponse(response: Response<SuggestionList>) {
                            Suggestions.loadSuggestions(response.body()!!)
                        }
                    })

                    registerForNotifications(activityRef)
                }
            })
        }

        private fun registerForNotifications(activityRef: WeakReference<Activity>) {
            val apiEndpointUrl: String = WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_API_ENDPOINT_URL)

            WhatTheDuck.tokenProvider = BeamsTokenProvider(
                "$apiEndpointUrl/collection/notification_token",
                object : AuthDataGetter {
                    override fun getAuthData(): AuthData {
                        return AuthData(DmServer.getRequestHeaders(true), HashMap())
                    }
                }
            )
            WhatTheDuck.registerForNotifications(activityRef, false)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuck).setup()

        val user: User? = appDB!!.userDao().currentUser
        if (user != null) {
            DmServer.apiDmUser = user.username
            DmServer.apiDmPassword = user.password
            fetchCollection(WeakReference(this@Login), false)
        } else {
            binding = LoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
            showLoginForm()
        }
    }

    private fun showLoginForm() {
        binding.endSignup.setOnClickListener {
            startActivity(Intent(this, Signup::class.java)
                .putExtra("username", binding.username.text.toString())
            )
        }

        binding.login.setOnClickListener { view: View? ->
            hideKeyboard(view)
            loginAndFetchCollection()
        }

        binding.linkToDM.setOnClickListener { view: View? ->
            hideKeyboard(view)
            this@Login.startActivity(
                Intent(Intent.ACTION_VIEW).setData(Uri.parse(WhatTheDuck.dmUrl))
            )
        }
    }

    private fun loginAndFetchCollection() {
        val username = binding.username.text.toString()
        val password = binding.password.text.toString()

        DmServer.apiDmUser = username
        DmServer.apiDmPassword = toSHA1(password, WeakReference(this))

        val activityRef = WeakReference<Activity>(this)

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            WhatTheDuck.alert(activityRef, R.string.input_error, R.string.input_error__empty_credentials)
            binding.progressBar.visibility = View.INVISIBLE
        } else {
            fetchCollection(activityRef, true)
        }
    }

    private fun hideKeyboard(view: View?) {
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}