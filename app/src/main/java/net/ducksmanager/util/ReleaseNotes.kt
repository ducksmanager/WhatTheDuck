package net.ducksmanager.util

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import net.ducksmanager.persistence.models.composite.UserMessage
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import java.lang.ref.WeakReference

class ReleaseNotes private constructor(private val majorVersion: String, private val messageId: Int, private val videoId: Int?) {
    companion object {
        val current: ReleaseNotes = ReleaseNotes("2.8", R.string.new_features_28_text, R.raw.demo_multiple_copy_selection)
    }

    fun showOnVersionUpdate(originActivityRef: WeakReference<Activity?>) {
        if (Settings.shouldShowMessage(getMessageId())) {
            val originActivity = originActivityRef.get()
            val builder = AlertDialog.Builder(originActivity)
            val factory = LayoutInflater.from(originActivity)
            val view = factory.inflate(R.layout.release_notes, null)
            builder.setView(view)
            builder.setTitle(originActivity!!.getString(R.string.new_feature))
            builder.setNeutralButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                appDB!!.userMessageDao().insert(UserMessage(getMessageId(), false))
                dialogInterface.dismiss()
            }
            (view.findViewById<View>(R.id.text) as TextView).setText(messageId)
            if (videoId != null) {
                val uri = Uri.parse("android.resource://" + WhatTheDuck.applicationContext!!.packageName + "/" + videoId)
                val videoView = view.findViewById<VideoView>(R.id.video)
                videoView.setVideoURI(uri)
                videoView.setOnPreparedListener { it.isLooping = true }
                videoView.start()
            } else {
                view.findViewById<ImageView>(R.id.video).visibility = View.GONE
            }
            builder.show()
        }
    }

    private fun getMessageId(): String = "release_notes_$majorVersion"

}