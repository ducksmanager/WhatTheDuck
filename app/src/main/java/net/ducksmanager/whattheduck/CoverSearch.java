package net.ducksmanager.whattheduck;

import android.content.Intent;
import android.view.View;

import com.koushikdutta.async.future.FutureCallback;

import net.ducksmanager.util.CoverFlowActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class CoverSearch extends RetrieveTask {

    public static List cls;
    public static final String uploadTempDir = "Pictures";
    public static final String uploadFileName = "wtd_jpg";

    private static final FutureCallback<String> futureCallback = new FutureCallback<String>() {
        @Override
        public void onCompleted(Exception e, String result) {
        try {
            cls.findViewById(R.id.addToCollectionWrapper).setVisibility(View.VISIBLE);
            cls.findViewById(R.id.progressBarLoading).setVisibility(View.GONE);
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
                    Intent i = new Intent(CoverSearch.cls, CoverFlowActivity.class);
                    i.putExtra("resultCollection", resultCollection);
                    CoverSearch.cls.startActivity(i);
                } else {
                    if (object.has("type")) {
                        switch((String) object.get("type")) {
                            case "SEARCH_RESULTS":
                                WhatTheDuck.wtd.alert(cls, R.string.add_cover_no_results);
                            break;
                            default:
                                WhatTheDuck.wtd.alert(cls, (String) object.get("type"));
                        }
                    }
                }
            } catch (JSONException jsone) {
                if (result.contains("exceeds your upload")) {
                    WhatTheDuck.wtd.alert(CoverSearch.cls, R.string.add_cover_error_file_too_big);
                }
                else {
                    WhatTheDuck.wtd.alert(CoverSearch.cls, R.string.internal_error);
                    jsone.printStackTrace();
                }
            }
        } catch (Exception ex) {
            WhatTheDuck.wtd.alert(cls, ex.getMessage());
        }
        }
    };

    public CoverSearch(List cls, File coverPicture) {
        super("/cover-id/search", R.id.progressBarConnection, false, futureCallback, uploadFileName, coverPicture);
        CoverSearch.cls = cls;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting cover search : " + System.currentTimeMillis());
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("coversearch/start");
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        ((WhatTheDuckApplication) WhatTheDuck.wtd.getApplication()).trackEvent("coversearch/finish");
        System.out.println("Ending cover search : " + System.currentTimeMillis());
    }
}