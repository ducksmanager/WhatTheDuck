package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import net.ducksmanager.util.Settings;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

public class RetrieveTask extends AsyncTask<Object, Object, String> {

    private final String urlSuffix;
    protected WeakReference<Activity> originActivityRef;

    private Exception thrownException;

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

    @Override
    protected String doInBackground(Object[] objects) {
        try {
            return WhatTheDuck.wtd.retrieveOrFail(new DefaultDownloadHandler(), this.urlSuffix);

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

    private static void handleResultExceptionOnActivity(Exception e, WeakReference<Activity> activityRef) {
        WhatTheDuck.wtd.showLoginForm();
        WhatTheDuck.wtd.toggleProgressbarLoading(activityRef, false);

        if (e instanceof SecurityException) {
            WhatTheDuck.wtd.alert(
                R.string.input_error,
                R.string.input_error__invalid_credentials, "");
            Settings.setUsername("");
            Settings.setPassword("");
        }
        else if (e instanceof PackageManager.NameNotFoundException) {
            e.printStackTrace();
        }
        else if (e instanceof JSONException) {
            WhatTheDuck.wtd.alert(activityRef,
                R.string.internal_error,
                R.string.internal_error__malformed_list, "");
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
