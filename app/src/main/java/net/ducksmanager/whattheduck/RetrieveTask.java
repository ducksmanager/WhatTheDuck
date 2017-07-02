package net.ducksmanager.whattheduck;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.VisibleForTesting;

import com.koushikdutta.async.future.FutureCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class RetrieveTask extends AsyncTask<Object, Object, String> {

    private boolean legacyServer = true;
    protected String urlSuffix;
    protected static Integer progressBarId;

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
            String response="";
            try {
                URL userCollectionURL = new URL(url);
                BufferedReader in = new BufferedReader(new InputStreamReader(userCollectionURL.openStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    response+=inputLine;
                in.close();
            } catch (MalformedURLException e) {
                WhatTheDuck.wtd.alert(R.string.error, R.string.error__malformed_url);
            } catch (IOException e) {
                WhatTheDuck.wtd.alert(R.string.network_error, R.string.network_error__no_connection);
            }
            return response;
        }
    }

    @VisibleForTesting
    public static DownloadHandler downloadHandler = null;


    public RetrieveTask(String urlSuffix, Integer progressBarId) {
        this.urlSuffix = urlSuffix;
        RetrieveTask.progressBarId = progressBarId;
    }

    public RetrieveTask(String urlSuffix, Integer progressBarId, boolean legacyServer, FutureCallback futureCallback, String fileName, File file) {
        this.urlSuffix = urlSuffix;
        RetrieveTask.progressBarId = progressBarId;
        this.legacyServer = legacyServer;
        this.fileName = fileName;
        this.file = file;
        this.futureCallback = futureCallback;
    }

    @Override
    protected String doInBackground(Object[] objects) {
        try {
            if (legacyServer) {
                if (downloadHandler == null) {
                    downloadHandler = new DefaultDownloadHandler();
                }
                return WhatTheDuck.wtd.retrieveOrFail(downloadHandler, this.urlSuffix);
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
            if (progressBarId != null) {
                WhatTheDuck.wtd.toggleProgressbarLoading(progressBarId, false);
            }

            if (this.thrownException instanceof SecurityException) {
                WhatTheDuck.wtd.alert(
                        R.string.input_error,
                        R.string.input_error__invalid_credentials, "");
                WhatTheDuck.setUsername("");
                WhatTheDuck.setPassword("");
            }
            else if (this.thrownException instanceof PackageManager.NameNotFoundException) {
                this.thrownException.printStackTrace();
            }
            else {
                if (this.thrownException.getMessage() != null
                 && this.thrownException.getMessage().equals(R.string.network_error+"")) {
                    WhatTheDuck.wtd.alert(
                            R.string.network_error,
                            R.string.network_error__no_connection);
                }
                else {
                    WhatTheDuck.wtd.alert(this.thrownException.getMessage());
                }
            }
        }
    }

    protected boolean hasFailed() {
        return this.thrownException != null;
    }
}
