package net.ducksmanager.retrievetasks;


import android.content.Intent;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.whattheduck.Collection;
import net.ducksmanager.whattheduck.CountryList;
import net.ducksmanager.whattheduck.Issue;
import net.ducksmanager.whattheduck.List;
import net.ducksmanager.whattheduck.PurchaseAdapter;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;

public class ConnectAndRetrieveList extends RetrieveTask {

    private final WeakReference<WhatTheDuck> wtdActivityRef;
    private final Boolean fromUI;

    public ConnectAndRetrieveList(Boolean fromUI) {
        super("", R.id.progressBarConnection);
        wtdActivityRef = new WeakReference<>(WhatTheDuck.wtd);
        this.fromUI = fromUI;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.userCollection = new Collection();
        WhatTheDuck wtdActivity = wtdActivityRef.get();

        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("retrievecollection/start");

        if (this.fromUI) {
            if (WhatTheDuck.getUsername() == null
                || !WhatTheDuck.getUsername().equals(((EditText) wtdActivity.findViewById(R.id.username)).getText().toString())
                || WhatTheDuck.getEncryptedPassword() == null) {

                WhatTheDuck.setUsername(((EditText) wtdActivity.findViewById(R.id.username)).getText().toString());
                WhatTheDuck.setPassword(((EditText) wtdActivity.findViewById(R.id.password)).getText().toString());
                WhatTheDuck.setRememberCredentials(((CheckBox) wtdActivity.findViewById(R.id.checkBoxRememberCredentials)).isChecked());
            }
        }

        if (TextUtils.isEmpty(WhatTheDuck.getUsername()) || (TextUtils.isEmpty(WhatTheDuck.getPassword()) && TextUtils.isEmpty(WhatTheDuck.getEncryptedPassword()))) {
            WhatTheDuck.wtd.alert(R.string.input_error,
                R.string.input_error__empty_credentials);
            ProgressBar mProgressBar = wtdActivity.findViewById(R.id.progressBarConnection);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            cancel(true);
            return;
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(progressBarId, true);
    }

    @Override
    protected void onPostExecute(String response) {
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("retrievecollection/finish");
        super.onPostExecute(response);
        if (super.hasFailed()) {
            return;
        }

        WhatTheDuck wtdActivity = wtdActivityRef.get();

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
                                JSONObject issueObject = publicationIssues.getJSONObject(i);
                                String issueNumber = issueObject.getString("Numero");
                                String issueCondition = issueObject.getString("Etat");

                                PurchaseAdapter.PurchaseWithDate purchase;
                                if (issueObject.isNull("Acquisition")) {
                                    purchase = null;
                                }
                                else {
                                    JSONObject purchaseObject = issueObject.getJSONObject("Acquisition");
                                    Integer purchaseId = purchaseObject.getInt("ID_Acquisition");
                                    Date purchaseDate = PurchaseAdapter.dateFormat.parse(purchaseObject.getString("Date_Acquisition"));
                                    String purchaseName = purchaseObject.getString("Description_Acquisition");
                                    purchase = new PurchaseAdapter.PurchaseWithDate(purchaseId, purchaseDate, purchaseName);
                                }

                                WhatTheDuck.userCollection.addIssue(
                                    countryAndPublication,
                                    new Issue(issueNumber, issueCondition, purchase)
                                );
                            }
                        }

                        CountryListing.hasFullList = false;

                        CountryListing.addCountries(object);
                        PublicationListing.addPublications(object);
                    }
                    else { // Empty list
                        CountryListing.hasFullList = false;
                    }

                    List.type = Collection.CollectionType.USER.toString();
                    wtdActivity.startActivity(new Intent(wtdActivity, CountryList.class));
                } else {
                    throw new JSONException("");
                }
            }
            catch (JSONException e) {
                JSONArray issues = object.getJSONArray("numeros");
                if (issues.length() > 0)
                    throw e;
            } catch (ParseException e) {
                WhatTheDuck.wtd.alert(R.string.internal_error,
                    R.string.internal_error__malformed_list, " : " + e.getMessage());
            } finally {
                wtdActivity.toggleProgressbarLoading(progressBarId, false);
            }
        } catch (JSONException e) {
            WhatTheDuck.wtd.alert(R.string.internal_error,
                    R.string.internal_error__malformed_list, " : " + e.getMessage());
        }
    }
}
