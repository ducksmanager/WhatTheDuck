package net.ducksmanager.whattheduck;

import android.content.Intent;

import net.ducksmanager.util.CoverFlowActivity;

import org.json.JSONException;
import org.json.JSONObject;

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

        JSONObject object;
        try {
            object = new JSONObject(response);
            Collection resultCollection = new Collection();
            Iterator<String> issueIterator = object.keys();
            while (issueIterator.hasNext()) {
                String issueNumber = issueIterator.next();
                resultCollection.addIssue("fr", "PM", new Issue(issueNumber, Issue.IssueCondition.NO_CONDITION));
            }
            Intent i = new Intent(cls, CoverFlowActivity.class);
            i.putExtra("resultCollection", resultCollection);
            cls.startActivity(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}