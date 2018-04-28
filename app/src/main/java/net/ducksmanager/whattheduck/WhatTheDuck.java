package net.ducksmanager.whattheduck;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders.Any.B;

import net.ducksmanager.retrievetasks.ConnectAndRetrieveList;
import net.ducksmanager.retrievetasks.Signup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Locale;
import java.util.Properties;

import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL;
import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_DM_URL;

public class WhatTheDuck extends Activity {
    private static final String SERVER_PAGE="WhatTheDuck.php";

    @VisibleForTesting
    public static final String DUCKSMANAGER_PAGE_WITH_REMOTE_URL="WhatTheDuck_server.php";

    public static String USER_SETTINGS = "settings.properties";

    private static String serverURL;

    private static String username = null;
    private static String password = null;
    private static Boolean rememberCredentials = null;
    private static String encryptedPassword = null;
    private static Boolean showWelcomeMessage = true;
    private static Boolean showCoverTooltip = true;

    public static WhatTheDuck wtd;

    public static Collection userCollection = new Collection();
    public static Collection coaCollection = new Collection();

    private static String selectedCountry = null;
    private static String selectedPublication = null;
    private static String selectedIssue = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        wtd=this;
        super.onCreate(savedInstanceState);
        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

        loadUserSettings();

        String encryptedPassword = getEncryptedPassword();

        if (encryptedPassword != null) {
            new ConnectAndRetrieveList(false).execute();
        }
        else {
            initUI();
        }
    }

    public void initUI() {
        setContentView(R.layout.whattheduck);
        ((CheckBox) findViewById(R.id.checkBoxRememberCredentials)).setChecked(username != null);

        EditText usernameEditText = findViewById(R.id.username);
        usernameEditText.setText(username);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                ((EditText) findViewById(R.id.password)).setText("");
            }
        });

        Button signupButton = findViewById(R.id.end_signup);
        signupButton.setOnClickListener(view -> {
            WhatTheDuck.setUsername(((EditText) WhatTheDuck.this.findViewById(R.id.username)).getText().toString());
            WhatTheDuck.setPassword(((EditText) WhatTheDuck.this.findViewById(R.id.password)).getText().toString());

            wtd.startActivity(new Intent(wtd, Signup.class));
        });

        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(view -> {
            hideKeyboard(view);
            new ConnectAndRetrieveList(true).execute();
        });

        TextView linkToDM = findViewById(R.id.linkToDM);
        linkToDM.setOnClickListener(view -> {
            hideKeyboard(view);
            final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getDmUrl()));
            WhatTheDuck.this.startActivity(intent);
        });
    }

    private String getDmUrl() {
        return WhatTheDuckApplication.config.getProperty(CONFIG_KEY_DM_URL);
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void info(WeakReference<Activity> activity, int titleId) {
        Toast.makeText(activity.get(), titleId, Toast.LENGTH_SHORT).show();
    }

    public void alert(String message) {
        alert(new WeakReference<>(this), message);
    }

    public void alert(int messageId) {
        alert(new WeakReference<>(this), getString(messageId));
    }
    
    public void alert(WeakReference<Activity> activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
        builder.setTitle(getString(R.string.error));
        builder.setMessage(message);
        builder.create().show();
    }
    
    public void alert(WeakReference<Activity> activity, int messageId) {
        alert(activity, getString(messageId));
    }

    public void alert(WeakReference<Activity> activity, int titleId, int messageId, String extraMessage) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
        builder.setTitle(getString(titleId));
        builder.setMessage(getString(messageId)+extraMessage);

        this.runOnUiThread(() ->
            builder.create().show()
        );
    }
    
    public void alert(int titleId, int messageId, String extraMessage) {
        alert(new WeakReference<>(this), titleId, messageId, extraMessage);
    }
    
    public void alert(int titleId, int messageId) {
        alert(new WeakReference<>(this), titleId, messageId, "");
    }

    private static void loadUserSettings() {
        Properties props=new Properties();
        InputStream inputStream;
        try {
            inputStream = wtd.openFileInput(USER_SETTINGS);
            props.load(inputStream);
            setUsername((String)props.get("username"));
            setEncryptedPassword((String)props.get("password"));
            setShowWelcomeMessage(props.get("showWelcomeMessage") == null || props.get("showWelcomeMessage").equals("true"));
            setShowCoverTooltip(props.get("showCoverTooltip") == null || props.get("showCoverTooltip").equals("true"));
            setRememberCredentials(true);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveSettings(Boolean withCredentials) {
        Properties props=new Properties();

        InputStream inputStream;
        try {
            inputStream = wtd.openFileInput(USER_SETTINGS);
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (withCredentials != null) {
            if (withCredentials.equals(Boolean.TRUE)) {
                props.put("username", getUsername());
                props.put("password", getEncryptedPassword());
            } else {
                props.remove("username");
                props.remove("password");
            }
        }

        props.put("showWelcomeMessage", getShowWelcomeMessage().toString());
        props.put("showCoverTooltip", getShowCoverTooltip().toString());

        FileOutputStream outputStream;
        try {
            outputStream = wtd.openFileOutput(USER_SETTINGS, MODE_PRIVATE);
            props.store(outputStream, "");
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        WhatTheDuck.username = username;
    }

    public static Boolean getRememberCredentials() {
        return rememberCredentials;
    }

    public static void setRememberCredentials(Boolean rememberCredentials) {
        WhatTheDuck.rememberCredentials = rememberCredentials;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        WhatTheDuck.password = password;
        if (password == null) {
            setEncryptedPassword(null);
        }
        else {
            setEncryptedPassword(toSHA1(password));
        }
    }

    public static String getEncryptedPassword() {
        return encryptedPassword;
    }

    private static void setEncryptedPassword(String encryptedPassword) {
        WhatTheDuck.encryptedPassword = encryptedPassword;
    }

    public static Boolean getShowWelcomeMessage() {
        return showWelcomeMessage;
    }

    public static void setShowWelcomeMessage(Boolean showWelcomeMessage) {
        WhatTheDuck.showWelcomeMessage = showWelcomeMessage;
    }

    public static Boolean getShowCoverTooltip() {
        return showCoverTooltip;
    }

    public static void setShowCoverTooltip(Boolean showCoverTooltip) {
        WhatTheDuck.showCoverTooltip = showCoverTooltip;
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

    public void retrieveOrFailDmServer(String urlSuffix, FutureCallback<String> futureCallback, String fileName, File file) throws Exception {
        if (isOffline()) {
            throw new Exception(getString(R.string.network_error));
        }

        if (getEncryptedPassword() == null) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(getPassword().getBytes());
            setEncryptedPassword(byteArray2Hex(md.digest()));
        }

        urlSuffix = urlSuffix.replaceAll("\\{locale\\}", getApplicationContext().getResources().getConfiguration().locale.getLanguage());

        B call = Ion.with(this.findViewById(android.R.id.content).getContext())
            .load(WhatTheDuckApplication.config.getProperty(CONFIG_KEY_API_ENDPOINT_URL) + urlSuffix)
            .setHeader("x-dm-version", WhatTheDuck.wtd.getApplicationVersion());

        if (file != null) {
            call.setMultipartFile(fileName, file);
        }

        call.asString().setCallback(futureCallback);
    }

    public String retrieveOrFail(RetrieveTask.DownloadHandler downloadHandler, String urlSuffix) throws Exception {
        if (isOffline()) {
            throw new Exception(""+R.string.network_error);
        }

        if (getEncryptedPassword() == null) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(getPassword().getBytes());
            setEncryptedPassword(byteArray2Hex(md.digest()));
        }

        if (serverURL == null) {
            serverURL = downloadHandler.getPage(getDmUrl()+"/"+DUCKSMANAGER_PAGE_WITH_REMOTE_URL);
        }

        String response = downloadHandler.getPage(serverURL+"/"+SERVER_PAGE
                                      + "?pseudo_user="+URLEncoder.encode(username, "UTF-8")
                                      + "&mdp_user="+encryptedPassword
                                      + "&mdp="+ WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_SECURITY_PASSWORD)
                                      + "&version="+getApplicationVersion()
                                      + "&language="+ Locale.getDefault().getLanguage()
                                      + urlSuffix);

        response = response.replaceAll("/\\/", "");
        if (response.equals("0")) {
            throw new SecurityException();
        }
        else
            return response;
    }

    public void toggleProgressbarLoading(WeakReference<Activity> activity, int progressBarId, boolean toggle) {
        ProgressBar progressBar = activity.get().findViewById(progressBarId);

        if (progressBar != null) {
            if (toggle) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }
            else {
                progressBar.clearAnimation();
                progressBar.setVisibility(ProgressBar.GONE);
            }
        }
    }

    public void toggleProgressbarLoading(int progressBarId, boolean toggle) {
        toggleProgressbarLoading(new WeakReference<>(WhatTheDuck.wtd), progressBarId, toggle);
    }

    private boolean isOffline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo == null || !netInfo.isConnected();
    }

    public String getApplicationVersion() throws NameNotFoundException {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
        return info.versionName;
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

    public static String getSelectedCountry() {
        return WhatTheDuck.selectedCountry;
    }

    public static void setSelectedCountry(String selectedCountry) {
        WhatTheDuck.selectedCountry = selectedCountry;
    }

    public static String getSelectedPublication() {
        return WhatTheDuck.selectedPublication;
    }

    public static void setSelectedPublication(String selectedPublication) {
        WhatTheDuck.selectedPublication = selectedPublication;
    }

    public static String getSelectedIssue() {
        return WhatTheDuck.selectedIssue;
    }

    public static void setSelectedIssue(String selectedIssue) {
        WhatTheDuck.selectedIssue = selectedIssue;
    }

    public static void showAbout(Activity originActivity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(originActivity);
        builder.setTitle(originActivity.getString(R.string.about));
        try {
            String versionNumber = originActivity.getPackageManager().getPackageInfo(originActivity.getPackageName(), 0).versionName;
            builder.setMessage(originActivity.getString(R.string.version) + " " + versionNumber + "\n\n" + originActivity.getString(R.string.rate_app));
            builder.create().show();
        } catch (NameNotFoundException ignored) {
        }
    }

    @Override
    public void onBackPressed() {
    }
}
