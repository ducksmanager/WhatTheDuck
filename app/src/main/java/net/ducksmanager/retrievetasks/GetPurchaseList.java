package net.ducksmanager.retrievetasks;

import android.app.Activity;
import android.content.Intent;

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
import java.util.HashMap;

public abstract class GetPurchaseList extends RetrieveTask {
    protected GetPurchaseList() {
        super("&get_achats=true", R.id.progressBarLoading);
    }

    public static void initAndShowAddIssue(Activity originActivity) {
        new GetPurchaseList() {
            @Override
            protected void afterDataHandling() {
                Intent i = new Intent(originActivity, net.ducksmanager.whattheduck.AddIssue.class);
                originActivity.startActivity(i);
            }

            @Override
            protected WeakReference<Activity> getOriginActivity() {
                return new WeakReference<>(originActivity);
            }
        }.execute();
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(this.getOriginActivity(), progressBarId, true);
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("getpurchases/start");
    }

    @Override
    protected void onPostExecute(String response) {
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("getpurchases/finish");
        super.onPostExecute(response);
        if (!super.hasSucceeded()) {
            return;
        }

        try {
            if (response == null) {
                return;
            }

            JSONObject object = new JSONObject(response);
            if (object.has("achats")) {
                HashMap<Integer, PurchaseAdapter.Purchase> purchases = new HashMap<>();

                JSONArray purchaseObjects = object.getJSONArray("achats");
                for (int i = 0; i < purchaseObjects.length(); i++) {
                    JSONObject purchaseObject = (JSONObject) purchaseObjects.get(i);
                    try {
                        Integer purchaseId = Integer.parseInt((String) purchaseObject.get("ID_Acquisition"));
                        purchases.put(
                            purchaseId,
                            new PurchaseAdapter.PurchaseWithDate(
                                purchaseId,
                                PurchaseAdapter.dateFormat.parse((String)purchaseObject.get("Date")),
                                (String) purchaseObject.get("Description")
                            )
                        );
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                WhatTheDuck.userCollection.setPurchases(purchases);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(this.getOriginActivity(), progressBarId, false);

        afterDataHandling();
    }

    protected abstract void afterDataHandling();

    protected abstract WeakReference<Activity> getOriginActivity();
}
