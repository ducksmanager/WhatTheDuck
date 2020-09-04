package net.ducksmanager.util

import android.app.Activity
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.UserMessage
import net.ducksmanager.persistence.models.dm.NotificationCountry
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import retrofit2.Response
import java.lang.ref.WeakReference
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class Settings {

    companion object {
        const val MESSAGE_KEY_WELCOME = "welcome_message"
        const val MESSAGE_KEY_DATA_CONSUMPTION = "data_consumption"
        const val MESSAGE_KEY_WELCOME_BOOKCASE_VIEW = "welcome_bookcase_view"

        fun loadNotificationCountries(originActivity: Activity, callback: () -> Unit = {}) {
            DmServer.api.userNotificationCountries.enqueue(object : DmServer.Callback<List<String>>(DmServer.EVENT_GET_USER_NOTIFICATION_COUNTRIES, originActivity) {
                override fun onSuccessfulResponse(response: Response<List<String>>) {
                    appDB!!.notificationCountryDao().deleteAll()
                    appDB!!.notificationCountryDao().insertList(response.body()!!.map {
                        NotificationCountry(it)
                    })
                    callback()
                }

                override fun onFailureFailover() {
                    callback()
                }
            })
        }

        fun shouldShowMessage(messageKey: String?) = appDB!!.userMessageDao().findByKey(messageKey!!)?.isShown == true

        fun addToMessagesAlreadyShown(messageKey: String?) {
            appDB!!.userMessageDao().insert(UserMessage(messageKey, false))
        }

        private fun byteArray2Hex(hash: ByteArray): String {
            val formatter = Formatter()
            for (b in hash) {
                formatter.format("%02x", b)
            }
            val hex = formatter.toString()
            formatter.close()
            return hex
        }

        @JvmStatic
        fun toSHA1(text: String, activityRef: WeakReference<Activity>?): String {
            return try {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(text.toByteArray())
                byteArray2Hex(md.digest())
            } catch (e: NoSuchAlgorithmException) {
                if (activityRef != null) {
                    WhatTheDuck.alert(activityRef, R.string.internal_error, R.string.internal_error__crypting_failed)
                }
                ""
            }
        }
    }
}