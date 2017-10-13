package net.ducksmanager.whattheduck;


import android.content.Intent;
import android.text.TextUtils;
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

    private final WhatTheDuck wtd;
    private final Boolean fromUI;

    public ConnectAndRetrieveList(Boolean fromUI) {
        super("", R.id.progressBarConnection);
        wtd = WhatTheDuck.wtd;
        this.fromUI = fromUI;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.userCollection = new Collection();

        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("retrievecollection/start");

        if (this.fromUI) {
            if (WhatTheDuck.getUsername() == null
                || !WhatTheDuck.getUsername().equals(((EditText) wtd.findViewById(R.id.username)).getText().toString())
                || WhatTheDuck.getEncryptedPassword() == null) {

                WhatTheDuck.setUsername(((EditText) wtd.findViewById(R.id.username)).getText().toString());
                WhatTheDuck.setPassword(((EditText) wtd.findViewById(R.id.password)).getText().toString());
                WhatTheDuck.setRememberCredentials(((CheckBox) wtd.findViewById(R.id.checkBoxRememberCredentials)).isChecked());
            }
        }

        if (TextUtils.isEmpty(WhatTheDuck.getUsername()) || (TextUtils.isEmpty(WhatTheDuck.getPassword()) && TextUtils.isEmpty(WhatTheDuck.getEncryptedPassword()))) {
            wtd.alert(R.string.input_error,
                R.string.input_error__empty_credentials);
            ProgressBar mProgressBar = wtd.findViewById(R.id.progressBarConnection);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            cancel(true);
            return;
        }

        wtd.toggleProgressbarLoading(progressBarId, true);
    }

    @Override
    protected void onPostExecute(String response) {
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("retrievecollection/finish");
        super.onPostExecute(response);
        if (super.hasFailed()) {
            return;
        }

        try {
            if (response == null) {
                return;
            }

            WhatTheDuck.saveSettings(WhatTheDuck.getRememberCredentials());

            JSONObject object = new JSONObject(response);
            try {
                if (object.has("numeros")) {
                    if (object.get("numeros").getClass().equals(JSONObject.class)) {
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
                    }
                    else { // Empty list
                        CountryListing.hasFullList = false;
                    }

                    Intent i = new Intent(wtd, CountryList.class);
                    i.putExtra("type", Collection.CollectionType.USER.toString());
                    wtd.startActivity(i);
                } else {
                    throw new JSONException("");
                }
            }
            catch (JSONException e) {
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
