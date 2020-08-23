package net.ducksmanager.util

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import net.ducksmanager.persistence.models.composite.UserMessage
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import java.lang.ref.WeakReference

class ReleaseNotes private constructor(private val majorVersion: String, private val messageId: Int, imageId: Int) {
    companion object {
        val current: ReleaseNotes = ReleaseNotes("1.8", R.string.newFeatures18Text, R.drawable.bookcase_view_switch)
    }

    private val imageId: Int?
    fun showOnVersionUpdate(originActivityRef: WeakReference<Activity?>) {
        if (Settings.shouldShowMessage(getMessageId())) {
            val originActivity = originActivityRef.get()
            val builder = AlertDialog.Builder(originActivity)
            val factory = LayoutInflater.from(originActivity)
            val view = factory.inflate(R.layout.release_notes, null)
            builder.setView(view)
            builder.setTitle(originActivity!!.getString(R.string.newFeature))
            builder.setNeutralButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                appDB!!.userMessageDao().insert(UserMessage(getMessageId(), false))
                dialogInterface.dismiss()
            }
            (view.findViewById<View>(R.id.text) as TextView).setText(messageId)
            if (imageId != null) {
                view.findViewById<ImageView>(R.id.image).setImageResource(imageId)
            } else {
                view.findViewById<ImageView>(R.id.image).visibility = View.GONE
            }
            builder.show()
        }
    }

    private fun getMessageId(): String = "release_notes_$majorVersion"

    init {
        this.imageId = imageId
    }
}