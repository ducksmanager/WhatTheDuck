package net.ducksmanager.whattheduck;


import android.content.Intent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ConnectAndRetrieveList extends RetrieveTask {
    private CheckBox mCheckboxRememberCredentials;

    private final WhatTheDuck wtd;

    public ConnectAndRetrieveList() {
        super("", R.id.progressBarConnection);
        wtd = WhatTheDuck.wtd;
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
                WhatTheDuck.saveSettings();
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

                CountryListing.hasFullList = false;

                CountryListing.addCountries(object);
                PublicationListing.addPublications(object);

                Intent i = new Intent(wtd, CountryList.class);
                i.putExtra("type", Collection.CollectionType.USER.toString());
                wtd.startActivity(i);

            } catch (JSONException e) {
                JSONArray issues = object.getJSONArray("numeros");
                if (issues.length() > 0)
                    throw e;
            } finally {
                wtd.toggleProgressbarLoading(progressBarId, false);
            }
        } catch (JSONException e) {
            wtd.alert(R.string.internal_error,
                    R.string.internal_error__malformed_list, " : " + e.getMessage());
        }
    }
}
