package net.ducksmanager.util;

import android.content.Context;
import android.text.TextUtils;

import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class Settings {
    public static String USER_SETTINGS = "settings.properties";
    public static String username = null;
    public static String password = null;
    private static Boolean rememberCredentials = false;
    public static String encryptedPassword = null;
    private static Set<String> messagesAlreadyShown = new HashSet<>();

    public static final String MESSAGE_KEY_WELCOME = "welcome_message";
    public static final String MESSAGE_KEY_DATA_CONSUMPTION = "data_consumption";
    public static final String MESSAGE_KEY_WELCOME_BOOKCASE_VIEW_LANDSCAPE = "welcome_bookcase_view";

    public static void loadUserSettings() {
        Properties props=new Properties();
        InputStream inputStream;
        try {
            inputStream = WhatTheDuck.wtd.openFileInput(USER_SETTINGS);
            props.load(inputStream);
            setUsername((String)props.get("username"));
            setEncryptedPassword((String)props.get("password"));
            loadAlreadyShownMessages(props);
            setRememberCredentials(true);
            inputStream.close();
        } catch (IOException e) {
            System.out.println("No user settings found");
            messagesAlreadyShown = new HashSet<>();
        }
    }

    public static void saveSettings() {
        Properties props=new Properties();

        if (getRememberCredentials()) {
            if (getUsername() != null) {
                props.put("username", getUsername());
            }
            if (getEncryptedPassword() != null) {
                props.put("password", getEncryptedPassword());
            }
        } else {
            props.remove("username");
            props.remove("password");
        }
        saveAlreadyShownMessages(props);

        FileOutputStream outputStream;
        try {
            outputStream = WhatTheDuck.wtd.openFileOutput(USER_SETTINGS, Context.MODE_PRIVATE);
            props.store(outputStream, "WhatTheDuck user settings");
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Settings.username = username;
    }

    private static Boolean getRememberCredentials() {
        return rememberCredentials;
    }

    public static void setRememberCredentials(Boolean rememberCredentials) {
        Settings.rememberCredentials = rememberCredentials;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Settings.password = password;
        setEncryptedPassword(password == null ? null : toSHA1(password));
    }

    public static String getEncryptedPassword() {
        return encryptedPassword;
    }

    public static void setEncryptedPassword(String encryptedPassword) {
        Settings.encryptedPassword = encryptedPassword;
    }

    private static void loadAlreadyShownMessages(Properties properties) {
        String propertyValue = (String) properties.get("messagesAlreadyShown");
        messagesAlreadyShown = new HashSet<>();
        messagesAlreadyShown.addAll(Arrays.asList(TextUtils.split(propertyValue == null ? "" : propertyValue, ",")));
    }

    private static void saveAlreadyShownMessages(Properties properties) {
        properties.setProperty("messagesAlreadyShown", TextUtils.join(",", messagesAlreadyShown));
    }

    public static boolean shouldShowMessage(String messageKey) {
        return !messagesAlreadyShown.contains(messageKey);
    }

    public static void addToMessagesAlreadyShown(String messageKey) {
        messagesAlreadyShown.add(messageKey);
    }

    public static String byteArray2Hex(byte[] hash) {
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
