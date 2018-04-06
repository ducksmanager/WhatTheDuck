package net.ducksmanager.util;


import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.ducksmanager.whattheduck.IssueWithFullUrl;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;

class CoverFlowAdapter extends BaseAdapter {

    private ArrayList<IssueWithFullUrl> mData = new ArrayList<>(0);
    private final Context mContext;
    private final Picasso picasso;

    private static OkHttpClient httpClient = new OkHttpClient.Builder()
        .addInterceptor(chain -> {
            Request newRequest;
            String dmVersion = null;
            try {
                dmVersion = WhatTheDuck.wtd.getApplicationVersion();
            } catch (PackageManager.NameNotFoundException e) {
                dmVersion = "?";
            } finally {
                newRequest = chain.request().newBuilder()
                    .addHeader("X-Dm-Version", dmVersion).build();
            }
            return chain.proceed(newRequest);
        })
        .build();


    CoverFlowAdapter(Context context) {
        mContext = context;
        picasso = new Picasso.Builder(mContext)
            .downloader(new OkHttp3Downloader(httpClient))
            .listener((picasso, uri, exception) -> exception.printStackTrace())
            .build();
    }

    void setData(ArrayList<IssueWithFullUrl> data) {
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

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_coverflow, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.label);
            viewHolder.image = convertView.findViewById(R.id.image);
            viewHolder.progressBar = convertView.findViewById(R.id.progressBarImageDownload);

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        picasso.load(mData.get(position).getFullUrl()).into(viewHolder.image, new Callback() {
            @Override
            public void onSuccess() {
                viewHolder.image.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                viewHolder.image.setVisibility(View.VISIBLE);
                viewHolder.progressBar.setVisibility(View.GONE);
            }
        });

        viewHolder.text.setText(mData.get(position).getIssueNumber());

        return convertView;
    }


    private static class ViewHolder {
        TextView text;
        ImageView image;
        ProgressBar progressBar;
    }
}

