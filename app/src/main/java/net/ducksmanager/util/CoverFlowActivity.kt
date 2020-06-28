package net.ducksmanager.util

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow.OnScrollPositionListener
import net.ducksmanager.activity.AddIssues
import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToResourceId
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToStringId
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.databinding.ActivityCoverflowBinding

class CoverFlowActivity : AppCompatActivity() {
    private lateinit var data: List<CoverSearchIssueWithUserIssueAndScore>
    private lateinit var binding: ActivityCoverflowBinding

    private lateinit var adapter: CoverFlowAdapter

    companion object {
        @JvmField
        var currentSuggestion: CoverSearchIssueWithUserIssueAndScore? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuck).trackActivity(this)
        binding = ActivityCoverflowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WhatTheDuck.appDB!!.coverSearchIssueDao().findAll().observe(this, Observer { searchIssues: List<CoverSearchIssueWithUserIssueAndScore> ->
            data = searchIssues
            adapter = CoverFlowAdapter(this)
            adapter.setData(searchIssues)

            binding.coverflow.adapter = adapter
            binding.coverflow.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
                if (currentSuggestion!!.userIssue == null) {
                    WhatTheDuck.selectedCountry = currentSuggestion!!.coverSearchIssue.coverCountryCode
                    WhatTheDuck.selectedPublication = currentSuggestion!!.coverSearchIssue.coverPublicationCode
                    WhatTheDuck.selectedIssues = mutableSetOf(currentSuggestion!!.coverSearchIssue.coverIssueNumber)
                    this@CoverFlowActivity.startActivity(Intent(this@CoverFlowActivity, AddIssues::class.java))
                } else {
                    Toast.makeText(
                        this@CoverFlowActivity,
                        R.string.issue_already_possessed,
                        Toast.LENGTH_SHORT)
                        .show()
                }
            }
            binding.coverflow.setOnScrollPositionListener(object : OnScrollPositionListener {
                override fun onScrolledToPosition(position: Int) {
                    currentSuggestion = data[position]
                    val uri = "@drawable/flags_" + currentSuggestion!!.coverSearchIssue.coverCountryCode
                    var imageResource = resources.getIdentifier(uri, null, packageName)
                    if (imageResource == 0) {
                        imageResource = R.drawable.flags_unknown
                    }

                    toggleInfoVisibility(View.VISIBLE)

                    binding.countrybadge.setImageResource(imageResource)

                    val condition = currentSuggestion!!.userIssue?.condition
                    if (condition != null) {
                        binding.conditionbadge.setImageResource(issueConditionToResourceId(condition))
                        binding.conditiontext.text = getString(issueConditionToStringId(condition))
                        binding.conditiontext.textSize = 18f
                    } else {
                        binding.conditionbadge.visibility = View.GONE
                        binding.conditiontext.text = getString(R.string.add_cover)
                        binding.conditiontext.textSize = 14f
                    }

                    if (currentSuggestion!!.suggestionScore <= 0) {
                        binding.score.root.visibility = View.GONE
                    }
                    binding.score.scorevalue.text = currentSuggestion!!.suggestionScore.toString()
                    binding.score.scorevalue.textSize = 20F

                    binding.resultNumber.text = resources.getString(
                        R.string.coversearch_result_template,
                        resources.getString(R.string.result),
                        position + 1,
                        data.size
                    )

                    binding.issuetitle.text = resources.getString(
                        R.string.title_template,
                        data[position].coverSearchIssue.coverPublicationTitle,
                        data[position].coverSearchIssue.coverIssueNumber
                    )
                }

                override fun onScrolling() {
                    toggleInfoVisibility(View.INVISIBLE)
                }

                fun toggleInfoVisibility(visibility: Int) {
                    listOf(
                        binding.countrybadge,
                        binding.issuetitle,
                        binding.conditionbadge,
                        binding.conditiontext,
                        binding.score.root,
                        binding.resultNumber
                    ).forEach{ it.visibility = visibility }
                }
            })
        })
    }
}