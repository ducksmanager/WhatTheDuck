package net.ducksmanager.whattheduck;

import android.content.Intent;

import net.ducksmanager.util.CoverFlowActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class CoverSearch extends RetrieveTask {

    private List cls;

    public CoverSearch(List cls, Integer imageResource) {
        super("/cover-id/search", R.id.progressBarConnection, false, buildFileList(imageResource));
        this.cls = cls;
    }

    private static HashMap<String, Integer> buildFileList(Integer imageResource) {
        HashMap<String, Integer> files = new HashMap<>();
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
        System.out.println("Ending cover search   : " + System.currentTimeMillis());
        if (super.hasFailed()) {
            return;
        }

        System.out.println("Success");
        JSONObject object;
        try {
            object = new JSONObject(response);
            ArrayList<IssueWithFullUrl> resultCollection = new ArrayList<>();
            Iterator<String> issueIterator = object.keys();
            while (issueIterator.hasNext()) {
                String issueNumber = issueIterator.next();
                JSONObject issue = (JSONObject) object.get(issueNumber);
                resultCollection.add(new IssueWithFullUrl(
                        (String) issue.get("countrycode"),
                        (String) issue.get("publicationtitle"),
                        new Issue(issueNumber, Issue.IssueCondition.NO_CONDITION),
                        WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_API_ENDPOINT_URL) + "/cover-id/download/" + issue.get("fullurl"))
                );
            }
            Intent i = new Intent(cls, CoverFlowActivity.class);
            i.putExtra("resultCollection", resultCollection);
            cls.startActivity(i);
        } catch (JSONException e) {
            WhatTheDuck.wtd.alert(super.thrownException.getMessage());
        }

    }
}