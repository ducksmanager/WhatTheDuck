package net.ducksmanager.whattheduck;

import android.content.pm.PackageManager;
import android.os.AsyncTask;

public class RetrieveTask extends AsyncTask<Object, Object, String> {

    protected String urlSuffix;
    protected static Integer progressBarId;

    private Exception thrownException;

    public RetrieveTask(String urlSuffix, Integer progressBarId) {
        this.urlSuffix = urlSuffix;
        RetrieveTask.progressBarId = progressBarId;
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
