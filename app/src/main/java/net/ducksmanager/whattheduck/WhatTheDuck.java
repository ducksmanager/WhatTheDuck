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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.ducksmanager.apigateway.DmServer;
import net.ducksmanager.persistence.AppDatabase;
import net.ducksmanager.persistence.models.dm.Issue;
import net.ducksmanager.persistence.models.dm.Purchase;
import net.ducksmanager.persistence.models.dm.User;
import net.ducksmanager.retrievetasks.Signup;
import net.ducksmanager.util.Settings;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import retrofit2.Response;

import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_DM_URL;

public class WhatTheDuck extends AppCompatActivity {

    public static WhatTheDuck wtd;

    public static String DB_NAME = "appDB";
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
        DmServer.initApi();
        appDB = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, DB_NAME)
            .allowMainThreadQueries()
            .build();

        Settings.migrateUserSettingsToDbIfExist();
        loadUser();
    }

    public void loadUser() {
        User user = WhatTheDuck.appDB.userDao().getCurrentUser();

        if (user != null) {
            DmServer.setApiDmUser(user.getUsername());
            DmServer.setApiDmPassword(user.getPassword());
            fetchCollection(new WeakReference<>(this), CountryList.class, false);
        }
        else {
            setContentView(R.layout.whattheduck);
            showLoginForm();
        }
    }

    private void showLoginForm() {
        findViewById(R.id.login_form).setVisibility(View.VISIBLE);

        ((EditText)findViewById(R.id.username)).addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                ((EditText) findViewById(R.id.password)).setText("");
            }
        });

        Button signupButton = findViewById(R.id.end_signup);
        signupButton.setOnClickListener(view ->
            wtd.startActivity(new Intent(wtd, Signup.class)
                .putExtra("username", ((EditText) WhatTheDuck.this.findViewById(R.id.username)).getText().toString())
            )
        );

        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(view -> {
            hideKeyboard(view);
            loginAndFetchCollection();
        });

        TextView linkToDM = findViewById(R.id.linkToDM);
        linkToDM.setOnClickListener(view -> {
            hideKeyboard(view);
            final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getDmUrl()));
            WhatTheDuck.this.startActivity(intent);
        });
    }

    private void loginAndFetchCollection() {
        String username = ((EditText)this.findViewById(R.id.username)).getText().toString();
        String password = ((EditText)this.findViewById(R.id.password)).getText().toString();
        DmServer.setApiDmUser(username);
        DmServer.setApiDmPassword(Settings.toSHA1(password));

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            WhatTheDuck.wtd.alert(R.string.input_error, R.string.input_error__empty_credentials);
            ProgressBar mProgressBar = this.findViewById(R.id.progressBar);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            this.findViewById(R.id.login_form).setVisibility(View.VISIBLE);
        }
        else {
            fetchCollection(new WeakReference<>(this), CountryList.class, true);
        }
    }

    public void fetchCollection(WeakReference<Activity> activityRef, Class targetClass, Boolean alertIfError) {
        DmServer.api.getUserIssues().enqueue(new DmServer.Callback<List<Issue>>("retrieveCollection", activityRef.get(), alertIfError) {
            public void onSuccessfulResponse(Response<List<Issue>> issueListResponse) {
                appDB.userDao().insert(new User(DmServer.apiDmUser, DmServer.apiDmPassword));

                appDB.issueDao().deleteAll();
                appDB.issueDao().insertList(issueListResponse.body());

                DmServer.api.getUserPurchases().enqueue(new DmServer.Callback<List<Purchase>>("getPurchases", WhatTheDuck.wtd) {
                    @Override
                    public void onSuccessfulResponse(Response<List<Purchase>> purchaseListResponse) {
                        appDB.purchaseDao().deleteAll();
                        appDB.purchaseDao().insertList(purchaseListResponse.body());

                        ItemList.type = WhatTheDuck.CollectionType.USER.toString();
                        activityRef.get().startActivity(new Intent(activityRef.get(), targetClass));
                    }
                });
            }

            @Override
            public void onErrorResponse(Response<List<Issue>> response) {
                if (!alertIfError) {
                    setContentView(R.layout.whattheduck);
                    showLoginForm();
                }
            }
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

    private void alert(WeakReference<Activity> activity, int titleId, int messageId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity.get());
        builder.setTitle(getString(titleId));
        builder.setMessage(getString(messageId));

        this.runOnUiThread(() ->
            builder.create().show()
        );
    }

    public void alert(int titleId, int messageId) {
        alert(new WeakReference<>(this), titleId, messageId);
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
        System.out.println(text);
        if (wtd != null) {
            ((WhatTheDuckApplication) wtd.getApplication()).trackEvent(text);
        }
    }
}
