package net.ducksmanager.retrievetasks;


import android.app.Activity;

import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import java.lang.ref.WeakReference;

public abstract class CreatePurchase extends RetrieveTask {

    private static WeakReference<Activity> originActivityRef;

    protected CreatePurchase(WeakReference<Activity> originActivityRef, String purchaseDateStr, String purchaseName) {
        super(
            "&ajouter_achat"
                +"&date_achat="+purchaseDateStr
                +"&description_achat="+purchaseName,
            R.id.progressBarLoading
        );
        CreatePurchase.originActivityRef = originActivityRef;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(originActivityRef, progressBarId, true);
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addpurchase/start");
    }

    @Override
    protected void onPostExecute(String response) {
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addpurchase/finish");
        if (!response.equals("OK")) {
            WhatTheDuck.wtd.alert(originActivityRef, R.string.internal_error, R.string.internal_error__purchase_creation_failed, "");
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(originActivityRef, progressBarId, false);
        afterDataHandling();
    }

    protected abstract void afterDataHandling();
}
