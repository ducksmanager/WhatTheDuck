package net.ducksmanager.whattheduck;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

public class AddIssue extends AsyncTask<Object,Integer,Object> {
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private static int progressBarId;

    private static IssueList issueList;
    private static String shortCountryAndPublication;
    private static Issue selectedIssue;
	
    public AddIssue(IssueList il, int progressBarId, String shortCountryAndPublication, Issue selectedIssue) {
    	AddIssue.issueList = il;
    	AddIssue.progressBarId = progressBarId;
    	AddIssue.shortCountryAndPublication = shortCountryAndPublication;
    	AddIssue.selectedIssue = selectedIssue;
    }

	@Override
	protected Object doInBackground(Object... arg0) {
		mHandler.post(new Runnable() {
	        public void run() {
        		String results;
				try {
					results = WhatTheDuck.wtd.retrieveOrFail(AddIssue.progressBarId,
																	"&ajouter_numero"
																   +"&pays_magazine="+shortCountryAndPublication
																   +"&numero="+URLEncoder.encode(selectedIssue.getIssueNumber(), "UTF-8")
																   +"&etat="+URLEncoder.encode(selectedIssue.getIssueConditionStr(), "UTF-8"));
	        		if (results.equals("OK")) {
	        			WhatTheDuck.wtd.info(AddIssue.issueList, R.string.confirmation_message__issue_inserted);
	        			WhatTheDuck.userCollection.addIssue(shortCountryAndPublication, selectedIssue);
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
