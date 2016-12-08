package net.ducksmanager.whattheduck;

import android.content.Intent;

import net.ducksmanager.util.CoverFlowActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class CoverSearch extends RetrieveTask {

    private List cls;

    public CoverSearch(List cls) {
        super("/cover-id/search", R.id.progressBarConnection, false);
        this.cls = cls;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (super.hasFailed()) {
            return;
        }

        ArrayList<String> issues = new ArrayList<>();


        JSONObject object = null;
        try {
            object = new JSONObject(response);
            Iterator<String> issueIterator = object.keys();
            while (issueIterator.hasNext()) {
                String issueNumber = issueIterator.next();
                issues.add(issueNumber);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(cls, CoverFlowActivity.class);
        i.putExtra("issues", issues);
        cls.startActivity(i);
    }
}