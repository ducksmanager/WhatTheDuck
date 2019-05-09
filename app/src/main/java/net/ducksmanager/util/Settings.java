package net.ducksmanager.util;

import android.text.TextUtils;

import net.ducksmanager.persistence.models.composite.UserMessage;
import net.ducksmanager.persistence.models.dm.User;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Objects;
import java.util.Properties;

public class Settings {
    @Deprecated
    public static String USER_SETTINGS = "settings.properties";

    public static final String MESSAGE_KEY_WELCOME = "welcome_message";
    public static final String MESSAGE_KEY_DATA_CONSUMPTION = "data_consumption";
    public static final String MESSAGE_KEY_WELCOME_BOOKCASE_VIEW = "welcome_bookcase_view";

    public static void migrateUserSettingsToDbIfExist() {
        Properties props=new Properties();
        InputStream inputStream;
        try {
            inputStream = WhatTheDuck.wtd.openFileInput(USER_SETTINGS);
            props.load(inputStream);
            if (props.containsKey("username")) {
                WhatTheDuck.appDB.userDao().deleteAll();
                WhatTheDuck.appDB.userDao().insert(new User((String) Objects.requireNonNull(props.get("username")), (String)props.get("password")));
            }
            String propertyValue = (String) props.get("messagesAlreadyShown");
            if (propertyValue != null) {
                WhatTheDuck.appDB.userMessageDao().deleteAll();
                for (String messageKey : TextUtils.split(propertyValue, ",")) {
                    WhatTheDuck.appDB.userMessageDao().insert(new UserMessage(messageKey, false));
                }
            }
            inputStream.close();
            WhatTheDuck.wtd.deleteFile(USER_SETTINGS);
        } catch (IOException e) {
            System.out.println("No user settings found");
        }
    }

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
