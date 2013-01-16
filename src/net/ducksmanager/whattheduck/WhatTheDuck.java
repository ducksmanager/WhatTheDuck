package net.ducksmanager.whattheduck;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import net.ducksmanager.security.Security;

import org.apache.http.auth.AuthenticationException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class WhatTheDuck extends Activity {
    private static final String SERVER_PAGE="http://87.106.165.63/WhatTheDuck.php";
    private static final String DUCKSMANAGER_URL="http://www.ducksmanager.net";
	public static final String CREDENTIALS_FILENAME = "ducksmanager_credentials";
	public static final String VERSION = "1.1.3";
	
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
		
        
        Button loginButton = (Button) findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
        		new ConnectAndRetrieveList(R.id.progressBarConnection).execute(new Object[0]);
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
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
	}

	public static String getEncryptedPassword() {
		return encryptedPassword;
	}

	public static void setEncryptedPassword(String encryptedPassword) {
		WhatTheDuck.encryptedPassword = encryptedPassword;
	}


    private static String byteArray2Hex(byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
    
	public String retrieveOrFail(int progressBarId, String urlSuffix)  {
        ProgressBar progressBar = (ProgressBar) findViewById(progressBarId);
        if (progressBar != null)
        	progressBar.setVisibility(ProgressBar.VISIBLE);
		try {
			if (!isOnline()) {
				throw new Exception(""+R.string.network_error);
			}
			
			if (getEncryptedPassword() == null) {
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(getPassword().getBytes());
				setEncryptedPassword(byteArray2Hex(md.digest()));
			}
			
			URL userCollectionURL = new URL(SERVER_PAGE
										  + "?pseudo_user="+URLEncoder.encode(username)
										  + "&mdp_user="+encryptedPassword
										  + "&mdp="+Security.SECURITY_PASSWORD
										  + "&version="+VERSION
										  + urlSuffix);
			BufferedReader in = new BufferedReader(new InputStreamReader(userCollectionURL.openStream()));
			
			String inputLine;
			String response="";
			while ((inputLine = in.readLine()) != null)
				response+=inputLine;
			in.close();
            
			response = response.replaceAll("/\\/", "");
			if (response.equals("0")) {	
				throw new AuthenticationException();
			}
			else
				return response;
		} catch (MalformedURLException e) {
			this.alert(R.string.error,
		   			   R.string.error__malformed_url);
		} catch (IOException e) {
			this.alert(R.string.network_error,
					   R.string.network_error__server_unavailable);
		} catch (NoSuchAlgorithmException e) {
			this.alert(R.string.internal_error,
					   R.string.internal_error__crypting_failed);
		} catch (AuthenticationException e) {
			this.alert(R.string.input_error, 
					   R.string.input_error__invalid_credentials);
			setUsername(null);
			setPassword(null);
		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().equals(R.string.network_error+"")) {
				this.alert(R.string.network_error, 
						   R.string.network_error__no_connection);
			}
			else {
				this.alert(e.getMessage());
			}
		}
		finally {
	        if (progressBar != null) {
				progressBar.clearAnimation();
				progressBar.setVisibility(ProgressBar.GONE);
	        }
		}
		return null;
	}
	
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}
}
