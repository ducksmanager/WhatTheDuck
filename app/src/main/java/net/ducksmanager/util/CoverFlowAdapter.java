package net.ducksmanager.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.ducksmanager.whattheduck.IssueWithFullUrl;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

class CoverFlowAdapter extends BaseAdapter {

    private final HashMap<String,Bitmap> imageCache = new HashMap<>();

    private ArrayList<IssueWithFullUrl> mData = new ArrayList<>(0);
    private final Context mContext;

    CoverFlowAdapter(Context context) {
        mContext = context;
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

        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_coverflow, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = rowView.findViewById(R.id.label);
            viewHolder.image = rowView.findViewById(R.id.image);
            viewHolder.image.setTag(mData.get(position).getFullUrl());
            viewHolder.progressBar = rowView.findViewById(R.id.progressBarImageDownload);

            DownloadImagesTask task = new DownloadImagesTask(viewHolder.progressBar);
            task.execute(viewHolder.image);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.text.setText(mData.get(position).getIssueNumber());

        return rowView;
    }


    private static class ViewHolder {
        TextView text;
        ImageView image;
        ProgressBar progressBar;
    }

    private class DownloadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

        ProgressBar progressBar = null;
        ImageView imageView = null;

        DownloadImagesTask(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        protected Bitmap doInBackground(ImageView... imageViews) {
            this.imageView = imageViews[0];
            //noinspection WrongThread
            return downloadImage((String)imageView.getTag());
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(result);
            progressBar.setVisibility(View.GONE);
        }

        private Bitmap downloadImage(String url) {
            if (imageCache.containsKey(url)) {
                return imageCache.get(url);
            }

            Bitmap bmp;
            try{
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                con.setRequestProperty("X-Dm-Version", WhatTheDuck.wtd.getApplicationVersion());

                InputStream is = con.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] tmpBuffer = new byte[256];
                int n;
                while ((n = is.read(tmpBuffer)) >= 0) {
                    bos.write(tmpBuffer, 0, n);
                }

                bmp = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().length);
                if (null != bmp) {
                    imageCache.put(url, bmp);
                    return bmp;
                }

            } catch(Exception e){
                System.err.println(e.getMessage());
            }
            return null;
        }
    }
}

