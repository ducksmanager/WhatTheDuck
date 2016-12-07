package net.ducksmanager.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextSwitcher;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import net.ducksmanager.whattheduck.Issue;
import net.ducksmanager.whattheduck.R;

import java.util.ArrayList;

import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;


public class CoverFlowActivity extends Activity {

    private FeatureCoverFlow mCoverFlow;
    private CoverFlowAdapter mAdapter;
    private ArrayList<Issue> mData = new ArrayList<>(0);
    private TextSwitcher mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coverflow);

        mData.add(new Issue("1", Issue.NO_CONDITION));
        mData.add(new Issue("2", Issue.NO_CONDITION));
        mData.add(new Issue("3", Issue.NO_CONDITION));

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
                mTitle.setText(mData.get(position).getIssueNumber());
            }

            @Override
            public void onScrolling() {
                mTitle.setText("");
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
