package net.ducksmanager.util

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow.OnScrollPositionListener
import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithUserIssueDetails
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToResourceId
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToStringId
import net.ducksmanager.whattheduck.AddIssue
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck

class CoverFlowActivity : AppCompatActivity() {
    private lateinit var data: List<CoverSearchIssueWithUserIssueDetails>

    private lateinit var adapter: CoverFlowAdapter

    private lateinit var mResultNumber: TextSwitcher
    private lateinit var mCountryBadge: ImageSwitcher
    private lateinit var mIssueCondition: ImageSwitcher
    private lateinit var mIssueConditionText: TextView
    private lateinit var mTitleText: TextView
    private lateinit var coverFlow: FeatureCoverFlow

    companion object {
        @JvmField
        var currentSuggestion: CoverSearchIssueWithUserIssueDetails? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuck).trackActivity(this)
        setContentView(R.layout.activity_coverflow)
        WhatTheDuck.appDB.coverSearchIssueDao().findAll().observe(this, Observer { searchIssues: List<CoverSearchIssueWithUserIssueDetails> ->
            data = searchIssues
            adapter = CoverFlowAdapter(this)
            adapter.setData(searchIssues)
            coverFlow = findViewById(R.id.coverflow)
            coverFlow.adapter = adapter
            coverFlow.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
                if (currentSuggestion!!.userIssue == null) {
                    WhatTheDuck.selectedCountry = currentSuggestion!!.coverSearchIssue.coverCountryCode
                    WhatTheDuck.selectedPublication = currentSuggestion!!.coverSearchIssue.coverPublicationCode
                    WhatTheDuck.selectedIssue = currentSuggestion!!.coverSearchIssue.coverIssueNumber
                    this@CoverFlowActivity.startActivity(Intent(this@CoverFlowActivity, AddIssue::class.java))
                } else {
                    Toast.makeText(
                        this@CoverFlowActivity,
                        R.string.issue_already_possessed,
                        Toast.LENGTH_SHORT)
                        .show()
                }
            }
            coverFlow.setOnScrollPositionListener(object : OnScrollPositionListener {
                override fun onScrolledToPosition(position: Int) {
                    currentSuggestion = data[position]

                    val uri = "@drawable/flags_" + currentSuggestion!!.coverSearchIssue.coverCountryCode
                    var imageResource = resources.getIdentifier(uri, null, packageName)
                    if (imageResource == 0) {
                        imageResource = R.drawable.flags_unknown
                    }

                    mCountryBadge.visibility = View.VISIBLE
                    mCountryBadge.setImageResource(imageResource)
                    mIssueConditionText.visibility = View.VISIBLE

                    val condition = currentSuggestion?.userIssue?.condition
                    if (condition != null) {
                        mIssueCondition.visibility = View.VISIBLE
                        mIssueCondition.setImageResource(issueConditionToResourceId(condition))
                        mIssueConditionText.setText(issueConditionToStringId(condition))
                        mIssueConditionText.textSize = 18f
                    } else {
                        mIssueCondition.visibility = View.GONE
                        mIssueConditionText.setText(R.string.add_cover)
                        mIssueConditionText.textSize = 14f
                    }

                    mResultNumber.visibility = View.VISIBLE
                    mResultNumber.setText(resources.getString(R.string.result) + " " + (position + 1) + "/" + data.size)

                    mTitleText.visibility = View.VISIBLE
                    mTitleText.text = data[position].coverSearchIssue.coverPublicationTitle + " " + data[position].coverSearchIssue.coverIssueNumber
                }

                override fun onScrolling() {
                    mResultNumber.visibility = View.INVISIBLE
                    mTitleText.visibility = View.INVISIBLE
                    mCountryBadge.visibility = View.INVISIBLE
                    mIssueCondition.visibility = View.INVISIBLE
                    mIssueConditionText.visibility = View.INVISIBLE
                }
            })
        })
        mResultNumber = findViewById(R.id.resultNumber)
        mResultNumber.setFactory {
            LayoutInflater.from(this@CoverFlowActivity).inflate(R.layout.item_title, null)
        }
        mCountryBadge = findViewById(R.id.imageSwitcherCountryBadge)
        mCountryBadge.setFactory { ImageView(applicationContext) }

        mIssueCondition = findViewById(R.id.prefiximage)
        mIssueCondition.setFactory { ImageView(applicationContext) }

        val mIssueConditionTextSwitcher = findViewById<TextSwitcher>(R.id.prefiximage_description)
        mIssueConditionTextSwitcher.setFactory {
            val inflater = LayoutInflater.from(this@CoverFlowActivity)
            mIssueConditionText = inflater.inflate(R.layout.item_title, null) as TextView
            mIssueConditionText
        }
        val mTitleSwitcher = findViewById<TextSwitcher>(R.id.title)
        mTitleSwitcher.setFactory {
            val inflater = LayoutInflater.from(this@CoverFlowActivity)
            mTitleText = inflater.inflate(R.layout.item_title, null) as TextView
            mTitleText
        }
    }
}