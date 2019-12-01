package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.UserToCreate
import net.ducksmanager.util.Settings.toSHA1
import net.ducksmanager.whattheduck.CountryList
import net.ducksmanager.whattheduck.Signup
import retrofit2.Response
import java.lang.ref.WeakReference

class Signup : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuck).trackActivity(this)
        setContentView(R.layout.signup)
        setTitle(R.string.app_name)

        val usernameField = findViewById<EditText>(R.id.username_signup)

        val defaultUsername = intent.getStringExtra("username")
        if (defaultUsername != null) {
            usernameField.setText(defaultUsername)
        }

        findViewById<Button>(R.id.end_signup)
            .setOnClickListener {
                val username = usernameField.text.toString()
                val password = findViewById<EditText>(R.id.password_signup).text.toString()
                val password2 = findViewById<EditText>(R.id.password_confirmation).text.toString()
                val email = findViewById<EditText>(R.id.email_address).text.toString()
                DmServer.api.createUser(UserToCreate(username, password, password2, email)).enqueue(object : DmServer.Callback<Void>("signup", this@Signup, true) {
                    override fun onSuccessfulResponse(response: Response<Void>) {
                        DmServer.apiDmUser = username
                        DmServer.apiDmPassword = toSHA1(password, WeakReference(this@Signup))
                        Login.Companion.fetchCollection(WeakReference<Activity>(this@Signup), CountryList::class.java, true)
                    }
                })
            }
        findViewById<Button>(R.id.cancel_signup)
            .setOnClickListener {
                this@Signup.startActivity(Intent(this@Signup, Login::class.java))
            }
    }
}