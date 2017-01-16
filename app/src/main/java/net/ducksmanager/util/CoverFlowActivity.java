package net.ducksmanager.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.TextSwitcher;
import android.widget.ViewSwitcher;

import net.ducksmanager.whattheduck.IssueWithFullUrl;
import net.ducksmanager.whattheduck.R;

import java.util.ArrayList;

import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;


public class CoverFlowActivity extends Activity {

    private FeatureCoverFlow mCoverFlow;
    private CoverFlowAdapter mAdapter;
    private ArrayList<IssueWithFullUrl> mData = new ArrayList<>(0);
    private TextSwitcher mTitle;
    private ImageSwitcher mFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coverflow);

        Bundle extras = getIntent().getExtras();
        mData = (ArrayList<IssueWithFullUrl>) extras.get("resultCollection");

        mFlag = (ImageSwitcher) findViewById(R.id.coverResultCountryFlag);

        mTitle = (TextSwitcher) findViewById(R.id.coverResultTitle);
        mTitle.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                LayoutInflater inflater = LayoutInflater.from(CoverFlowActivity.this);
                return inflater.inflate(R.layout.item_title, null);
            }
        });

        mAdapter = new CoverFlowAdapter(this);
        mAdapter.setData(mData);
        mCoverFlow = (FeatureCoverFlow) findViewById(R.id.coverflow);
        mCoverFlow.setAdapter(mAdapter);

        mCoverFlow.setOnScrollPositionListener(new FeatureCoverFlow.OnScrollPositionListener() {
            @Override
            public void onScrolledToPosition(int position) {
                mFlag.setVisibility(View.VISIBLE);
                mFlag.setBackgroundResource(FlagHelper.getImageResource(CoverFlowActivity.this, mData.get(position).getCountryCode()));
                mTitle.setText(mData.get(position).toString());
            }

            @Override
            public void onScrolling() {
                mTitle.setText("");
                mFlag.setVisibility(View.INVISIBLE);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_coverflow_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }
}
