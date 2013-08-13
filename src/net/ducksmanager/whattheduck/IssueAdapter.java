package net.ducksmanager.whattheduck;

import java.util.ArrayList;

import net.ducksmanager.whattheduck.Collection.CollectionType;
import android.content.Context;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class IssueAdapter extends ArrayAdapter<Issue> {

	private ArrayList<Issue> items;
	private String typeCollection;

	public IssueAdapter(Context context, int textViewResourceId,
			ArrayList<Issue> items, String typeCollection) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.typeCollection = typeCollection;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.row, null);
		}
		v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
    			CheckBox checkBoxSelection = (CheckBox) view.findViewById(R.id.checkBoxSelection);
    			checkBoxSelection.setChecked(!checkBoxSelection.isChecked());
            }
		});
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
			if (hasIssueCondition || this.typeCollection.equals(CollectionType.USER.toString())) {
				checkBoxSelection.setVisibility(CheckBox.INVISIBLE);
			}
		}
		return v;
	}
}