package net.ducksmanager.whattheduck;

import java.util.ArrayList;
import java.util.Locale;

import net.ducksmanager.inducks.coa.CoaListing;
import net.ducksmanager.inducks.coa.CoaListing.ListType;
import net.ducksmanager.whattheduck.Collection.CollectionType;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class IssueList extends List {
    protected static final int ACTIVITY_LISTENUMEROS=2;
    ArrayList<Issue> selectedIssues = new ArrayList<Issue>();

    private LayoutInflater inflater;
    private ArrayList<Issue> issues = null;
    private IssueAdapter issueAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (type.equals(CollectionType.USER.toString())) {
	        setTitle(getString(R.string.my_collection)
	        		+">"+CoaListing.getCountryFullName(getCollection().getSelectedCountry())
	        		+">"+CoaListing.getPublicationFullName(getCollection().getSelectedCountry(), 
	        											   getCollection().getSelectedPublication()));
	        show();
        }
        else {
        	setTitle(getString(R.string.insert_issue_menu)+">"
        			+CoaListing.getCountryFullName(getCollection().getSelectedCountry())+">"
        			+CoaListing.getPublicationFullName(getCollection().getSelectedCountry(), 
							   						   getCollection().getSelectedPublication()));
    		new CoaListing(this, ListType.ISSUE_LIST, R.id.progressBarLoading, getCollection().getSelectedCountry(), getCollection().getSelectedPublication()).execute(new Object[0]);

    		Button addIssuesButton = (Button) this.findViewById(R.id.addIssues);
    		addIssuesButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                	IssueList.this.addSelectedIssues();
                }
            });
    		addIssuesButton.setVisibility(Button.VISIBLE);
        }
        
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.issueAdapter = new IssueAdapter(this, R.layout.row, this.issues, type);
        setListAdapter(this.issueAdapter);

    }
    
    public void show() {
        show(getCollection().getIssueList(getCollection().getSelectedCountry(), 
		   								  getCollection().getSelectedPublication(), 
		   								  this.type),
		   								  Boolean.FALSE);
    }
    
    public void show(ArrayList<Issue> issues, Boolean useless) {
    	this.issues = issues;
        
    	if (issues.size() > 20) {
    		EditText filterEditText = (EditText) this.findViewById(R.id.filter);
    		filterEditText.setVisibility(EditText.VISIBLE);
    		
    		filterEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                	String typedText = s.toString();
                	ArrayList<Issue> filteredIssues = new ArrayList<Issue>();
                	for (Issue issue : IssueList.this.issues)
                		if (issue.getIssueNumber().replace("* ", "").toLowerCase(Locale.FRANCE).contains(typedText.toLowerCase()))
                			filteredIssues.add(issue);

                    IssueList.this.issueAdapter = new IssueAdapter(IssueList.this, R.layout.row, filteredIssues, type);
                    setListAdapter(IssueList.this.issueAdapter);
                }
            });
    	}
    }
    
    @Override
    public void onBackPressed() {
    	Intent i = new Intent(WhatTheDuck.wtd, PublicationList.class);
        i.putExtra("type", type);
        startActivity(i);
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
            	WhatTheDuck.coaCollection.setSelectedCountry(getCollection().getSelectedCountry());
            	WhatTheDuck.coaCollection.setSelectedPublication(getCollection().getSelectedPublication());
                Intent i = new Intent(WhatTheDuck.wtd, IssueList.class);
                i.putExtra("type", CollectionType.COA.toString());
                startActivity(i);
                return super.onMenuItemSelected(featureId, item, true);
        }
        return super.onMenuItemSelected(featureId, item, false);
    }
    
    protected void addSelectedIssues() {
        if (type.equals(CollectionType.COA.toString())) {
        	selectedIssues = new ArrayList<Issue>();
        	for (int i = 0;i<getListView().getChildCount(); i++) {
    			CheckBox checkBox = (CheckBox) getListView().getChildAt(i).findViewById(R.id.checkBoxSelection);
    			if (checkBox.isChecked()) {
                    selectedIssues.add((Issue) getListView().getItemAtPosition(i));
                }
            }
        	
    		final CharSequence[] items = {getString(R.string.condition_bad), getString(R.string.condition_notsogood), getString(R.string.condition_good)};

    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(getString(R.string.insert_issue__confirm, selectedIssues.size()))
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
    		        	   String DMcondition = null;
    		        	   if (condition.equals(getString(R.string.condition_bad)))
    		        		   DMcondition = Issue.BAD_CONDITION;
    		        	   else if (condition.equals(getString(R.string.condition_notsogood)))
    		        		   DMcondition = Issue.NOTSOGOOD_CONDITION;
    		        	   else
    		        		   DMcondition = Issue.GOOD_CONDITION;
    		        	   for (Issue i : selectedIssues) {
    		        		   i.setIssueCondition(Issue.issueConditionStrToIssueCondition(DMcondition));
    		        	   }
    		        	   new AddIssue(IssueList.this, R.id.progressBarInsert, getCollection().getSelectedPublication(), selectedIssues, null).execute(new Object[0]);
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

    private void saveState() {
    }
    
    private class IssueAdapter extends ArrayAdapter<Issue> {

    	private ArrayList<Issue> items;
    	private String typeCollection;

        boolean[] checkBoxState;
        ViewHolder viewHolder;

        // class for caching the views in a row
        private class ViewHolder {
            CheckBox checkBox;
        }

    	public IssueAdapter(Context context, int textViewResourceId,
    			ArrayList<Issue> items, String typeCollection) {
    		super(context, textViewResourceId, items);
    		this.items = items;
    		this.typeCollection = typeCollection;
            this.checkBoxState = new boolean[items.size()];
    	}

    	@Override
    	public View getView(final int position, View convertView, ViewGroup parent) {
    		View v = convertView;
    		if (v == null) {
    			convertView = inflater.inflate(R.layout.row, null);
                viewHolder = new ViewHolder();
                convertView.setTag(viewHolder);
    		}
    		else {
                viewHolder = (ViewHolder) convertView.getTag();
    		}
            viewHolder.checkBox.setChecked(checkBoxState[position]);
            
            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        checkBoxState[position] = true;
                    } else {
                        checkBoxState[position] = false;
                    }
                }
            });
            
//    		v.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View view) {
//        			CheckBox checkBoxSelection = (CheckBox) view.findViewById(R.id.checkBoxSelection);
//        			if (checkBoxSelection.getVisibility() == CheckBox.VISIBLE) {
//        				checkBoxSelection.setChecked(!checkBoxSelection.isChecked());
//        			}
//                }
//    		});
    		Issue i = items.get(position);
    		if (i != null) {
    			TextView issueNumber = (TextView) v.findViewById(R.id.issuenumber);
    			ImageView imageCondition = (ImageView) v.findViewById(R.id.issuecondition);
    			CheckBox checkBoxSelection = (CheckBox) v.findViewById(R.id.checkBoxSelection);
    			
    			Boolean hasIssueCondition = imageCondition != null && i.getIssueCondition() != null;
    			if (issueNumber != null)
    				issueNumber.setText(i.getIssueNumber());
    			if (hasIssueCondition) {
    				int resourceId = Issue.issueConditionToResourceId(i.getIssueCondition());
    				imageCondition.setImageResource(resourceId);
    			}
    			else {
    				imageCondition.setVisibility(CheckBox.INVISIBLE);
    			}
    			if (hasIssueCondition || this.typeCollection.equals(CollectionType.USER.toString())) {
    				checkBoxSelection.setVisibility(CheckBox.GONE);
    			}
    			else {
    				checkBoxSelection.setVisibility(CheckBox.VISIBLE);
    			}
    		}
    		return v;
    	}
    }
}
