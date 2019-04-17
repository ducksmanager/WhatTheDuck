package net.ducksmanager.util;

import net.ducksmanager.persistence.models.composite.UserMessage;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Settings {
    public static final String MESSAGE_KEY_WELCOME = "welcome_message";
    public static final String MESSAGE_KEY_DATA_CONSUMPTION = "data_consumption";
    public static final String MESSAGE_KEY_WELCOME_BOOKCASE_VIEW = "welcome_bookcase_view";
    public static final String SETTING_KEY_REMEMBER_CREDENTIALS = "remember_credentials";

    public static boolean shouldShowMessage(String messageKey) {
        UserMessage userMessageForReleaseNotes = WhatTheDuck.appDB.userMessageDao().findByKey(messageKey);
        return userMessageForReleaseNotes != null && userMessageForReleaseNotes.isShown();
    }

    public static void addToMessagesAlreadyShown(String messageKey) {
        WhatTheDuck.appDB.userMessageDao().insert(new UserMessage(messageKey, false));
    }

    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String hex = formatter.toString();
        formatter.close();
        return hex;
    }

    public static String toSHA1(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(text.getBytes());
            return byteArray2Hex(md.digest());
        }
        catch (NoSuchAlgorithmException e) {
            WhatTheDuck.wtd.alert(R.string.internal_error,
                               R.string.internal_error__crypting_failed);
            return "";
        }
    }
}
