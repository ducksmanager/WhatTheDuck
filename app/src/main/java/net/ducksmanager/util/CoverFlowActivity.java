package net.ducksmanager.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import net.ducksmanager.whattheduck.AddIssue;
import net.ducksmanager.whattheduck.Issue;
import net.ducksmanager.whattheduck.IssueWithFullUrl;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import java.util.ArrayList;

import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;


public class CoverFlowActivity extends Activity {

    private ArrayList<IssueWithFullUrl> mData = new ArrayList<>(0);
    private TextSwitcher mResultNumber;
    private ImageSwitcher mCountryBadge;
    private ImageSwitcher mIssueCondition;

    private TextView mIssueConditionText;
    private TextView mTitleText;
    
    private IssueWithFullUrl current = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

        setContentView(R.layout.activity_coverflow);

        Bundle extras = getIntent().getExtras();
        mData = (ArrayList<IssueWithFullUrl>) extras.get("resultCollection");

        mResultNumber = findViewById(R.id.resultNumber);
        mResultNumber.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                LayoutInflater inflater = LayoutInflater.from(CoverFlowActivity.this);
                return inflater.inflate(R.layout.item_title, null);
            }
        });

        mCountryBadge = findViewById(R.id.imageSwitcherCountryBadge);
        mCountryBadge.setFactory(new ViewSwitcher.ViewFactory() {
             public View makeView() {
                 return new ImageView(getApplicationContext());
            }
         });

        mIssueCondition = findViewById(R.id.prefiximage);
        mIssueCondition.setFactory(new ViewSwitcher.ViewFactory() {
             public View makeView() {
                 return new ImageView(getApplicationContext());
            }
         });

        TextSwitcher mIssueConditionTextSwitcher = findViewById(R.id.prefiximage_description);
        mIssueConditionTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                LayoutInflater inflater = LayoutInflater.from(CoverFlowActivity.this);
                mIssueConditionText = (TextView) inflater.inflate(R.layout.item_title, null);
                return mIssueConditionText;
            }
        });

        TextSwitcher mTitleSwitcher = findViewById(R.id.title);
        mTitleSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                LayoutInflater inflater = LayoutInflater.from(CoverFlowActivity.this);
                mTitleText = (TextView) inflater.inflate(R.layout.item_title, null);
                return mTitleText;
            }
        });

        CoverFlowAdapter mAdapter = new CoverFlowAdapter(this);
        mAdapter.setData(mData);

        FeatureCoverFlow mCoverFlow = findViewById(R.id.coverflow);
        mCoverFlow.setAdapter(mAdapter);

        mCoverFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Issue existingIssue = WhatTheDuck.userCollection.getIssue(current.getCountryCode(), current.getPublicationCode(), current.getIssueNumber());
            if (existingIssue == null) {
                Issue newIssue = new Issue(current.getIssueNumber(), (Issue.IssueCondition) null);
                WhatTheDuck.setSelectedCountry (current.getCountryCode());
                WhatTheDuck.setSelectedPublication (current.getPublicationCode());
                WhatTheDuck.setSelectedIssue(newIssue.getIssueNumber());

                Intent i = new Intent(CoverFlowActivity.this, AddIssue.class);
                startActivity(i);
            }
            else {
                Toast.makeText(
                    CoverFlowActivity.this,
                    R.string.issue_already_possessed,
                    Toast.LENGTH_SHORT)
                .show();
            }
            }
        });

        mCoverFlow.setOnScrollPositionListener(new FeatureCoverFlow.OnScrollPositionListener() {
            @Override
            public void onScrolledToPosition(int position) {
                current = mData.get(position);
                
                String uri = "@drawable/flags_" + current.getCountryCode();
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());

                if (imageResource == 0) {
                    imageResource = R.drawable.flags_unknown;
                }
                mCountryBadge.setVisibility(View.VISIBLE);
                mCountryBadge.setImageResource(imageResource);

                mIssueConditionText.setVisibility(View.VISIBLE);

                Issue existingIssue = WhatTheDuck.userCollection.getIssue(current.getCountryCode(), current.getPublicationCode(), current.getIssueNumber());
                if (existingIssue != null) {
                    Issue.IssueCondition condition = existingIssue.getIssueCondition();
                    mIssueCondition.setVisibility(View.VISIBLE);
                    mIssueCondition.setImageResource(Issue.issueConditionToResourceId(condition));
                    mIssueConditionText.setText(getResources().getString(Issue.issueConditionToStringId(condition)));
                    mIssueConditionText.setTextSize(18);
                }
                else {
                    mIssueCondition.setVisibility(View.GONE);
                    mIssueConditionText.setText(R.string.add_cover);
                    mIssueConditionText.setTextSize(14);
                }

                mResultNumber.setVisibility(View.VISIBLE);
                mResultNumber.setText(getResources().getString(R.string.result) + " " + (position + 1) + "/" + mData.size());

                mTitleText.setVisibility(View.VISIBLE);
                mTitleText.setText(mData.get(position).getPublicationTitle() + " " + mData.get(position).getIssueNumber());
            }

            @Override
            public void onScrolling() {
                mResultNumber.setVisibility(View.INVISIBLE);
                mTitleText.setVisibility(View.INVISIBLE);
                mCountryBadge.setVisibility(View.INVISIBLE);
                mIssueCondition.setVisibility(View.INVISIBLE);
                mIssueConditionText.setVisibility(View.INVISIBLE);
            }
        });
    }
}
