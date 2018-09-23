package net.ducksmanager.retrievetasks;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import net.ducksmanager.util.Settings;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import static net.ducksmanager.whattheduck.WhatTheDuck.trackEvent;

public class Signup extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

        setContentView(R.layout.signup);
        
        setTitle(R.string.app_name);

        EditText usernameField = findViewById(R.id.username_signup);
        EditText passwordField = findViewById(R.id.password_signup);
        EditText passwordConfirmationField = findViewById(R.id.password_confirmation);
        EditText emailField = findViewById(R.id.email_address);

        Button endSignupButton = findViewById(R.id.end_signup);
        Button cancelSignupButton = findViewById(R.id.cancel_signup);

        usernameField.setText(Settings.getUsername());
        passwordField.setText(Settings.getPassword());

        endSignupButton.setOnClickListener(view -> {
            Settings.setUsername(usernameField.getText().toString());
            Settings.setPassword(passwordField.getText().toString());

            String password2 = Settings.toSHA1(passwordConfirmationField.getText().toString());
            String email = emailField.getText().toString();

            new ConnectAndRetrieveList(Signup.this, "&action=signup&mdp_user2="+password2+"&email="+email).execute();
        });
        
        cancelSignupButton.setOnClickListener(view -> {
            Intent i = new Intent(Signup.this, WhatTheDuck.class);
            Signup.this.startActivity(i);
        });
    }


    protected static class ConnectAndRetrieveList extends RetrieveTask {

        ConnectAndRetrieveList(Activity originActivity, String urlSuffix) {
            super(urlSuffix, new WeakReference<>(originActivity));
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            trackEvent("signup/finish");
            if (response != null) {
                try {
                    response = new String(response.getBytes("ISO8859-1"), "UTF-8");

                    if (response.equals("OK")) {
                        Activity originActivity = originActivityRef.get();
                        Intent i = new Intent(originActivity, WhatTheDuck.class);
                        WhatTheDuck.wtd.startActivity(i);

                        EditText usernameField = WhatTheDuck.wtd.findViewById(R.id.username);
                        EditText passwordField = WhatTheDuck.wtd.findViewById(R.id.password);
                        usernameField.setText(Settings.getUsername());
                        passwordField.setText(Settings.getPassword());

                        WhatTheDuck.wtd.info(originActivityRef, R.string.signup__confirm);
                    } else {
                        WhatTheDuck.wtd.alert(originActivityRef, response);
                    }
                } catch (UnsupportedEncodingException e) {
                    WhatTheDuck.wtd.alert(originActivityRef, R.string.internal_error);
                }
            }
        }
    }
}
