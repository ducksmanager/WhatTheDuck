package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.pusher.pushnotifications.BeamsCallback;
import com.pusher.pushnotifications.PushNotifications;
import com.pusher.pushnotifications.PusherCallbackError;
import com.pusher.pushnotifications.auth.AuthData;
import com.pusher.pushnotifications.auth.BeamsTokenProvider;

import net.ducksmanager.apigateway.DmServer;
import net.ducksmanager.persistence.models.dm.Issue;
import net.ducksmanager.persistence.models.dm.Purchase;
import net.ducksmanager.persistence.models.dm.User;
import net.ducksmanager.util.Settings;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Response;
import timber.log.Timber;

import static net.ducksmanager.whattheduck.WhatTheDuck.*;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((WhatTheDuck)getApplication()).setup();
        User user = appDB.userDao().getCurrentUser();

        if (user != null) {
            DmServer.setApiDmUser(user.username);
            DmServer.setApiDmPassword(user.password);
            fetchCollection(new WeakReference<>(Login.this), CountryList.class, false);
        } else {
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
            startActivity(new Intent(this, Signup.class)
                .putExtra("username", ((EditText) Login.this.findViewById(R.id.username)).getText().toString())
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
            Login.this.startActivity(intent);
        });
    }

    private void loginAndFetchCollection() {
        String username = ((EditText)this.findViewById(R.id.username)).getText().toString();
        String password = ((EditText)this.findViewById(R.id.password)).getText().toString();
        DmServer.setApiDmUser(username);
        DmServer.setApiDmPassword(Settings.toSHA1(password, new WeakReference<>(this)));
        WeakReference<Activity> activityRef = new WeakReference<>(this);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            WhatTheDuck.alert(activityRef, R.string.input_error, R.string.input_error__empty_credentials);
            ProgressBar mProgressBar = this.findViewById(R.id.progressBar);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            this.findViewById(R.id.login_form).setVisibility(View.VISIBLE);
        }
        else {
            fetchCollection(activityRef, CountryList.class, true);
        }
    }

    public static void fetchCollection(WeakReference<Activity> activityRef, Class targetClass, Boolean alertIfError) {
        DmServer.api.getUserIssues().enqueue(new DmServer.Callback<List<Issue>>("retrieveCollection", activityRef.get(), alertIfError) {
            public void onSuccessfulResponse(Response<List<Issue>> issueListResponse) {
                User user = new User(DmServer.apiDmUser, DmServer.apiDmPassword);
                appDB.userDao().insert(user);

                String apiEndpointUrl = config.getProperty(CONFIG_KEY_API_ENDPOINT_URL);
                tokenProvider = new BeamsTokenProvider(
                    apiEndpointUrl +"/collection/notification_token",
                    () -> new AuthData(
                        DmServer.getRequestHeaders(true),
                        new HashMap<>()
                    )
                );

                if (!isTestContext(apiEndpointUrl)) {
                    try {
                        PushNotifications.start(activityRef.get(), config.getProperty(CONFIG_KEY_PUSHER_INSTANCE_ID));
                        PushNotifications.setUserId(user.username, tokenProvider, new BeamsCallback<Void, PusherCallbackError>() {
                            @Override
                            public void onSuccess(@NonNull Void... values) {
                                Timber.i("Successfully authenticated with Pusher Beams");
                            }

                            @Override
                            public void onFailure(PusherCallbackError error) {
                                Timber.i("PusherBeams : Pusher Beams authentication failed: %s", error.getMessage());
                            }
                        });
                    }
                    catch(Exception e) {
                        Timber.e("Pusher init failed : %s", e.getMessage());
                    }
                }

                appDB.issueDao().deleteAll();
                appDB.issueDao().insertList(issueListResponse.body());

                DmServer.api.getUserPurchases().enqueue(new DmServer.Callback<List<Purchase>>("getPurchases", activityRef.get()) {
                    @Override
                    public void onSuccessfulResponse(Response<List<Purchase>> purchaseListResponse) {
                        appDB.purchaseDao().deleteAll();
                        appDB.purchaseDao().insertList(purchaseListResponse.body());

                        ItemList.type = CollectionType.USER.toString();
                        activityRef.get().startActivity(new Intent(activityRef.get(), targetClass));
                    }
                });
            }

            @Override
            public void onErrorResponse(Response<List<Issue>> response) {
                if (!alertIfError) {
                    activityRef.get().startActivity(new Intent(activityRef.get(), Login.class));
                }
            }
        });
    }

    private void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
