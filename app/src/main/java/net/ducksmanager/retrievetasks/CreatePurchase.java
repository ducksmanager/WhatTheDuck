package net.ducksmanager.retrievetasks;


import android.app.Activity;

import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;

import static net.ducksmanager.whattheduck.WhatTheDuck.trackEvent;

public abstract class CreatePurchase extends RetrieveTask {

    protected static WeakReference<Activity> originActivityRef;

    protected CreatePurchase(WeakReference<Activity> originActivityRef, String purchaseDateStr, String purchaseName) throws UnsupportedEncodingException {
        super(
            "&ajouter_achat"
                +"&date_achat="+purchaseDateStr
                +"&description_achat="+ URLEncoder.encode(purchaseName, "UTF-8"),
            originActivityRef);
        CreatePurchase.originActivityRef = originActivityRef;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(originActivityRef, true);
        trackEvent("addpurchase/start");
    }

    @Override
    protected void onPostExecute(String response) {
        trackEvent("addpurchase/finish");
        if (!response.equals("OK")) {
            WhatTheDuck.wtd.alert(originActivityRef, R.string.internal_error, R.string.internal_error__purchase_creation_failed, "");
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(originActivityRef, false);
        afterDataHandling();
    }

    protected abstract void afterDataHandling();
}
