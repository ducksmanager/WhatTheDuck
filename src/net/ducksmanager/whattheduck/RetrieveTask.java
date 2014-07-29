package net.ducksmanager.whattheduck;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import org.apache.http.auth.AuthenticationException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class RetrieveTask extends AsyncTask<Object, Object, String> {

    protected String urlSuffix;
    private Exception thrownException;

    public RetrieveTask(String urlSuffix, boolean utf8Encoding) {
        if (utf8Encoding) {
            try {
                this.urlSuffix = URLEncoder.encode(urlSuffix, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                WhatTheDuck.wtd.alert(
                        R.string.internal_error,"",
                        R.string.internal_error__issue_insertion_failed,"");
            }
        }
        else {
            this.urlSuffix = urlSuffix;
        }
    }

    public RetrieveTask(String urlSuffix) {
        this(urlSuffix, false);
    }

    @Override
    protected String doInBackground(Object[] objects) {
        try {
            return WhatTheDuck.wtd.retrieveOrFail(this.urlSuffix);

        } catch (Exception e) {
            this.thrownException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (this.thrownException != null) {
            if (this.thrownException instanceof AuthenticationException) {
                WhatTheDuck.wtd.alert(
                        R.string.input_error, "",
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
