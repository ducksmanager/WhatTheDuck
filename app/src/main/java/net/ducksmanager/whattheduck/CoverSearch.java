package net.ducksmanager.whattheduck;

import android.content.Intent;

import net.ducksmanager.util.CoverFlowActivity;

import org.json.JSONException;
import org.json.JSONObject;

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