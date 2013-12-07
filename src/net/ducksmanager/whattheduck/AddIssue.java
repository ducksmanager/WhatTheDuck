package net.ducksmanager.whattheduck;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import net.ducksmanager.whattheduck.Issue.IssueCondition;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

public class AddIssue extends AsyncTask<Object,Integer,Object> {
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private static int progressBarId;

    private static IssueList issueList;
    private static String shortCountryAndPublication;
    private static ArrayList<Issue> selectedIssues;
    private static IssueCondition issueCondition;
	
    public AddIssue(IssueList il, int progressBarId, String shortCountryAndPublication, ArrayList<Issue> selectedIssues, IssueCondition issueCondition) {
    	AddIssue.issueList = il;
    	AddIssue.progressBarId = progressBarId;
    	AddIssue.shortCountryAndPublication = shortCountryAndPublication;
    	AddIssue.selectedIssues = selectedIssues;
    	AddIssue.issueCondition = issueCondition;
    }

	@Override
	protected Object doInBackground(Object... arg0) {
		mHandler.post(new Runnable() {
	        public void run() {
	        	String selectedIssuesStr = "";
				try {
		        	for (Issue issue : selectedIssues) {
		        		selectedIssuesStr+=URLEncoder.encode(issue.getIssueNumber(), "UTF-8")+",";
		        	}
		        	selectedIssuesStr = selectedIssuesStr.substring(0, selectedIssuesStr.length()-1);
		        	
	        		String result;
					result = WhatTheDuck.wtd.retrieveOrFail(AddIssue.progressBarId,
																	"&ajouter_numero"
																   +"&pays_magazine="+shortCountryAndPublication
																   +"&numeros="+selectedIssuesStr
																   +"&etat="+URLEncoder.encode(Issue.issueConditionToIssueConditionStr(issueCondition), "UTF-8"));
	        		if (result.matches("^(OK)+$")) {
	        			WhatTheDuck.wtd.info(AddIssue.issueList, R.string.confirmation_message__issue_inserted);

	    	        	for (Issue issue : selectedIssues) {
	    	        		WhatTheDuck.userCollection.addIssue(shortCountryAndPublication, issue);
	    	        	}
						((IssueList)issueList).show();
	        		}
	        		else
	        			WhatTheDuck.wtd.alert(R.string.internal_error, R.string.internal_error__issue_insertion_failed);
				} catch (UnsupportedEncodingException e) {
					WhatTheDuck.wtd.alert(R.string.internal_error,"",
      		   			  	  			  R.string.internal_error__issue_insertion_failed,"");
				}
	        }
		});
		return null;
	}

}
