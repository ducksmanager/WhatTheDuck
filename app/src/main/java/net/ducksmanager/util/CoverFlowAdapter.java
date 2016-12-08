package net.ducksmanager.util;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.ducksmanager.whattheduck.IssueComplete;
import net.ducksmanager.whattheduck.R;

import java.util.ArrayList;

public class CoverFlowAdapter extends BaseAdapter {

    private ArrayList<IssueComplete> mData = new ArrayList<>(0);
    private Context mContext;

    public CoverFlowAdapter(Context context) {
        mContext = context;
    }

    public void setData(ArrayList<IssueComplete> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int pos) {
        return mData.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_coverflow, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.label);
            viewHolder.image = (ImageView) rowView
                    .findViewById(R.id.image);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

//        holder.image.setImageResource(mData.get(position).imageResId);
        holder.image.setImageResource(R.drawable.flags_ae);
        holder.text.setText(mData.get(position).getIssue().getIssueNumber());

        return rowView;
    }


    static class ViewHolder {
        public TextView text;
        public ImageView image;
    }
}

