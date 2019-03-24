package net.ducksmanager.retrievetasks;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.koushikdutta.async.future.FutureCallback;

import net.ducksmanager.util.CoverFlowActivity;
import net.ducksmanager.persistence.models.composite.IssueWithFullUrl;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.RetrieveTask;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import static net.ducksmanager.whattheduck.WhatTheDuck.trackEvent;

public class CoverSearch extends RetrieveTask {

    public static WeakReference<Activity> originActivityRef;
    public static final String uploadTempDir = "Pictures";
    public static final String uploadFileName = "wtd_jpg";

    private static final FutureCallback<String> futureCallback = new FutureCallback<String>() {
        @Override
        public void onCompleted(Exception e, String result) {
        try {
            Activity originActivity = originActivityRef.get();

            originActivity.findViewById(R.id.addToCollectionWrapper).setVisibility(View.VISIBLE);
            originActivity.findViewById(R.id.progressBar).setVisibility(View.GONE);
            if (e != null)
                throw e;

            System.out.println("Success");
            JSONObject object;
            try {
                object = new JSONObject(result);
                if (object.has("issues")) {
                    JSONObject issues = object.getJSONObject("issues");
                    ArrayList<IssueWithFullUrl> resultCollection = new ArrayList<>();
                    Iterator<String> issueIterator = issues.keys();
                    while (issueIterator.hasNext()) {
                        String issueCode = issueIterator.next();
                        JSONObject issue = (JSONObject) issues.get(issueCode);
                        resultCollection.add(new IssueWithFullUrl(
                            (String) issue.get("countrycode"),
                            (String) issue.get("publicationcode"),
                            (String) issue.get("publicationtitle"),
                            (String) issue.get("issuenumber"),
                            WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL) + "/cover-id/download/" + issue.get("coverid"))
                        );
                    }
                    Intent i = new Intent(originActivity, CoverFlowActivity.class);
                    i.putExtra("resultCollection", resultCollection);
                    originActivity.startActivity(i);
                } else {
                    if (object.has("type")) {
                        switch((String) object.get("type")) {
                            case "SEARCH_RESULTS":
                                WhatTheDuck.wtd.alert(originActivityRef, R.string.add_cover_no_results);
                            break;
                            default:
                                WhatTheDuck.wtd.alert(originActivityRef, (String) object.get("type"));
                        }
                    }
                }
            } catch (JSONException jsone) {
                if (result.contains("exceeds your upload")) {
                    WhatTheDuck.wtd.alert(originActivityRef, R.string.add_cover_error_file_too_big);
                }
                else {
                    WhatTheDuck.wtd.alert(originActivityRef, R.string.internal_error);
                    jsone.printStackTrace();
                }
            }
        } catch (Exception ex) {
            WhatTheDuck.wtd.alert(originActivityRef, ex.getMessage());
        }
        }
    };

    public CoverSearch(WeakReference<Activity> originActivityRef, File coverPicture) {
        super("/cover-id/search", false, futureCallback, uploadFileName, coverPicture);
        CoverSearch.originActivityRef = originActivityRef;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting cover search : " + System.currentTimeMillis());
        trackEvent("coversearch/start");
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        trackEvent("coversearch/finish");
        System.out.println("Ending cover search : " + System.currentTimeMillis());
    }
}