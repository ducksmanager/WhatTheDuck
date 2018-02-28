package net.ducksmanager.whattheduck;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class IssueAdapter extends ItemAdapter<Issue> {
    public IssueAdapter(List list, ArrayList<Issue> items) {
        super(list, R.layout.row_edge, items);
    }

    @Override
    protected boolean isHighlighted(Issue i) {
        return i.getIssueCondition() != null;
    }

    @Override
    protected Integer getPrefixImageResource(Issue i, Activity activity) {
        return null;
    }

    @Override
    protected Integer getSuffixImageResource(Issue i) {
        return null;
    }

    @Override
    protected String getSuffixText(Issue i) {
        return null;
    }

    TextView getTitleTextView(View mainView) {
        return null;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);

        if (resourceToInflate != R.layout.row) {
            Issue i = getItem(position);

            String url = WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_EDGES_URL)
                + "/edges/"
                + WhatTheDuck.getSelectedCountry()
                + "/gen/"
                + WhatTheDuck.getSelectedPublication()
                    .replaceFirst("[^/]+/", "")
                    .replaceAll(" ", "")
                + "." + i.getIssueNumber() + ".png";


//            Picasso.with(IssueAdapter.this.getContext()).load(url).into(new Target() {
//                @Override
//                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                    int width = bitmap.getWidth();
//                    int height = bitmap.getHeight();
//                    edgeView.setLayoutParams(new LinearLayout.LayoutParams(width,height));
//                    ((ImageView)edgeView).setImageBitmap(bitmap);
//                }
//
//                @Override
//                public void onBitmapFailed(Drawable errorDrawable) {
//
//                }
//
//                @Override
//                public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                }
//            });

            View edgeView = v.findViewById(R.id.itemedge);
            Picasso
                .with(getContext())
                .load(url)
                .rotate(90f)
                .into((ImageView) edgeView);

//            edgeView.setLayoutParams(new LinearLayout.LayoutParams(250,edgeView.getLayoutParams().height));
        }

        return v;
    }

    @Override
    protected String getText(Issue i) {
        return i.getIssueNumber();
    }

    @Override
    protected String getComparatorText(Issue i) {
        return getText(i);
    }
}
