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

import net.ducksmanager.whattheduck.IssueComplete;
import net.ducksmanager.whattheduck.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class CoverFlowAdapter extends BaseAdapter {

    private ArrayList<IssueComplete> mData = new ArrayList<>(0);
    private Context mContext;

    CoverFlowAdapter(Context context) {
        mContext = context;
    }

    void setData(ArrayList<IssueComplete> data) {
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
            viewHolder.image = (ImageView) rowView.findViewById(R.id.image);
            viewHolder.image.setTag("http://www.google.com/logos/2013/estonia_independence_day_2013-1057005.3-hp.jpg");
            viewHolder.progressBar = (ProgressBar) rowView.findViewById(R.id.progressBarImageDownload);

            DownloadImagesTask task = new DownloadImagesTask(viewHolder.progressBar);
            task.execute(viewHolder.image);

            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.text.setText(mData.get(position).getIssue().getIssueNumber());

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
            return downloadImage((String)imageView.getTag());
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(result);
            progressBar.setVisibility(View.GONE);
        }

        private Bitmap downloadImage(String url) {

            Bitmap bmp;
            try{
                URL ulrn = new URL(url);
                HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                InputStream is = con.getInputStream();
                bmp = BitmapFactory.decodeStream(is);
                if (null != bmp)
                    return bmp;

            } catch(Exception ignored){
                // TODO show default cover
            }
            return null;
        }
    }
}

