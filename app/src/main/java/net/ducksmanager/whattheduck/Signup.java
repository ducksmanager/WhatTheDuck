package net.ducksmanager.whattheduck;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

public class Signup extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

        setContentView(R.layout.signup);
        
        setTitle(R.string.app_name);

        ((EditText) Signup.this.findViewById(R.id.username_signup)).setText(WhatTheDuck.getUsername());
        ((EditText) Signup.this.findViewById(R.id.password_signup)).setText(WhatTheDuck.getPassword());

        Button endSignupButton = findViewById(R.id.end_signup);
        endSignupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                WhatTheDuck.setUsername(((EditText) Signup.this.findViewById(R.id.username_signup)).getText().toString());
                WhatTheDuck.setPassword(((EditText) Signup.this.findViewById(R.id.password_signup)).getText().toString());

                String password2 = WhatTheDuck.toSHA1(((EditText) Signup.this.findViewById(R.id.password_confirmation)).getText().toString());
                String email = ((EditText) Signup.this.findViewById(R.id.email_address)).getText().toString();

                new ConnectAndRetrieveList(Signup.this, "&action=signup&mdp_user2="+password2+"&email="+email).execute();
            }
        });
        
        Button cancelSignupButton = findViewById(R.id.cancel_signup);
        cancelSignupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(Signup.this, WhatTheDuck.class);
                Signup.this.startActivity(i);
            }
        });
    }


    public static class ConnectAndRetrieveList extends RetrieveTask {

        private final WeakReference<Activity> originActivityRef;

        public ConnectAndRetrieveList(Activity originActivity, String urlSuffix) {
            super(urlSuffix, null);
            this.originActivityRef = new WeakReference<>(originActivity);
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("signup/finish");
            if (response != null) {
                try {
                    response = new String(response.getBytes("ISO8859-1"), "UTF-8");

                    if (response.equals("OK")) {
                        Activity originActivity = originActivityRef.get();
                        Intent i = new Intent(originActivity, WhatTheDuck.class);
                        WhatTheDuck.wtd.startActivity(i);
                        ((EditText) WhatTheDuck.wtd.findViewById(R.id.username)).setText(WhatTheDuck.getUsername());
                        ((EditText) WhatTheDuck.wtd.findViewById(R.id.password)).setText(WhatTheDuck.getPassword());
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
