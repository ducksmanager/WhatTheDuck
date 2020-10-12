package net.ducksmanager.activity

import android.os.Bundle
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.UserFeedback
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.databinding.ReportBinding
import retrofit2.Response
import java.lang.ref.WeakReference

class Report : AppCompatActivityWithDrawer() {
    private lateinit var binding: ReportBinding

    override fun shouldShowToolbar() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggleToolbar()

        binding.reportSend.setOnClickListener {
            DmServer.api.sendFeedback(UserFeedback(binding.reportInput.text.toString()))
                .enqueue(object: DmServer.Callback<Void>("sendFeedback", this, true) {
                    override fun onSuccessfulResponse(response: Response<Void>) {
                        WhatTheDuck.info(WeakReference(this@Report), R.string.thanks_report, 3000)
                        finish()
                    }
                })
        }
    }
}
