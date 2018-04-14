package net.ducksmanager.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import net.ducksmanager.retrievetasks.GetPurchaseList;
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
    
    private static IssueWithFullUrl currentSuggestion = null;
    public static String currentCoverUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

        setContentView(R.layout.activity_coverflow);

        Bundle extras = getIntent().getExtras();
        mData = (ArrayList<IssueWithFullUrl>) extras.get("resultCollection");

        mResultNumber = findViewById(R.id.resultNumber);
        mResultNumber.setFactory(() -> {
            LayoutInflater inflater = LayoutInflater.from(CoverFlowActivity.this);
            return inflater.inflate(R.layout.item_title, null);
        });

        mCountryBadge = findViewById(R.id.imageSwitcherCountryBadge);
        mCountryBadge.setFactory(() ->
            new ImageView(getApplicationContext()));

        mIssueCondition = findViewById(R.id.prefiximage);
        mIssueCondition.setFactory(() ->
            new ImageView(getApplicationContext()));

        TextSwitcher mIssueConditionTextSwitcher = findViewById(R.id.prefiximage_description);
        mIssueConditionTextSwitcher.setFactory(() -> {
            LayoutInflater inflater = LayoutInflater.from(CoverFlowActivity.this);
            mIssueConditionText = (TextView) inflater.inflate(R.layout.item_title, null);
            return mIssueConditionText;
        });

        TextSwitcher mTitleSwitcher = findViewById(R.id.title);
        mTitleSwitcher.setFactory(() -> {
            LayoutInflater inflater = LayoutInflater.from(CoverFlowActivity.this);
            mTitleText = (TextView) inflater.inflate(R.layout.item_title, null);
            return mTitleText;
        });

        CoverFlowAdapter mAdapter = new CoverFlowAdapter(this);
        mAdapter.setData(mData);

        FeatureCoverFlow mCoverFlow = findViewById(R.id.coverflow);
        mCoverFlow.setAdapter(mAdapter);

        mCoverFlow.setOnItemClickListener((parent, view, position, id) -> {
            Issue existingIssue = WhatTheDuck.userCollection.getIssue(currentSuggestion.getCountryCode(), currentSuggestion.getPublicationCode(), currentSuggestion.getIssueNumber());
            if (existingIssue == null) {
                Issue newIssue = new Issue(currentSuggestion.getIssueNumber(), null);
                WhatTheDuck.setSelectedCountry (currentSuggestion.getCountryCode());
                WhatTheDuck.setSelectedPublication (currentSuggestion.getPublicationCode());
                WhatTheDuck.setSelectedIssue(newIssue.getIssueNumber());

                GetPurchaseList.initAndShowAddIssue(CoverFlowActivity.this);
            }
            else {
                Toast.makeText(
                    CoverFlowActivity.this,
                    R.string.issue_already_possessed,
                    Toast.LENGTH_SHORT)
                .show();
            }
        });

        mCoverFlow.setOnScrollPositionListener(new FeatureCoverFlow.OnScrollPositionListener() {
            @Override
            public void onScrolledToPosition(int position) {
                currentSuggestion = mData.get(position);
                currentCoverUrl = currentSuggestion.getFullUrl();

                String uri = "@drawable/flags_" + currentSuggestion.getCountryCode();
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());

                if (imageResource == 0) {
                    imageResource = R.drawable.flags_unknown;
                }
                mCountryBadge.setVisibility(View.VISIBLE);
                mCountryBadge.setImageResource(imageResource);

                mIssueConditionText.setVisibility(View.VISIBLE);

                Issue existingIssue = WhatTheDuck.userCollection.getIssue(currentSuggestion.getCountryCode(), currentSuggestion.getPublicationCode(), currentSuggestion.getIssueNumber());
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
