package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pusher.pushnotifications.BeamsCallback
import com.pusher.pushnotifications.PushNotifications
import com.pusher.pushnotifications.PusherCallbackError
import com.pusher.pushnotifications.auth.AuthData
import com.pusher.pushnotifications.auth.AuthDataGetter
import com.pusher.pushnotifications.auth.BeamsTokenProvider
import net.ducksmanager.api.DmServer
import net.ducksmanager.api.DmServer.getRequestHeaders
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.persistence.models.dm.User
import net.ducksmanager.util.Settings.toSHA1
import retrofit2.Response
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

class Login : AppCompatActivity() {

    companion object {
        fun fetchCollection(activityRef: WeakReference<Activity>, targetClass: Class<*>?, alertIfError: Boolean?) {
            DmServer.api.userIssues.enqueue(object : DmServer.Callback<List<Issue>>("retrieveCollection", activityRef.get()!!, alertIfError!!) {
                override fun onSuccessfulResponse(response: Response<List<Issue>>) {
                    val user = User(DmServer.apiDmUser!!, DmServer.apiDmPassword!!)
                    WhatTheDuck.appDB.userDao().insert(user)

                    val apiEndpointUrl: String = WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_API_ENDPOINT_URL)

                    WhatTheDuck.tokenProvider = BeamsTokenProvider(
                        "$apiEndpointUrl/collection/notification_token",
                        object: AuthDataGetter {
                            override fun getAuthData(): AuthData {
                                return AuthData(getRequestHeaders(true), HashMap())
                            }
                        }
                    )

                    if (!WhatTheDuck.isTestContext(apiEndpointUrl)) {
                        try {
                            PushNotifications.start(activityRef.get(), WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_PUSHER_INSTANCE_ID))
                            PushNotifications.setUserId(user.username, WhatTheDuck.tokenProvider, object : BeamsCallback<Void, PusherCallbackError> {
                                override fun onSuccess(vararg values: Void) {
                                    Timber.i("Successfully authenticated with Pusher Beams")
                                }

                                override fun onFailure(error: PusherCallbackError) {
                                    Timber.i("PusherBeams : Pusher Beams authentication failed: %s", error.message)
                                }
                            })
                        } catch (e: Exception) {
                            Timber.e("Pusher init failed : %s", e.message)
                        }
                    }
                    WhatTheDuck.appDB.issueDao().deleteAll()
                    WhatTheDuck.appDB.issueDao().insertList(response.body()!!)
                    DmServer.api.userPurchases.enqueue(object : DmServer.Callback<List<Purchase>>("getPurchases", activityRef.get()!!, true) {
                        override fun onSuccessfulResponse(response: Response<List<Purchase>>) {
                            WhatTheDuck.appDB.purchaseDao().deleteAll()
                            WhatTheDuck.appDB.purchaseDao().insertList(response.body()!!)
                            ItemList.type = WhatTheDuck.CollectionType.USER.toString()
                            activityRef.get()!!.startActivity(Intent(activityRef.get(), targetClass))
                        }
                    })
                }

                override fun onErrorResponse(response: Response<List<Issue>>?) {
                    if (!alertIfError!!) {
                        activityRef.get()!!.startActivity(Intent(activityRef.get(), Login::class.java))
                    }
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuck).setup()

        val user: User? = WhatTheDuck.appDB.userDao().currentUser
        if (user != null) {
            DmServer.apiDmUser = user.username
            DmServer.apiDmPassword = user.password
            fetchCollection(WeakReference(this@Login), CountryList::class.java, false)
        } else {
            setContentView(R.layout.whattheduck)
            showLoginForm()
        }
    }

    private fun showLoginForm() {
        findViewById<View>(R.id.login_form).visibility = View.VISIBLE

        findViewById<EditText>(R.id.username).addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                findViewById<EditText>(R.id.password).setText("")
            }
        })

        findViewById<Button>(R.id.end_signup)
            .setOnClickListener {
                startActivity(Intent(this, Signup::class.java)
                    .putExtra("username", findViewById<EditText>(R.id.username).text.toString())
                )
            }

        findViewById<Button>(R.id.login)
            .setOnClickListener { view: View? ->
                hideKeyboard(view)
                loginAndFetchCollection()
            }

        findViewById<TextView>(R.id.linkToDM)
            .setOnClickListener { view: View? ->
                hideKeyboard(view)
                this@Login.startActivity(
                    Intent(Intent.ACTION_VIEW).setData(Uri.parse(WhatTheDuck.dmUrl))
                )
            }
    }

    private fun loginAndFetchCollection() {
        val username = findViewById<EditText>(R.id.username).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()

        DmServer.apiDmUser = username
        DmServer.apiDmPassword = toSHA1(password, WeakReference(this))

        val activityRef = WeakReference<Activity>(this)

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            WhatTheDuck.alert(activityRef, R.string.input_error, R.string.input_error__empty_credentials)
            findViewById<ProgressBar>(R.id.progressBar).visibility = ProgressBar.INVISIBLE
            findViewById<View>(R.id.login_form).visibility = View.VISIBLE
        } else {
            fetchCollection(activityRef, CountryList::class.java, true)
        }
    }

    private fun hideKeyboard(view: View?) {
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}