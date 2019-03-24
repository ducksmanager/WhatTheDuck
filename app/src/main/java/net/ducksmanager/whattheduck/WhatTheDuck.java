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
import android.text.Editable;
import android.text.TextUtils;
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

import net.ducksmanager.apigateway.DmServer;
import net.ducksmanager.persistence.AppDatabase;
import net.ducksmanager.persistence.models.dm.Issue;
import net.ducksmanager.retrievetasks.Signup;
import net.ducksmanager.util.Settings;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;

import androidx.annotation.VisibleForTesting;
import androidx.room.Room;
import retrofit2.Response;

import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL;
import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_DM_URL;

public class WhatTheDuck extends Activity {
    @VisibleForTesting
    public static final String WTD_PAGE_PATH = "remote/WhatTheDuck.php";

    public static WhatTheDuck wtd;

    public static Collection userCollection = new Collection();
    public static AppDatabase appDB;

    private static String selectedCountry = null;
    private static String selectedPublication = null;
    private static String selectedIssue = null;

    public enum CollectionType {COA,USER}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        wtd=this;
        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

        super.onCreate(savedInstanceState);

        Settings.loadUserSettings();

        String encryptedPassword = Settings.getEncryptedPassword();
        DmServer.initApi();
        appDB = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "appDB")
            .allowMainThreadQueries()
            .build();

        setContentView(R.layout.whattheduck);
        if (encryptedPassword != null) {
            connectAndFetchCollection();
        }
        else {
            showLoginForm();
        }
    }

    public void showLoginForm() {
        findViewById(R.id.login_form).setVisibility(View.VISIBLE);
        ((CheckBox) findViewById(R.id.checkBoxRememberCredentials)).setChecked(Settings.username != null);

        EditText usernameEditText = findViewById(R.id.username);
        usernameEditText.setText(Settings.username);
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
            Settings.setUsername(((EditText) WhatTheDuck.this.findViewById(R.id.username)).getText().toString());
            Settings.setPassword(((EditText) WhatTheDuck.this.findViewById(R.id.password)).getText().toString());

            wtd.startActivity(new Intent(wtd, Signup.class));
        });

        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(view -> {
            hideKeyboard(view);
            if (Settings.getUsername() == null
                || !Settings.getUsername().equals(((EditText) this.findViewById(R.id.username)).getText().toString())
                || Settings.getEncryptedPassword() == null) {

                Settings.setUsername(((EditText) this.findViewById(R.id.username)).getText().toString());
                Settings.setPassword(((EditText) this.findViewById(R.id.password)).getText().toString());
                Settings.setRememberCredentials(((CheckBox) this.findViewById(R.id.checkBoxRememberCredentials)).isChecked());
            }
            connectAndFetchCollection();
        });

        TextView linkToDM = findViewById(R.id.linkToDM);
        linkToDM.setOnClickListener(view -> {
            hideKeyboard(view);
            final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getDmUrl()));
            WhatTheDuck.this.startActivity(intent);
        });
    }

    void connectAndFetchCollection() {
        if (TextUtils.isEmpty(Settings.getUsername()) || TextUtils.isEmpty(Settings.getPassword()) && TextUtils.isEmpty(Settings.getEncryptedPassword())) {
            WhatTheDuck.wtd.alert(R.string.input_error, R.string.input_error__empty_credentials);
            ProgressBar mProgressBar = this.findViewById(R.id.progressBar);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            this.findViewById(R.id.login_form).setVisibility(View.VISIBLE);
        }
        else {
            toggleProgressbarLoading(true);
            trackEvent("retrievecollection/start");
            DmServer.api.getUserIssues().enqueue(new DmServer.Callback<List<Issue>>(this.findViewById(R.id.progressBar)) {
                public void onSuccessfulResponse(Response<List<Issue>> response) {
                    trackEvent("retrievecollection/finish");
                    appDB.issueDao().insertList(response.body());

                    ItemList.type = WhatTheDuck.CollectionType.USER.toString();
                    WhatTheDuck.this.startActivity(new Intent(WhatTheDuck.this, CountryList.class));
                }
            });
        }
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

    public void info(WeakReference<Activity> activity, int titleId, int duration) {
        Toast.makeText(activity.get(), titleId, duration).show();
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

    public void retrieveOrFailDmServer(String urlSuffix, FutureCallback<String> futureCallback, String fileName, File file) throws Exception {
        if (isOffline()) {
            throw new Exception(getString(R.string.network_error));
        }

        if (Settings.getEncryptedPassword() == null) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(Settings.getPassword().getBytes());
            Settings.setEncryptedPassword(Settings.byteArray2Hex(md.digest()));
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

        if (Settings.getEncryptedPassword() == null) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(Settings.getPassword().getBytes());
            Settings.setEncryptedPassword(Settings.byteArray2Hex(md.digest()));
        }

        String response = downloadHandler.getPage(getDmUrl()+"/"+ WTD_PAGE_PATH
                                      + "?pseudo_user="+URLEncoder.encode(Settings.username, "UTF-8")
                                      + "&mdp_user="+ Settings.encryptedPassword
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

    public void toggleProgressbarLoading(WeakReference<Activity> activityRef, boolean toggle) {
        ProgressBar progressBar = activityRef.get().findViewById(R.id.progressBar);

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

    public void toggleProgressbarLoading(boolean toggle) {
        toggleProgressbarLoading(new WeakReference<>(WhatTheDuck.wtd), toggle);
    }

    private boolean isOffline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo == null || !netInfo.isConnected();
    }

    public String getApplicationVersion() {
        PackageManager manager = this.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return "Unknown";
        }
        return info.versionName;
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

    public static boolean isMobileConnection() {
        ConnectivityManager cm = (ConnectivityManager)WhatTheDuck.wtd.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
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

    public static void trackEvent(String text) {
        if (wtd != null) {
            ((WhatTheDuckApplication) wtd.getApplication()).trackEvent(text);
        }
    }
}
