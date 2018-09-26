package net.ducksmanager.util

import android.content.Context
import android.text.TextUtils
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object Settings {
    var USER_SETTINGS = "settings.properties"
    var username: String? = null

    var password: String? = null
        set(password) {
            Settings.password = password
            encryptedPassword = if (password == null) null else toSHA1(password)
        }

    private var rememberCredentials: Boolean? = false
        set
    var encryptedPassword: String? = null
    private var messagesAlreadyShown: MutableSet<String> = HashSet()

    val MESSAGE_KEY_WELCOME = "welcome_message"
    val MESSAGE_KEY_DATA_CONSUMPTION = "data_consumption"
    val MESSAGE_KEY_WELCOME_BOOKCASE_VIEW = "welcome_bookcase_view"

    fun loadUserSettings() {
        val props = Properties()
        val inputStream: InputStream
        try {
            inputStream = WhatTheDuck.wtd!!.openFileInput(USER_SETTINGS)
            props.load(inputStream)
            username = props["username"] as String
            encryptedPassword = props["password"] as String
            loadAlreadyShownMessages(props)
            rememberCredentials = true
            inputStream.close()
        } catch (e: IOException) {
            println("No user settings found")
            messagesAlreadyShown = HashSet()
        }

    }

    fun saveSettings() {
        val props = Properties()

        if (rememberCredentials!!) {
            if (username != null) {
                props["username"] = username
            }
            if (encryptedPassword != null) {
                props["password"] = encryptedPassword
            }
        } else {
            props.remove("username")
            props.remove("password")
        }
        saveAlreadyShownMessages(props)

        val outputStream: FileOutputStream
        try {
            outputStream = WhatTheDuck.wtd!!.openFileOutput(USER_SETTINGS, Context.MODE_PRIVATE)
            props.store(outputStream, "WhatTheDuck user settings")
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun loadAlreadyShownMessages(properties: Properties) {
        val propertyValue = properties["messagesAlreadyShown"] as String
        messagesAlreadyShown = HashSet()
        messagesAlreadyShown.addAll(Arrays.asList(*TextUtils.split(propertyValue, ",")))
    }

    private fun saveAlreadyShownMessages(properties: Properties) {
        properties.setProperty("messagesAlreadyShown", TextUtils.join(",", messagesAlreadyShown))
    }

    fun shouldShowMessage(messageKey: String): Boolean {
        return !messagesAlreadyShown.contains(messageKey)
    }

    fun addToMessagesAlreadyShown(messageKey: String) {
        messagesAlreadyShown.add(messageKey)
    }

    fun byteArray2Hex(hash: ByteArray): String {
        val formatter = Formatter()
        for (b in hash) {
            formatter.format("%02x", b)
        }
        val hex = formatter.toString()
        formatter.close()
        return hex
    }

    fun toSHA1(text: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-1")
            md.update(text.toByteArray())
            byteArray2Hex(md.digest())
        } catch (e: NoSuchAlgorithmException) {
            WhatTheDuck.wtd!!.alert(R.string.internal_error,
                    R.string.internal_error__crypting_failed)
            ""
        }

    }
}
