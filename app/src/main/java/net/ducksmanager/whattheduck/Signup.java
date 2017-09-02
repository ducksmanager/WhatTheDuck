package net.ducksmanager.whattheduck;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.UnsupportedEncodingException;

public class Signup extends Activity {


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        
        setTitle(R.string.app_name);

        ((EditText) Signup.this.findViewById(R.id.username_signup)).setText(WhatTheDuck.getUsername());
        ((EditText) Signup.this.findViewById(R.id.password_signup)).setText(WhatTheDuck.getPassword());

        Button endSignupButton = (Button) findViewById(R.id.end_signup);
        endSignupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                WhatTheDuck.setUsername(((EditText) Signup.this.findViewById(R.id.username_signup)).getText().toString());
                WhatTheDuck.setPassword(((EditText) Signup.this.findViewById(R.id.password_signup)).getText().toString());

                String password2 = WhatTheDuck.toSHA1(((EditText) Signup.this.findViewById(R.id.password_confirmation)).getText().toString());
                String email = ((EditText) Signup.this.findViewById(R.id.email_address)).getText().toString();

                new ConnectAndRetrieveList("&action=signup&mdp_user2="+password2+"&email="+email).execute();
            }
        });
        
        Button cancelSignupButton = (Button) findViewById(R.id.cancel_signup);
        cancelSignupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(Signup.this, WhatTheDuck.class);
                Signup.this.startActivity(i);
            }
        });
    }


    public class ConnectAndRetrieveList extends RetrieveTask {

        public ConnectAndRetrieveList(String urlSuffix) {
            super(urlSuffix, null);
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    response = new String(response.getBytes("ISO8859-1"), "UTF-8");

                    if (response.equals("OK")) {
                        Intent i = new Intent(Signup.this, WhatTheDuck.class);
                        WhatTheDuck.wtd.startActivity(i);
                        ((EditText) WhatTheDuck.wtd.findViewById(R.id.username)).setText(WhatTheDuck.getUsername());
                        ((EditText) WhatTheDuck.wtd.findViewById(R.id.password)).setText(WhatTheDuck.getPassword());
                        WhatTheDuck.wtd.info(WhatTheDuck.wtd, R.string.signup__confirm);
                    } else {
                        WhatTheDuck.wtd.alert(Signup.this, response);
                    }
                } catch (UnsupportedEncodingException e) {
                    WhatTheDuck.wtd.alert(Signup.this, getString(R.string.internal_error));
                }
            }
        }
    }
}
