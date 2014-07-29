package net.ducksmanager.whattheduck;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import net.ducksmanager.inducks.coa.CoaListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

public class ConnectAndRetrieveList extends RetrieveTask {
    private CheckBox mCheckboxRememberCredentials;

    private static int progressBarId;
    private final WhatTheDuck wtd;

    public ConnectAndRetrieveList(int progressBarId) {
        super("");
        wtd = WhatTheDuck.wtd;
        ConnectAndRetrieveList.progressBarId = progressBarId;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.userCollection = new Collection();
        if ( WhatTheDuck.getUsername() == null
         || !WhatTheDuck.getUsername().equals(((EditText) wtd.findViewById(R.id.username)).getText().toString())
         || WhatTheDuck.getEncryptedPassword() == null) {

            WhatTheDuck.setUsername(((EditText) wtd.findViewById(R.id.username)).getText().toString());
            WhatTheDuck.setPassword(((EditText) wtd.findViewById(R.id.password)).getText().toString());
            if (WhatTheDuck.getUsername().equals("") || (WhatTheDuck.getPassword().equals(""))) {
                wtd.alert(R.string.input_error,
                        R.string.input_error__empty_credentials);
                ProgressBar mProgressBar = (ProgressBar) wtd.findViewById(R.id.progressBarConnection);
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                cancel(true);
                return;
            }
        }

        wtd.toggleProgressbarLoading(progressBarId, true);
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (super.hasFailed()) {
            return;
        }

        try {
            if (response == null) {
                return;
            }

            mCheckboxRememberCredentials = (CheckBox) wtd.findViewById(R.id.checkBoxRememberCredentials);
            boolean rememberCredentials = mCheckboxRememberCredentials.isChecked();

            if (rememberCredentials) {
                try {
                    String credentials = WhatTheDuck.getUsername() + "\n" + WhatTheDuck.getEncryptedPassword();
                    FileOutputStream fos = wtd.openFileOutput(WhatTheDuck.CREDENTIALS_FILENAME, Context.MODE_PRIVATE);
                    fos.write(credentials.getBytes());
                    fos.close();
                } catch (IOException e) {
                    wtd.alert(R.string.internal_error,
                              R.string.internal_error__credentials_storage_failed);
                    wtd.toggleProgressbarLoading(progressBarId, false);
                }
            }

            JSONObject object = new JSONObject(response);
            try {
                JSONObject issues = object.getJSONObject("numeros");
                @SuppressWarnings("unchecked")
                Iterator<String> issueIterator = issues.keys();
                while (issueIterator.hasNext()) {
                    String countryAndPublication = issueIterator.next();
                    JSONArray publicationIssues = issues.getJSONArray(countryAndPublication);
                    for (int i = 0; i < publicationIssues.length(); i++) {
                        String issueNumber = publicationIssues.getJSONObject(i).getString("Numero");
                        String issueCondition = publicationIssues.getJSONObject(i).getString("Etat");
                        WhatTheDuck.userCollection.addIssue(countryAndPublication, new Issue(issueNumber, issueCondition));
                    }
                }

                JSONObject countryNames = object.getJSONObject("static").getJSONObject("pays");
                @SuppressWarnings("unchecked")
                Iterator<String> countryNameIterator = countryNames.keys();
                while (countryNameIterator.hasNext()) {
                    String countryShortName = countryNameIterator.next();
                    String countryFullName = countryNames.getString(countryShortName);
                    CoaListing.addCountry(countryShortName, countryFullName);
                }

                JSONObject publicationNames = object.getJSONObject("static").getJSONObject("magazines");
                @SuppressWarnings("unchecked")
                Iterator<String> publicationNameIterator = publicationNames.keys();
                while (publicationNameIterator.hasNext()) {
                    String shortName = publicationNameIterator.next();
                    String publicationFullName = publicationNames.getString(shortName);
                    CoaListing.addPublication(shortName, publicationFullName);
                }
            } catch (JSONException e) {
                JSONArray issues = object.getJSONArray("numeros");
                if (issues.length() > 0)
                    throw e;
            } finally {
                wtd.toggleProgressbarLoading(progressBarId, false);
            }
        } catch (JSONException e) {
            wtd.alert(R.string.internal_error, "",
                    R.string.internal_error__malformed_list, " : " + e.getMessage());
        }

        Intent i = new Intent(wtd, CountryList.class);
        i.putExtra("type", CollectionType.USER.toString());
        wtd.startActivity(i);
    }
}
