package net.ducksmanager.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.UserToCreate
import net.ducksmanager.util.Settings.Companion.toSHA1
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.databinding.SignupBinding
import retrofit2.Response
import java.lang.ref.WeakReference

class Signup : AppCompatActivity() {
    private lateinit var binding: SignupBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuck).trackActivity(this)

        binding = SignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setTitle(R.string.app_name)

        val usernameField = binding.usernameSignup

        val defaultUsername = intent.getStringExtra("username")
        if (defaultUsername != null) {
            usernameField.setText(defaultUsername)
        }

        binding.endSignup.setOnClickListener {
            val username = usernameField.text.toString()
            val password = binding.passwordSignup.text.toString()
            val password2 = binding.passwordConfirmation.text.toString()
            val email = binding.emailAddress.text.toString()
            DmServer.api.createUser(UserToCreate(username, password, password2, email)).enqueue(object : DmServer.Callback<Void>("signup", this@Signup, true) {
                override fun onSuccessfulResponse(response: Response<Void>) {
                    DmServer.apiDmUser = username
                    DmServer.apiDmPassword = toSHA1(password, WeakReference(this@Signup))
                    Login.fetchCollection(WeakReference(this@Signup), true)
                }
            })
        }
        binding.cancelSignup
            .setOnClickListener {
                this@Signup.startActivity(Intent(this@Signup, Login::class.java))
            }
    }
}