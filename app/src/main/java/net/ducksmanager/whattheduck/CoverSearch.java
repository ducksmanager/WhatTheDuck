package net.ducksmanager.whattheduck;

import android.content.Intent;

import com.koushikdutta.async.future.FutureCallback;

import net.ducksmanager.util.CoverFlowActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CoverSearch extends RetrieveTask {

    public static List cls;

    private static FutureCallback<String> futureCallback = new FutureCallback<String>() {
        @Override
        public void onCompleted(Exception e, String result) {
            try {
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
                                WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_API_ENDPOINT_URL) + "/cover-id/download/" + issue.get("coverid"))
                            );
                        }
                        Intent i = new Intent(CoverSearch.cls, CoverFlowActivity.class);
                        i.putExtra("resultCollection", resultCollection);
                        CoverSearch.cls.startActivity(i);
                    } else {
                        if (object.has("type")) {
                            switch((String) object.get("type")) {
                                case "SEARCH_RESULTS":
                                    WhatTheDuck.wtd.alert(cls, "No result found for covers matching your photo, please try again.");
                                break;
                                default:
                                    WhatTheDuck.wtd.alert(cls, (String) object.get("type"));
                            }
                        }
                    }
                } catch (JSONException jsone) {
                    jsone.printStackTrace();
                }
            } catch (Exception ex) {
                WhatTheDuck.wtd.alert(cls, ex.getMessage());
            }
        }
    };

    public CoverSearch(List cls, File coverPicture) {
        super("/cover-id/search", R.id.progressBarConnection, false, buildFileList(coverPicture), futureCallback);
        CoverSearch.cls = cls;
    }

    private static HashMap<String, File> buildFileList(File imageResource) {
        HashMap<String, File> files = new HashMap<>();
        files.put("wtd_jpg", imageResource);
        return files;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting cover search : " + System.currentTimeMillis());
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        System.out.println("Ending cover search : " + System.currentTimeMillis());
    }
}