package net.ducksmanager.util

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow.OnScrollPositionListener
import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToResourceId
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToStringId
import net.ducksmanager.activity.AddIssue
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

        WhatTheDuck.appDB.coverSearchIssueDao().findAll().observe(this, Observer { searchIssues: List<CoverSearchIssueWithUserIssueAndScore> ->
            data = searchIssues
            adapter = CoverFlowAdapter(this)
            adapter.setData(searchIssues)

            binding.coverflow.adapter = adapter
            binding.coverflow.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
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
                        binding.conditiontext.setText(getString(issueConditionToStringId(condition)))
                        (binding.conditiontext.currentView as TextView).textSize = 18f
                    } else {
                        binding.conditionbadge.visibility = View.GONE
                        binding.conditiontext.setText(getString(R.string.add_cover))
                        (binding.conditiontext.currentView as TextView).textSize = 14f
                    }

                    if (currentSuggestion!!.suggestionScore <= 0) {
                        binding.score.root.visibility = View.GONE
                    }
                    binding.score.scorevalue.text = currentSuggestion!!.suggestionScore.toString()
                    binding.score.scorevalue.textSize = 20F

                    binding.resultNumber.setText(resources.getString(R.string.result) + " " + (position + 1) + "/" + data.size)

                    binding.countrytitle.setText(data[position].coverSearchIssue.coverPublicationTitle + " " + data[position].coverSearchIssue.coverIssueNumber)
                }

                override fun onScrolling() {
                    toggleInfoVisibility(View.INVISIBLE)
                }

                fun toggleInfoVisibility(visibility: Int) {
                    listOf(
                        binding.countrybadge,
                        binding.countrytitle,
                        binding.conditionbadge,
                        binding.conditiontext,
                        binding.score.root,
                        binding.resultNumber
                    ).forEach{ it.visibility = visibility }
                }
            })
        })

        binding.resultNumber.setFactory {
            LayoutInflater.from(this@CoverFlowActivity).inflate(R.layout.item_title, null)
        }
        binding.countrybadge.setFactory { ImageView(applicationContext) }

        binding.countrytitle.setFactory {
            LayoutInflater.from(this@CoverFlowActivity).inflate(R.layout.item_title, null) as TextView
        }

        binding.conditionbadge.setFactory { ImageView(applicationContext) }

        binding.conditiontext.setFactory {
            LayoutInflater.from(this@CoverFlowActivity).inflate(R.layout.item_title, null) as TextView
        }
    }
}