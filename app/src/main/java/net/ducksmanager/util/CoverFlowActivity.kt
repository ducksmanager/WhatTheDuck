package net.ducksmanager.util

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.Toast

import net.ducksmanager.retrievetasks.GetPurchaseList
import net.ducksmanager.whattheduck.Issue
import net.ducksmanager.whattheduck.IssueWithFullUrl
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuckApplication

import java.util.ArrayList

import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow


class CoverFlowActivity : Activity() {

    private var mData = ArrayList<IssueWithFullUrl>(0)
    private var mResultNumber: TextSwitcher? = null
    private var mCountryBadge: ImageSwitcher? = null
    private var mIssueCondition: ImageSwitcher? = null

    private var mIssueConditionText: TextView? = null
    private var mTitleText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)

        (application as WhatTheDuckApplication).trackActivity(this)

        setContentView(R.layout.activity_coverflow)

        val extras = intent.extras
        mData = extras.get("resultCollection") as ArrayList<IssueWithFullUrl>

        mResultNumber = findViewById(R.id.resultNumber)
        mResultNumber!!.setFactory {
            val inflater = LayoutInflater.from(this@CoverFlowActivity)
            inflater.inflate(R.layout.item_title, null)
        }

        mCountryBadge = findViewById(R.id.imageSwitcherCountryBadge)
        mCountryBadge!!.setFactory { ImageView(applicationContext) }

        mIssueCondition = findViewById(R.id.prefiximage)
        mIssueCondition!!.setFactory { ImageView(applicationContext) }

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

        val mAdapter = CoverFlowAdapter(this)
        mAdapter.setData(mData)

        val mCoverFlow = findViewById<FeatureCoverFlow>(R.id.coverflow)
        mCoverFlow.adapter = mAdapter

        mCoverFlow.setOnItemClickListener { parent, view, position, id ->
            val existingIssue = WhatTheDuck.userCollection.getIssue(currentSuggestion!!.countryCode, currentSuggestion!!.publicationCode, currentSuggestion!!.issueNumber)
            if (existingIssue == null) {
                val newIssue = Issue(currentSuggestion!!.issueNumber, null)
                WhatTheDuck.selectedCountry = currentSuggestion!!.countryCode
                WhatTheDuck.selectedPublication = currentSuggestion!!.publicationCode
                WhatTheDuck.selectedIssue = newIssue.issueNumber

                GetPurchaseList.initAndShowAddIssue(this@CoverFlowActivity)
            } else {
                Toast.makeText(
                        this@CoverFlowActivity,
                        R.string.issue_already_possessed,
                        Toast.LENGTH_SHORT)
                        .show()
            }
        }

        mCoverFlow.setOnScrollPositionListener(object : FeatureCoverFlow.OnScrollPositionListener {
            override fun onScrolledToPosition(position: Int) {
                currentSuggestion = mData[position]
                currentCoverUrl = currentSuggestion!!.fullUrl

                val uri = "@drawable/flags_" + currentSuggestion!!.countryCode
                var imageResource = resources.getIdentifier(uri, null, packageName)

                if (imageResource == 0) {
                    imageResource = R.drawable.flags_unknown
                }
                mCountryBadge!!.visibility = View.VISIBLE
                mCountryBadge!!.setImageResource(imageResource)

                mIssueConditionText!!.visibility = View.VISIBLE

                val existingIssue = WhatTheDuck.userCollection.getIssue(currentSuggestion!!.countryCode, currentSuggestion!!.publicationCode, currentSuggestion!!.issueNumber)
                if (existingIssue != null) {
                    val condition = existingIssue.issueCondition
                    mIssueCondition!!.visibility = View.VISIBLE
                    mIssueCondition!!.setImageResource(Issue.issueConditionToResourceId(condition))
                    mIssueConditionText!!.text = resources.getString(Issue.issueConditionToStringId(condition))
                    mIssueConditionText!!.textSize = 18f
                } else {
                    mIssueCondition!!.visibility = View.GONE
                    mIssueConditionText!!.setText(R.string.add_cover)
                    mIssueConditionText!!.textSize = 14f
                }

                mResultNumber!!.visibility = View.VISIBLE
                mResultNumber!!.setText(resources.getString(R.string.result) + " " + (position + 1) + "/" + mData.size)

                mTitleText!!.visibility = View.VISIBLE
                mTitleText!!.text = mData[position].publicationTitle + " " + mData[position].issueNumber
            }

            override fun onScrolling() {
                mResultNumber!!.visibility = View.INVISIBLE
                mTitleText!!.visibility = View.INVISIBLE
                mCountryBadge!!.visibility = View.INVISIBLE
                mIssueCondition!!.visibility = View.INVISIBLE
                mIssueConditionText!!.visibility = View.INVISIBLE
            }
        })
    }

    companion object {

        private var currentSuggestion: IssueWithFullUrl? = null
        var currentCoverUrl: String? = null
    }
}
