package net.ducksmanager.whattheduck;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.IssueListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.whattheduck.Collection.CollectionType;

import java.util.ArrayList;
import java.util.Locale;

public class IssueList extends List {
    private Issue selectedIssue = null;

    private ArrayList<Issue> issues = null;
    private IssueAdapter issueAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String selectedCountry = WhatTheDuck.getSelectedCountry();
        final String selectedPublication = WhatTheDuck.getSelectedPublication();

		final String countryFullName = CountryListing.getCountryFullName(selectedCountry);
		final String publicationFullName = PublicationListing.getPublicationFullName(selectedCountry, selectedPublication);

		if (type.equals(CollectionType.USER.toString())
		|| (type.equals(CollectionType.COA.toString()) && IssueListing.hasFullList(selectedPublication))) {
	        this.show();
        }
        else {
            new IssueListing(this, selectedCountry, selectedPublication).execute();
        }

		setNavigationCountry(countryFullName, selectedCountry);
		setNavigationPublication(publicationFullName, selectedPublication);
    }

	public void show() {
        issues = getCollection().getIssueList(
			WhatTheDuck.getSelectedCountry(),
			WhatTheDuck.getSelectedPublication(),
            this.type);

        if (issues.size() == 0) {
            TextView emptyListText = (TextView) this.findViewById(R.id.emptyList);
            emptyListText.setVisibility(TextView.VISIBLE);
        }

        this.issueAdapter = new IssueAdapter(this, issues);
        setListAdapter(this.issueAdapter);

		EditText filterEditText = (EditText) this.findViewById(R.id.filter);
    	if (issues.size() > 20) {
    		filterEditText.setVisibility(EditText.VISIBLE);
    		filterEditText.requestFocus();
    		
    		filterEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                	String typedText = s.toString();
                	ArrayList<Issue> filteredIssues = new ArrayList<>();
                	for (Issue issue : IssueList.this.issues)
                		if (issue.getIssueNumber().replace("* ", "").toLowerCase(Locale.FRANCE).contains(typedText.toLowerCase()))
                			filteredIssues.add(issue);

                    IssueList.this.issueAdapter = new IssueAdapter(IssueList.this, filteredIssues);
                    setListAdapter(IssueList.this.issueAdapter);
                }
            });
    	}
		else {
			filterEditText.setVisibility(EditText.GONE);
		}
    }
    
    @Override
    public void onBackPressed() {
    	Intent i = new Intent(WhatTheDuck.wtd, PublicationList.class);
        i.putExtra("type", type);
        startActivity(i);
	}

	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (type.equals(CollectionType.COA.toString())) {
        	selectedIssue = (Issue) this.getListView().getItemAtPosition(((Long)id).intValue());
        	if (selectedIssue.getIssueNumber().startsWith("* "))
        		WhatTheDuck.wtd.info(this, R.string.input_error__issue_already_possessed);
        	else {
        		final CharSequence[] items = {getString(R.string.condition_bad), getString(R.string.condition_notsogood), getString(R.string.condition_good)};

        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        		builder.setTitle(getString(R.string.insert_issue__confirm,selectedIssue.getIssueNumber()))
        			   .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
        				   public void onClick(DialogInterface dialog, int item) {}
        		    	})
		        	   .setCancelable(true)
        		       .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
        		           public void onClick(DialogInterface dialog, int id) {
        		        	   dialog.dismiss();                
        		        	   int selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
        		        	   if (selectedPosition == -1) {
        		        			WhatTheDuck.wtd.info(IssueList.this, R.string.input_error__select_condition);
        		                	return;
        		        	   }
        		        	   String condition = items[selectedPosition].toString();
        		        	   String DMcondition;
        		        	   if (condition.equals(getString(R.string.condition_bad)))
        		        		   DMcondition = Issue.BAD_CONDITION;
        		        	   else if (condition.equals(getString(R.string.condition_notsogood)))
        		        		   DMcondition = Issue.NOTSOGOOD_CONDITION;
        		        	   else
        		        		   DMcondition = Issue.GOOD_CONDITION;
        		        	   selectedIssue.setIssueCondition(Issue.issueConditionStrToIssueCondition(DMcondition));
        		        	   new AddIssue(IssueList.this, WhatTheDuck.getSelectedPublication(), selectedIssue).execute();
        		           }
        		       })
        		       .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
        		           public void onClick(DialogInterface dialog, int id) {
        		                dialog.cancel();
        		           }
        		       });
        		builder.create().show();
        	}
        }
    	super.onListItemClick(l, v, position, id);
    }
}
