package net.ducksmanager.whattheduck;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import net.ducksmanager.api.DmServer;
import net.ducksmanager.persistence.models.composite.UserToCreate;
import net.ducksmanager.util.Settings;

import java.lang.ref.WeakReference;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Response;

public class Signup extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WhatTheDuck) getApplication()).trackActivity(this);

        setContentView(R.layout.signup);
        
        setTitle(R.string.app_name);

        EditText usernameField = findViewById(R.id.username_signup);
        EditText passwordField = findViewById(R.id.password_signup);
        EditText passwordConfirmationField = findViewById(R.id.password_confirmation);
        EditText emailField = findViewById(R.id.email_address);

        Button endSignupButton = findViewById(R.id.end_signup);
        Button cancelSignupButton = findViewById(R.id.cancel_signup);

        String defaultUsername = getIntent().getStringExtra("username");
        if (defaultUsername != null) {
            usernameField.setText(defaultUsername);
        }

        endSignupButton.setOnClickListener(view -> {
            String username = usernameField.getText().toString();
            String password = passwordField.getText().toString();

            String password2 = passwordConfirmationField.getText().toString();
            String email = emailField.getText().toString();

            DmServer.api.createUser(new UserToCreate(username, password, password2, email)).enqueue(new DmServer.Callback<Void>("signup", Signup.this, true) {
                @Override
                public void onSuccessfulResponse(Response<Void> response) {
                    DmServer.setApiDmUser(username);
                    DmServer.setApiDmPassword(Settings.toSHA1(password, new WeakReference<>(Signup.this)));
                    Login.fetchCollection(new WeakReference<>(Signup.this), CountryList.class, true);
                }
            });
        });
        
        cancelSignupButton.setOnClickListener(view -> {
            Intent i = new Intent(Signup.this, Login.class);
            Signup.this.startActivity(i);
        });
    }
}
