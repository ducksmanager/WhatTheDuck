package net.ducksmanager.whattheduck;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.widget.*;
import net.ducksmanager.security.Security;
import net.ducksmanager.whattheduck.Collection.CollectionType;

import org.apache.http.auth.AuthenticationException;

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
import android.text.TextWatcher;
import android.view.View;

public class WhatTheDuck extends Activity {
    private static final String SERVER_PAGE="WhatTheDuck.php";
    private static final String DUCKSMANAGER_URL="http://www.ducksmanager.net";
    private static final String DUCKSMANAGER_PAGE_WITH_REMOTE_URL="WhatTheDuck_server.php";
    
	public static final String CREDENTIALS_FILENAME = "ducksmanager_credentials";

	private static String serverURL;
	
    private static String username = null;
    private static String password = null;
    private static String encryptedPassword = null;

	public static WhatTheDuck wtd;
	
	public static Collection userCollection = new Collection();
	public static Collection coaCollection = new Collection();
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	wtd=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.whattheduck);
        
        try {
			FileInputStream fis = openFileInput(CREDENTIALS_FILENAME);
			int ch;
			StringBuffer response=new StringBuffer();
			while( (ch = fis.read()) != -1)
				response.append((char)ch);
			fis.close();
			String username=response.toString().split("\n")[0];
			String encryptedPassword=response.toString().split("\n")[1];
			setUsername(username);
			setEncryptedPassword(encryptedPassword);
			EditText usernameEditText = ((EditText) findViewById(R.id.username));
			usernameEditText.setText(username);
			usernameEditText.addTextChangedListener(new TextWatcher() {
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
				public void afterTextChanged(Editable s) { 
					((EditText) findViewById(R.id.password)).setText(""); 
				}
			});
			((EditText) findViewById(R.id.password)).setText("******");
		} catch (FileNotFoundException e1) {
		} catch (IOException e) {
			WhatTheDuck.this.alert(R.string.internal_error, 
		   			   			   R.string.internal_error__credentials_reading_failed);
		} catch (ArrayIndexOutOfBoundsException e) {
			WhatTheDuck.this.alert(R.string.internal_error, 
		   			   			   R.string.internal_error__credentials_reading_failed);
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
        		new ConnectAndRetrieveList(R.id.progressBarConnection).execute();
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
    
    public void alert(Context c, int titleId, String extraTitle, int messageId, String extraMessage) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(c);
    	builder.setTitle(getString(titleId)+extraTitle);
    	builder.setMessage(getString(messageId)+extraMessage);
    	builder.create().show();
    }
    
    public void alert(int titleId, String extraTitle, int messageId, String extraMessage) {
    	alert(this, titleId, extraTitle, messageId, extraMessage);
    }
    
    public void alert(int titleId, int messageId) {
    	alert(this, titleId, "", messageId, "");
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
		setEncryptedPassword(wtd.toSHA1(password));
	}

	public static String getEncryptedPassword() {
		return encryptedPassword;
	}

	private static void setEncryptedPassword(String encryptedPassword) {
		WhatTheDuck.encryptedPassword = encryptedPassword;
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
                                      + "&mdp="+Security.SECURITY_PASSWORD
                                      + "&version="+getApplicationVersion()
                                      + urlSuffix);

        response = response.replaceAll("/\\/", "");
        if (response.equals("0")) {
            throw new AuthenticationException();
        }
        else
            return response;
	}

    public void toggleProgressbarLoading(int progressBarId, boolean toggle) {
        ProgressBar progressBar = (ProgressBar) wtd.findViewById(progressBarId);

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
	
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }

	    return false;
	}	
	
	private String getApplicationVersion() throws NameNotFoundException {
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
	public String getServerURL() {
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
	
}
