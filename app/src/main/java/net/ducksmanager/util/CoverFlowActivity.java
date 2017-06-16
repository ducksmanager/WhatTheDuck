package net.ducksmanager.util;

import android.app.Activity;
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

        TextSwitcher mIssueConditionTextSwitcher = (TextSwitcher) findViewById(R.id.issuecondition_description);
        mIssueConditionTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                mIssueConditionText = new TextView(CoverFlowActivity.this);
                return mIssueConditionText;
            }
        });

        TextSwitcher mTitleSwitcher = (TextSwitcher) findViewById(R.id.title);
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

        FeatureCoverFlow mCoverFlow = (FeatureCoverFlow) findViewById(R.id.coverflow);
        mCoverFlow.setAdapter(mAdapter);

        mCoverFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Issue existingIssue = WhatTheDuck.userCollection.getIssue(current.getCountryCode(), current.getPublicationCode(), current.getIssueNumber());
            if (existingIssue == null) {
                Issue newIssue = new Issue(current.getIssueNumber(), (Issue.IssueCondition) null);
                WhatTheDuck.setSelectedPublication (current.getPublicationCode());
                AddIssue.showAddIssueDialog(CoverFlowActivity.this, newIssue);
            }
            else {
                Toast.makeText(
                    CoverFlowActivity.this,
                    R.string.numero_deja_possede,
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
                    mIssueConditionText.setText(R.string.ajouter_couverture);
                    mIssueConditionText.setTextSize(14);
                }

                mResultNumber.setVisibility(View.VISIBLE);
                mResultNumber.setText(getResources().getString(R.string.resultat) + " " + (position + 1) + "/" + mData.size());

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
