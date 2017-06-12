package net.ducksmanager.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import net.ducksmanager.whattheduck.Issue;
import net.ducksmanager.whattheduck.IssueWithFullUrl;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import java.util.ArrayList;

import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;


public class CoverFlowActivity extends Activity {

    private FeatureCoverFlow mCoverFlow;
    private CoverFlowAdapter mAdapter;
    private ArrayList<IssueWithFullUrl> mData = new ArrayList<>(0);
    private TextSwitcher mResultNumber;
    private ImageSwitcher mCountryBadge;
    private ImageSwitcher mIssueCondition;
    private TextSwitcher mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coverflow);

        Bundle extras = getIntent().getExtras();
        mData = (ArrayList<IssueWithFullUrl>) extras.get("resultCollection");

        mResultNumber = (TextSwitcher) findViewById(R.id.resultNumber);
        mResultNumber.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                LayoutInflater inflater = LayoutInflater.from(CoverFlowActivity.this);
                return inflater.inflate(R.layout.item_title, null);
            }
        });

        mCountryBadge = (ImageSwitcher) findViewById(R.id.imageSwitcherCountryBadge);
        mCountryBadge.setFactory(new ViewSwitcher.ViewFactory() {
             public View makeView() {
                 return new ImageView(getApplicationContext());
             }
         });

        mIssueCondition = (ImageSwitcher) findViewById(R.id.issuecondition);
        mIssueCondition.setFactory(new ViewSwitcher.ViewFactory() {
             public View makeView() {
                 return new ImageView(getApplicationContext());
             }
         });

        mTitle = (TextSwitcher) findViewById(R.id.title);
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

        mCoverFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(CoverFlowActivity.this,
                        mData.get(position % mData.size()).getIssueNumber(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        mCoverFlow.setOnScrollPositionListener(new FeatureCoverFlow.OnScrollPositionListener() {
            @Override
            public void onScrolledToPosition(int position) {
                String uri = "@drawable/flags_" + mData.get(position).getCountryCode();
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());

                if (imageResource == 0) {
                    imageResource = R.drawable.flags_unknown;
                }
                mCountryBadge.setVisibility(View.VISIBLE);
                mCountryBadge.setImageResource(imageResource);

                mIssueCondition.setVisibility(View.VISIBLE);

                Issue existingIssue = WhatTheDuck.userCollection.getIssue(mData.get(position).getCountryCode(), mData.get(position).getPublicationCode(), mData.get(position).getIssueNumber());
                if (existingIssue != null) {
                    Issue.IssueCondition condition = existingIssue.getIssueCondition();
                    mIssueCondition.setImageResource(Issue.issueConditionToResourceId(condition));
                }

                mResultNumber.setText("Résultat " + (position + 1) + "/" + mData.size());
                mTitle.setText(mData.get(position).getPublicationTitle() + " "
                             + mData.get(position).getIssueNumber());
            }

            @Override
            public void onScrolling() {
                mResultNumber.setText("");
                mTitle.setText("");
                mCountryBadge.setVisibility(View.INVISIBLE);
                mIssueCondition.setVisibility(View.INVISIBLE);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_coverflow_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
