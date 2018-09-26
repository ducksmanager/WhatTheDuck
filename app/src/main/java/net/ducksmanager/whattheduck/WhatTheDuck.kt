package net.ducksmanager.whattheduck


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import com.koushikdutta.ion.builder.Builders.Any.B

import net.ducksmanager.retrievetasks.ConnectAndRetrieveList
import net.ducksmanager.retrievetasks.Signup
import net.ducksmanager.util.Settings

import java.io.File
import java.lang.ref.WeakReference
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Locale

import net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL
import net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_DM_URL

class WhatTheDuck : Activity() {

    private val dmUrl: String
        get() = WhatTheDuckApplication.config!!.getProperty(CONFIG_KEY_DM_URL)

    private val isOffline: Boolean
        get() {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            return netInfo == null || !netInfo.isConnected
        }

    val applicationVersion: String
        @Throws(NameNotFoundException::class)
        get() {
            val manager = this.packageManager
            val info = manager.getPackageInfo(this.packageName, 0)
            return info.versionName
        }

    public override fun onCreate(savedInstanceState: Bundle) {
        wtd = this
        super.onCreate(savedInstanceState)
        (application as WhatTheDuckApplication).trackActivity(this)

        Settings.loadUserSettings()

        val encryptedPassword = Settings.encryptedPassword

        if (encryptedPassword != null) {
            ConnectAndRetrieveList(false).execute()
        } else {
            initUI()
        }
    }

    fun initUI() {
        setContentView(R.layout.whattheduck)
        (findViewById<View>(R.id.checkBoxRememberCredentials) as CheckBox).isChecked = Settings.username != null

        val usernameEditText = findViewById<EditText>(R.id.username)
        usernameEditText.setText(Settings.username)
        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                (findViewById<View>(R.id.password) as EditText).setText("")
            }
        })

        val signupButton = findViewById<Button>(R.id.end_signup)
        signupButton.setOnClickListener { view ->
            Settings.username = (this@WhatTheDuck.findViewById<View>(R.id.username) as EditText).text.toString()
            Settings.setPassword((this@WhatTheDuck.findViewById<View>(R.id.password) as EditText).text.toString())

            wtd!!.startActivity(Intent(wtd, Signup::class.java))
        }

        val loginButton = findViewById<Button>(R.id.login)
        loginButton.setOnClickListener { view ->
            hideKeyboard(view)
            ConnectAndRetrieveList(true).execute()
        }

        val linkToDM = findViewById<TextView>(R.id.linkToDM)
        linkToDM.setOnClickListener { view ->
            hideKeyboard(view)
            val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(dmUrl))
            this@WhatTheDuck.startActivity(intent)
        }
    }

    private fun hideKeyboard(view: View?) {
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun info(activity: WeakReference<Activity>, titleId: Int, duration: Int) {
        Toast.makeText(activity.get(), titleId, duration).show()
    }

    fun alert(messageId: Int) {
        alert(WeakReference<Activity>(this), getString(messageId))
    }

    fun alert(activity: WeakReference<Activity>, message: String) {
        val builder = AlertDialog.Builder(activity.get())
        builder.setTitle(getString(R.string.error))
        builder.setMessage(message)
        builder.create().show()
    }

    fun alert(activity: WeakReference<Activity>, messageId: Int) {
        alert(activity, getString(messageId))
    }

    fun alert(activity: WeakReference<Activity>, titleId: Int, messageId: Int, extraMessage: String) {
        val builder = AlertDialog.Builder(activity.get())
        builder.setTitle(getString(titleId))
        builder.setMessage(getString(messageId) + extraMessage)

        this.runOnUiThread { builder.create().show() }
    }

    fun alert(titleId: Int, messageId: Int, extraMessage: String) {
        alert(WeakReference(this), titleId, messageId, extraMessage)
    }

    fun alert(titleId: Int, messageId: Int) {
        alert(WeakReference(this), titleId, messageId, "")
    }

    @Throws(Exception::class)
    fun retrieveOrFailDmServer(urlSuffix: String, futureCallback: FutureCallback<String>, fileName: String, file: File?) {
        var urlSuffix = urlSuffix
        if (isOffline) {
            throw Exception(getString(R.string.network_error))
        }

        if (Settings.encryptedPassword == null) {
            val md = MessageDigest.getInstance("SHA-1")
            md.update(Settings.password!!.toByteArray())
            Settings.encryptedPassword = Settings.byteArray2Hex(md.digest())
        }

        urlSuffix = urlSuffix.replace("\\{locale\\}".toRegex(), applicationContext.resources.configuration.locale.language)

        val call = Ion.with(this.findViewById<View>(android.R.id.content).context)
                .load(WhatTheDuckApplication.config!!.getProperty(CONFIG_KEY_API_ENDPOINT_URL) + urlSuffix)
                .setHeader("x-dm-version", WhatTheDuck.wtd!!.applicationVersion)

        if (file != null) {
            call.setMultipartFile(fileName, file)
        }

        call.asString().setCallback(futureCallback)
    }

    @Throws(Exception::class)
    fun retrieveOrFail(downloadHandler: RetrieveTask.DownloadHandler, urlSuffix: String): String {
        if (isOffline) {
            throw Exception("" + R.string.network_error)
        }

        if (Settings.encryptedPassword == null) {
            val md = MessageDigest.getInstance("SHA-1")
            md.update(Settings.password!!.toByteArray())
            Settings.encryptedPassword = Settings.byteArray2Hex(md.digest())
        }

        if (serverURL == null) {
            serverURL = downloadHandler.getPage("$dmUrl/$DUCKSMANAGER_PAGE_WITH_REMOTE_URL")
        }

        var response = downloadHandler.getPage(serverURL + "/" + SERVER_PAGE
                + "?pseudo_user=" + URLEncoder.encode(Settings.username, "UTF-8")
                + "&mdp_user=" + Settings.encryptedPassword
                + "&mdp=" + WhatTheDuckApplication.config!!.getProperty(WhatTheDuckApplication.CONFIG_KEY_SECURITY_PASSWORD)
                + "&version=" + applicationVersion
                + "&language=" + Locale.getDefault().language
                + urlSuffix)

        response = response.replace("/\\/".toRegex(), "")
        return if (response == "0") {
            throw SecurityException()
        } else
            response
    }

    fun toggleProgressbarLoading(activityRef: WeakReference<Activity>, toggle: Boolean) {
        val progressBar = activityRef.get().findViewById<ProgressBar>(R.id.progressBar)

        if (progressBar != null) {
            if (toggle) {
                progressBar.visibility = ProgressBar.VISIBLE
            } else {
                progressBar.clearAnimation()
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    fun toggleProgressbarLoading(toggle: Boolean) {
        toggleProgressbarLoading(WeakReference<Activity>(WhatTheDuck.wtd), toggle)
    }

    override fun onBackPressed() {}

    companion object {
        private val SERVER_PAGE = "WhatTheDuck.php"

        @VisibleForTesting
        val DUCKSMANAGER_PAGE_WITH_REMOTE_URL = "WhatTheDuck_server.php"

        private var serverURL: String? = null

        var wtd: WhatTheDuck? = null

        var userCollection = Collection()
        var coaCollection = Collection()

        var selectedCountry: String? = null
        var selectedPublication: String? = null
        var selectedIssue: String? = null

        val isMobileConnection: Boolean
            get() {
                val cm = WhatTheDuck.wtd!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        ?: return false
                val activeNetwork = cm.activeNetworkInfo
                return activeNetwork.type == ConnectivityManager.TYPE_MOBILE
            }

        fun showAbout(originActivity: Activity) {
            val builder = AlertDialog.Builder(originActivity)
            builder.setTitle(originActivity.getString(R.string.about))
            try {
                val versionNumber = originActivity.packageManager.getPackageInfo(originActivity.packageName, 0).versionName
                builder.setMessage(originActivity.getString(R.string.version) + " " + versionNumber + "\n\n" + originActivity.getString(R.string.rate_app))
                builder.create().show()
            } catch (ignored: NameNotFoundException) {
            }

        }

        fun trackEvent(text: String) {
            if (wtd != null) {
                (wtd!!.application as WhatTheDuckApplication).trackEvent(text)
            }
        }
    }
}
