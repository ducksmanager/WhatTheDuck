package net.ducksmanager.retrievetasks


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.RetrieveTask
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuckApplication

import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference

import net.ducksmanager.whattheduck.WhatTheDuck.trackEvent

class Signup : Activity() {

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuckApplication).trackActivity(this)

        setContentView(R.layout.signup)

        setTitle(R.string.app_name)

        val usernameField = findViewById<EditText>(R.id.username_signup)
        val passwordField = findViewById<EditText>(R.id.password_signup)
        val passwordConfirmationField = findViewById<EditText>(R.id.password_confirmation)
        val emailField = findViewById<EditText>(R.id.email_address)

        val endSignupButton = findViewById<Button>(R.id.end_signup)
        val cancelSignupButton = findViewById<Button>(R.id.cancel_signup)

        usernameField.setText(Settings.username)
        passwordField.setText(Settings.password)

        endSignupButton.setOnClickListener { view ->
            Settings.username = usernameField.text.toString()
            Settings.setPassword(passwordField.text.toString())

            val password2 = Settings.toSHA1(passwordConfirmationField.text.toString())
            val email = emailField.text.toString()

            ConnectAndRetrieveList(this@Signup, "&action=signup&mdp_user2=$password2&email=$email").execute()
        }

        cancelSignupButton.setOnClickListener { view ->
            val i = Intent(this@Signup, WhatTheDuck::class.java)
            this@Signup.startActivity(i)
        }
    }


    protected class ConnectAndRetrieveList internal constructor(originActivity: Activity, urlSuffix: String) : RetrieveTask(urlSuffix, WeakReference(originActivity)) {

        override fun onPostExecute(response: String?) {
            var response = response
            super.onPostExecute(response)
            trackEvent("signup/finish")
            if (response != null) {
                try {
                    response = String(response.toByteArray(charset("ISO8859-1")), "UTF-8")

                    if (response == "OK") {
                        val originActivity = originActivityRef.get()
                        val i = Intent(originActivity, WhatTheDuck::class.java)
                        WhatTheDuck.wtd!!.startActivity(i)

                        val usernameField = WhatTheDuck.wtd!!.findViewById<EditText>(R.id.username)
                        val passwordField = WhatTheDuck.wtd!!.findViewById<EditText>(R.id.password)
                        usernameField.setText(Settings.username)
                        passwordField.setText(Settings.password)

                        WhatTheDuck.wtd!!.info(originActivityRef, R.string.signup__confirm, Toast.LENGTH_SHORT)
                    } else {
                        WhatTheDuck.wtd!!.alert(originActivityRef, response)
                    }
                } catch (e: UnsupportedEncodingException) {
                    WhatTheDuck.wtd!!.alert(originActivityRef, R.string.internal_error)
                }

            }
        }
    }
}
