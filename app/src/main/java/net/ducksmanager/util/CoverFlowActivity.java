package net.ducksmanager.util;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithUserIssueDetails;
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueDetails;
import net.ducksmanager.whattheduck.AddIssue;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;


public class CoverFlowActivity extends AppCompatActivity {

    private List<CoverSearchIssueWithUserIssueDetails> data;
    private TextSwitcher mResultNumber;
    private ImageSwitcher mCountryBadge;
    private ImageSwitcher mIssueCondition;

    private TextView mIssueConditionText;
    private TextView mTitleText;

    public static CoverSearchIssueWithUserIssueDetails currentSuggestion = null;

    CoverFlowAdapter adapter;
    FeatureCoverFlow coverFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((WhatTheDuckApplication) getApplication()).trackActivity(this);
        setContentView(R.layout.activity_coverflow);

        WhatTheDuck.appDB.coverSearchIssueDao().findAll().observe(this, searchIssues -> {
            data = searchIssues;
            adapter = new CoverFlowAdapter(this);
            adapter.setData(searchIssues);

            coverFlow = findViewById(R.id.coverflow);
            coverFlow.setAdapter(adapter);

            coverFlow.setOnItemClickListener((parent, view, position, id) -> {
                if (currentSuggestion.getUserIssue() == null) {
                    WhatTheDuck.setSelectedCountry (currentSuggestion.getCoverSearchIssue().getCoverCountryCode());
                    WhatTheDuck.setSelectedPublication (currentSuggestion.getCoverSearchIssue().getCoverPublicationCode());
                    WhatTheDuck.setSelectedIssue(currentSuggestion.getCoverSearchIssue().getCoverIssueNumber());

                    CoverFlowActivity.this.startActivity(new Intent(CoverFlowActivity.this, AddIssue.class));
                }
                else {
                    Toast.makeText(
                            CoverFlowActivity.this,
                            R.string.issue_already_possessed,
                            Toast.LENGTH_SHORT)
                            .show();
                }
            });

            coverFlow.setOnScrollPositionListener(new FeatureCoverFlow.OnScrollPositionListener() {
                @Override
                public void onScrolledToPosition(int position) {
                    currentSuggestion = data.get(position);

                    String uri = "@drawable/flags_" + currentSuggestion.getCoverSearchIssue().getCoverCountryCode();
                    int imageResource = getResources().getIdentifier(uri, null, getPackageName());

                    if (imageResource == 0) {
                        imageResource = R.drawable.flags_unknown;
                    }
                    mCountryBadge.setVisibility(View.VISIBLE);
                    mCountryBadge.setImageResource(imageResource);

                    mIssueConditionText.setVisibility(View.VISIBLE);

                    if (currentSuggestion.getUserIssue() != null) {
                        String condition = currentSuggestion.getUserIssue().getCondition();
                        mIssueCondition.setVisibility(View.VISIBLE);
                        mIssueCondition.setImageResource(InducksIssueWithUserIssueDetails.issueConditionToResourceId(condition));
                        mIssueConditionText.setText(InducksIssueWithUserIssueDetails.issueConditionToStringId(condition));
                        mIssueConditionText.setTextSize(18);
                    }
                    else {
                        mIssueCondition.setVisibility(View.GONE);
                        mIssueConditionText.setText(R.string.add_cover);
                        mIssueConditionText.setTextSize(14);
                    }

                    mResultNumber.setVisibility(View.VISIBLE);
                    mResultNumber.setText(getResources().getString(R.string.result) + " " + (position + 1) + "/" + data.size());

                    mTitleText.setVisibility(View.VISIBLE);
                    mTitleText.setText(data.get(position).getCoverSearchIssue().getCoverPublicationTitle() + " " + data.get(position).getCoverSearchIssue().getCoverIssueNumber());
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
        });

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
    }
}
