package net.ducksmanager.util

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import net.ducksmanager.whattheduck.R

import java.lang.ref.WeakReference

class ReleaseNotes private constructor(private val majorVersion: String, val messageId: Int?, private val imageId: Int?) {

    fun showOnVersionUpdate(originActivityRef: WeakReference<Activity>) {
        val originActivity = originActivityRef.get()
        if (shouldShowMessage()) {
            val builder = AlertDialog.Builder(originActivity)
            val factory = LayoutInflater.from(originActivity)
            val view = factory.inflate(R.layout.release_notes, null)
            builder.setView(view)

            builder.setTitle(originActivity?.getString(R.string.newFeature))

            builder.setNeutralButton(R.string.ok) { dialogInterface, i ->
                addToMessagesAlreadyShown()
                dialogInterface.dismiss()
            }

            (view.findViewById<View>(R.id.text) as TextView).setText(messageId!!)

            if (imageId != null) {
                (view.findViewById<View>(R.id.image) as ImageView).setImageResource(imageId)
            } else {
                view.findViewById<View>(R.id.image).visibility = View.GONE
            }

            builder.show()
        }
    }

    private fun shouldShowMessage(): Boolean {
        return Settings.shouldShowMessage(getMessageId())
    }

    private fun addToMessagesAlreadyShown() {
        Settings.addToMessagesAlreadyShown(getMessageId())
    }

    private fun getMessageId(): String {
        return "release_notes_$majorVersion"
    }

    companion object {

        var current: ReleaseNotes = ReleaseNotes("1.8", R.string.newFeatures18Text, R.drawable.bookcase_view_switch)

    }
}
