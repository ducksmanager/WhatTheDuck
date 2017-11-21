package net.ducksmanager.whattheduck;


import android.app.Activity;

public abstract class CreatePurchase extends RetrieveTask {

    private static Activity originActivity;

    CreatePurchase(Activity il, String purchaseDateStr, String purchaseName) {
        super(
            "&ajouter_achat"
                +"&date_achat="+purchaseDateStr
                +"&description_achat="+purchaseName,
            R.id.progressBarLoading
        );
        originActivity = il;
    }

    @Override
    protected void onPreExecute() {
        WhatTheDuck.wtd.toggleProgressbarLoading(originActivity, progressBarId, true);
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addpurchase/start");
    }

    @Override
    protected void onPostExecute(String response) {
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("addpurchase/finish");
        if (!response.equals("OK")) {
            WhatTheDuck.wtd.alert(originActivity, R.string.internal_error, R.string.internal_error__purchase_creation_failed, "");
        }

        WhatTheDuck.wtd.toggleProgressbarLoading(originActivity, progressBarId, false);
        afterDataHandling();
    }

    protected abstract void afterDataHandling();
}
