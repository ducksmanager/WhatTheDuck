package net.ducksmanager.whattheduck;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.ducksmanager.whattheduck.Collection.CollectionType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Locale;
import java.util.Properties;

public class WhatTheDuck extends Activity {
    private static final String SERVER_PAGE="WhatTheDuck.php";
    private static final String DUCKSMANAGER_URL="http://www.ducksmanager.net";
    private static final String DUCKSMANAGER_PAGE_WITH_REMOTE_URL="WhatTheDuck_server.php";

    public static final String CONFIG = "config.properties";
    public static final String CONFIG_KEY_SECURITY_PASSWORD = "security_password";
    public static final String CONFIG_KEY_API_ENDPOINT_URL = "api_endpoint_url";
    public static Properties config = null;

    public static final String USER_SETTINGS = "settings.properties";

    private static String serverURL;

    private static String username = null;
    private static String password = null;
    private static String encryptedPassword = null;
    private static Boolean showWelcomeMessage = true;
    private static Boolean showCoverTooltip = true;

    public static WhatTheDuck wtd;

    public static Collection userCollection = new Collection();
    public static Collection coaCollection = new Collection();

    private static String selectedCountry = null;
    private static String selectedPublication = null;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        wtd=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whattheduck);

        loadConfig(getAssets());

        loadUserSettings();
        String username = getUsername();
        String encryptedPassword = getEncryptedPassword();

        ((CheckBox) findViewById(R.id.checkBoxRememberCredentials)).setChecked(username != null);

        EditText usernameEditText = ((EditText) findViewById(R.id.username));
        usernameEditText.setText(username);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count,    int after) {}
            public void afterTextChanged(Editable s) {
                ((EditText) findViewById(R.id.password)).setText("");
            }
        });
        if (encryptedPassword != null) {
            ((EditText) findViewById(R.id.password)).setText("******");
        }


        Button signupButton = (Button) findViewById(R.id.end_signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                WhatTheDuck.setUsername(((EditText) WhatTheDuck.this.findViewById(R.id.username)).getText().toString());
                WhatTheDuck.setPassword(((EditText) WhatTheDuck.this.findViewById(R.id.password)).getText().toString());
                Intent i = new Intent(wtd, Signup.class);
                i.putExtra("type", CollectionType.USER.toString());
                wtd.startActivity(i);
            }
        });
        
        Button loginButton = (Button) findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new ConnectAndRetrieveList().execute();
            }
        });
        
        TextView linkToDM = (TextView) findViewById(R.id.linkToDM);
        linkToDM.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(DUCKSMANAGER_URL));
                WhatTheDuck.this.startActivity(intent);
            }
        });
    }

    public void info(Context c, int titleId) {
        Toast.makeText(c, titleId, Toast.LENGTH_SHORT).show();
    }
    
    public void alert(String message) {
        alert(this, message);
    }
    
    public void alert(Context c, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(getString(R.string.error));
        builder.setMessage(message);
        builder.create().show();
    }
    
    public void alert(Context c, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.error);
        builder.setMessage(getString(messageId));
        builder.create().show();
    }

    public void alert(Context c, int titleId, int messageId, String extraMessage) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(getString(titleId));
        builder.setMessage(getString(messageId)+extraMessage);

        this.runOnUiThread(new Runnable() {
            public void run() {
                builder.create().show();
            }
        });
    }
    
    public void alert(int titleId, int messageId, String extraMessage) {
        alert(this, titleId, messageId, extraMessage);
    }
    
    public void alert(int titleId, int messageId) {
        alert(this, titleId, messageId, "");
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
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadConfig(AssetManager assets) {
        InputStream reader = null;
        try {
            reader = assets.open(CONFIG);
            config = new Properties();
            config.load(reader);
        } catch (IOException e) {
            WhatTheDuck.wtd.alert(WhatTheDuck.wtd, R.string.internal_error);
            System.err.println("Config file not found, aborting");
            System.exit(-1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    WhatTheDuck.wtd.alert(WhatTheDuck.wtd, R.string.internal_error);
                    System.err.println("Error while reading config file, aborting");
                    System.exit(-1);
                }
            }
        }
    }

    public static void saveSettings(boolean withCredentials) {
        Properties props=new Properties();

        InputStream inputStream;
        try {
            inputStream = wtd.openFileInput(USER_SETTINGS);
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (withCredentials) {
            props.put("username", getUsername());
            props.put("password", getEncryptedPassword());
        }
        else {
            props.remove("username");
            props.remove("password");
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

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        WhatTheDuck.password = password;
        if (password == null) {
            setEncryptedPassword(null);
        }
        else {
            setEncryptedPassword(wtd.toSHA1(password));
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
        if (!isOnline()) {
            throw new Exception(""+R.string.network_error);
        }

        if (getEncryptedPassword() == null) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(getPassword().getBytes());
            setEncryptedPassword(byteArray2Hex(md.digest()));
        }

        Ion.with(this.findViewById(android.R.id.content).getContext())
            .load(config.getProperty(CONFIG_KEY_API_ENDPOINT_URL) + urlSuffix)
            .setHeader("x-dm-version", WhatTheDuck.wtd.getApplicationVersion())
            .setMultipartFile(fileName, file)
            .asString()
            .setCallback(futureCallback);
    }

    public String retrieveOrFail(String urlSuffix) throws Exception {
        if (!isOnline()) {
            throw new Exception(""+R.string.network_error);
        }

        if (getEncryptedPassword() == null) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(getPassword().getBytes());
            setEncryptedPassword(byteArray2Hex(md.digest()));
        }
        String response = getPage(getServerURL()+"/"+SERVER_PAGE
                                      + "?pseudo_user="+URLEncoder.encode(username, "UTF-8")
                                      + "&mdp_user="+encryptedPassword
                                      + "&mdp="+ config.getProperty(CONFIG_KEY_SECURITY_PASSWORD)
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

    public void toggleProgressbarLoading(Activity activity, int progressBarId, boolean toggle) {
        ProgressBar progressBar = (ProgressBar) activity.findViewById(progressBarId);

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
        toggleProgressbarLoading(WhatTheDuck.wtd, progressBarId, toggle);
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public String getApplicationVersion() throws NameNotFoundException {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
        return info.versionName;
    }

    private String getPage(String url) {
        String response="";
        try {
            URL userCollectionURL = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(userCollectionURL.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null)
            response+=inputLine;
            in.close();
        } catch (MalformedURLException e) {
            this.alert(R.string.error,
                          R.string.error__malformed_url);
        } catch (IOException e) {
            this.alert(R.string.network_error,
                       R.string.network_error__no_connection);
        }
        return response;
    }
    private String getServerURL() {
        if (serverURL == null) {
            serverURL = getPage(DUCKSMANAGER_URL+"/"+DUCKSMANAGER_PAGE_WITH_REMOTE_URL);
        }
        return serverURL;
    }

    public String toSHA1(String text) {
        try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                md.update(text.getBytes());
                return byteArray2Hex(md.digest());
        }
        catch (NoSuchAlgorithmException e) {
                this.alert(R.string.internal_error,
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

    @Override
    public void onBackPressed() {
    }
}
