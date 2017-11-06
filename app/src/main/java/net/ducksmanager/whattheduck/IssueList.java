package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.ducksmanager.inducks.coa.IssueListing;
import net.ducksmanager.util.SimpleCallback;
import net.ducksmanager.whattheduck.Collection.CollectionType;

public class IssueList extends List<Issue> {

    private String selectedCountry;
    private String selectedPublication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedCountry = WhatTheDuck.getSelectedCountry();
        selectedPublication = WhatTheDuck.getSelectedPublication();

        if (IssueListing.hasFullList(selectedPublication)) {
            this.show();
        }
        else {
            new IssueListing(this, selectedCountry, selectedPublication, new SimpleCallback() {
                @Override
                public void onDownloadFinished(Activity activity) {
                    ((List)activity).show();
                }
            }).execute();
        }

        setNavigationCountry(selectedCountry);
        setNavigationPublication(selectedCountry, selectedPublication);
    }

    protected void show() {
        items = getCollection().getIssueList(
            WhatTheDuck.getSelectedCountry(),
            WhatTheDuck.getSelectedPublication()
        );

        if (items.size() == 0) {
            TextView emptyListText = this.findViewById(R.id.emptyList);
            emptyListText.setVisibility(TextView.VISIBLE);
        }

        this.itemAdapter = new IssueAdapter(this, items);
        setListAdapter(this.itemAdapter);

        EditText filterEditText = this.findViewById(R.id.filter);
        if (items.size() > 20) {
            filterEditText.setVisibility(EditText.VISIBLE);

            filterEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    IssueList.this.itemAdapter.updateFilteredList(s.toString());
                    setListAdapter(IssueList.this.itemAdapter);
                }
            });
        }
        else {
            filterEditText.setVisibility(EditText.GONE);
        }

        this.getListView().setDivider(null);
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
            Issue selectedIssue = (Issue) this.getListView().getItemAtPosition(((Long) id).intValue());
            if (WhatTheDuck.userCollection.getIssue(WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication(), selectedIssue.getIssueNumber()) != null) {
                WhatTheDuck.wtd.info(this, R.string.input_error__issue_already_possessed);
            }
            else {
                AddIssue.showAddIssueDialog(IssueList.this, selectedIssue);
            }
        }
        super.onListItemClick(l, v, position, id);
    }

    public String getSelectedCountry() {
        return selectedCountry;
    }

    public String getSelectedPublication() {
        return selectedPublication;
    }
}
