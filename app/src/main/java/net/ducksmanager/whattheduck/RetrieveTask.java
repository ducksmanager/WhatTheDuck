package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.koushikdutta.async.future.FutureCallback;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

public class RetrieveTask extends AsyncTask<Object, Object, String> {

    private boolean legacyServer = true;
    private final String urlSuffix;
    protected WeakReference<Activity> originActivityRef;

    private Exception thrownException;
    private String fileName;
    private File file;
    private FutureCallback futureCallback;

    public interface DownloadHandler {
        String getPage(String url);
    }

    private class DefaultDownloadHandler implements DownloadHandler{
        @Override
        public String getPage(String url) {
            StringBuilder response= new StringBuilder();
            try {
                URL userCollectionURL = new URL(url);
                BufferedReader in = new BufferedReader(new InputStreamReader(userCollectionURL.openStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);
                in.close();
            } catch (MalformedURLException e) {
                WhatTheDuck.wtd.alert(R.string.error, R.string.error__malformed_url);
            } catch (IOException e) {
                WhatTheDuck.wtd.alert(R.string.network_error, R.string.network_error__no_connection);
            }
            return response.toString();
        }
    }

    protected RetrieveTask(String urlSuffix, WeakReference<Activity> originActivityRef) {
        this.urlSuffix = urlSuffix;
        this.originActivityRef = originActivityRef;
    }

    protected RetrieveTask(String urlSuffix, boolean legacyServer, FutureCallback futureCallback, String fileName, File file) {
        this.urlSuffix = urlSuffix;
        this.legacyServer = legacyServer;
        this.futureCallback = futureCallback;
        this.fileName = fileName;
        this.file = file;
    }

    @Override
    protected String doInBackground(Object[] objects) {
        try {
            if (legacyServer) {
                return WhatTheDuck.wtd.retrieveOrFail(new DefaultDownloadHandler(), this.urlSuffix);
            }
            else {
                WhatTheDuck.wtd.retrieveOrFailDmServer(this.urlSuffix, this.futureCallback, this.fileName, this.file);
            }

        } catch (Exception e) {
            this.thrownException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (this.thrownException != null) {
            handleResultException(this.thrownException);
        }
    }

    protected boolean hasFailed() {
        return this.thrownException != null;
    }

    public static void handleResultExceptionOnActivity(Exception e, WeakReference<Activity> activityRef) {
        WhatTheDuck.wtd.initUI();
        WhatTheDuck.wtd.toggleProgressbarLoading(activityRef, false);

        if (e instanceof SecurityException) {
            WhatTheDuck.wtd.alert(
                R.string.input_error,
                R.string.input_error__invalid_credentials, "");
            WhatTheDuck.setUsername("");
            WhatTheDuck.setPassword("");
        }
        else if (e instanceof PackageManager.NameNotFoundException) {
            e.printStackTrace();
        }
        else if (e instanceof JSONException) {
            WhatTheDuck.wtd.alert(activityRef,
                R.string.internal_error,
                R.string.internal_error__malformed_list," : " + e.getMessage());
        }
        else {
            if (e.getMessage() != null
                && e.getMessage().equals(R.string.network_error+"")) {
                WhatTheDuck.wtd.alert(
                    R.string.network_error,
                    R.string.network_error__no_connection);
            }
            else {
                WhatTheDuck.wtd.alert(activityRef, e.getMessage());
            }
        }
    }

    private void handleResultException(Exception e) {
        handleResultExceptionOnActivity(e, originActivityRef);
    }
}
