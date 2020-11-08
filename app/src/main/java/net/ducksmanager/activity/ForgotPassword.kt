package net.ducksmanager.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.EmailWrapper
import net.ducksmanager.whattheduck.R.string.reset_password_confirmation
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.databinding.ForgotPasswordBinding
import retrofit2.Response

class ForgotPassword : AppCompatActivity() {
    private lateinit var binding: ForgotPasswordBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuck).trackActivity(this)

        binding = ForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sendEmail.setOnClickListener {
            val email = binding.emailAddress.text.toString()
            DmServer.api.initForgotPassword(EmailWrapper(email)).enqueue(object : DmServer.Callback<Void>("initForgotPassword", this@ForgotPassword, true) {
                override fun onSuccessfulResponse(response: Response<Void>) {
                    binding.emailAddress.isEnabled = false
                    binding.sendEmail.isEnabled = false
                    binding.feedback.text = getString(reset_password_confirmation)
                }
            })
        }
    }
}