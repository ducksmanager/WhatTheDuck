package net.ducksmanager.whattheduck;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class IssueAdapter extends ArrayAdapter<Issue> {

    private final ArrayList<Issue> items;

    public IssueAdapter(Context context, ArrayList<Issue> items) {
        super(context, R.layout.row, items);
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.row, null);
        }
        Issue i = items.get(position);
        if (i != null) {
            TextView issueNumber = (TextView) v.findViewById(R.id.issuenumber);
            ImageView imageCondition = (ImageView) v.findViewById(R.id.issuecondition);
            if (issueNumber != null)
                issueNumber.setText(i.getIssueNumber());
            if (imageCondition != null && i.getIssueCondition() != null) {
                int resourceId = Issue.issueConditionToResourceId(i.getIssueCondition());
                imageCondition.setImageResource(resourceId);
            }
        }
        return v;
    }
}