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

import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithUserIssueDetails;
import net.ducksmanager.whattheduck.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import static net.ducksmanager.whattheduck.WhatTheDuckApplication.applicationVersion;

class CoverFlowAdapter extends BaseAdapter {

    private static final HashMap<String,Bitmap> imageCache = new HashMap<>();

    private List<CoverSearchIssueWithUserIssueDetails> data;
    private final Context context;

    CoverFlowAdapter(Context context) {
        this.context = context;
    }

    void setData(List<CoverSearchIssueWithUserIssueDetails> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int pos) {
        return data.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        ViewHolder viewHolder;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_coverflow, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.text = rowView.findViewById(R.id.label);
            viewHolder.image = rowView.findViewById(R.id.image);
            viewHolder.image.setTag(data.get(position).getCoverSearchIssue().getCoverFullUrl());
            viewHolder.progressBar = rowView.findViewById(R.id.progressBar);

            DownloadImagesTask task = new DownloadImagesTask(viewHolder.progressBar);
            task.execute(viewHolder.image);

            rowView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        viewHolder.text.setText(data.get(position).getCoverSearchIssue().getCoverIssueNumber());

        return rowView;
    }


    private static class ViewHolder {
        TextView text;
        ImageView image;
        ProgressBar progressBar;
    }

    private static class DownloadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

        final WeakReference<ProgressBar> progressBarRef;
        WeakReference<ImageView> imageViewRef = null;

        DownloadImagesTask(ProgressBar progressBar) {
            this.progressBarRef = new WeakReference<>(progressBar);
        }

        @Override
        protected Bitmap doInBackground(ImageView... imageViews) {
            this.imageViewRef = new WeakReference<>(imageViews[0]);
            //noinspection WrongThread
            return downloadImage((String) imageViewRef.get().getTag());
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            ImageView imageView = imageViewRef.get();
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(result);
            progressBarRef.get().setVisibility(View.GONE);
        }

        private Bitmap downloadImage(String url) {
            if (imageCache.containsKey(url)) {
                return imageCache.get(url);
            }

            Bitmap bmp;
            try{
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                con.setRequestProperty("X-Dm-Version", applicationVersion);

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

