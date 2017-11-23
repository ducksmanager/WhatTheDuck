package net.ducksmanager.retrievetasks;

import android.app.Activity;

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
import java.util.ArrayList;

public abstract class GetPurchaseList extends RetrieveTask {
    protected GetPurchaseList() {
        super("&get_achats=true", R.id.progressBarLoading);
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
        if (super.hasFailed()) {
            return;
        }

        try {
            if (response == null) {
                return;
            }

            JSONObject object = new JSONObject(response);
            if (object.has("achats")) {
                ArrayList<PurchaseAdapter.Purchase> purchases = new ArrayList<>();

                JSONArray purchaseObjects = object.getJSONArray("achats");
                for (int i = 0; i < purchaseObjects.length(); i++) {
                    JSONObject purchaseObject = (JSONObject) purchaseObjects.get(i);
                    try {
                        purchases.add(new PurchaseAdapter.Purchase(
                            Integer.parseInt((String) purchaseObject.get("ID_Acquisition")),
                            PurchaseAdapter.dateFormat.parse((String)purchaseObject.get("Date")),
                            (String) purchaseObject.get("Description")
                        ));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                WhatTheDuck.userCollection.setPurchaseList(purchases);
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
