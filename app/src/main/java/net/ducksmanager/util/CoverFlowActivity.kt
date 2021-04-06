package net.ducksmanager.util

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow.OnScrollPositionListener
import net.ducksmanager.activity.AddIssues
import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithDetails
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToResourceId
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToStringId
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedCountry
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedIssues
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedPublication
import net.ducksmanager.whattheduck.databinding.ActivityCoverflowBinding

class CoverFlowActivity : AppCompatActivity() {
    private lateinit var data: List<CoverSearchIssueWithDetails>
    private lateinit var binding: ActivityCoverflowBinding

    private lateinit var adapter: CoverFlowAdapter

    companion object {
        var currentSuggestion: CoverSearchIssueWithDetails? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WhatTheDuck).trackActivity(this)
        binding = ActivityCoverflowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appDB!!.coverSearchIssueDao().findAll().observe(this, { searchIssues: List<CoverSearchIssueWithDetails> ->
            if (searchIssues.isEmpty()) {
                return@observe
            }
            data = searchIssues
            adapter = CoverFlowAdapter(this)
            adapter.setData(searchIssues)

            binding.coverflow.visibility = VISIBLE
            binding.coverflow.adapter = adapter
            binding.coverflow.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long ->
                if (currentSuggestion!!.userIssue == null) {
                    appDB!!.inducksCountryDao().findByCountryCode(currentSuggestion!!.coverSearchIssue.coverCountryCode).observe(this, { country ->
                        selectedCountry = country
                        appDB!!.inducksPublicationDao().findByPublicationCode(currentSuggestion!!.coverSearchIssue.coverPublicationCode).observe(this, { publication ->
                            selectedPublication = publication
                            selectedIssues = mutableListOf(currentSuggestion!!.coverSearchIssue.coverIssueNumber)
                            this@CoverFlowActivity.startActivity(Intent(this@CoverFlowActivity, AddIssues::class.java))
                        })
                    })
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

                    toggleInfoVisibility(VISIBLE)

                    binding.countrybadge.setImageResource(imageResource)

                    val condition = currentSuggestion!!.userIssue?.condition
                    if (condition != null) {
                        val conditionResourceId = issueConditionToResourceId(condition)
                        if (conditionResourceId != null) {
                            binding.conditionbadge.setImageResource(conditionResourceId)
                            binding.conditiontext.text = getString(issueConditionToStringId(condition))
                        }
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

                    val quotation = currentSuggestion!!.coverSearchIssue.quotation
                    if (quotation != null) {
                        binding.quotation.visibility = VISIBLE
                        if (quotation.containsKey("min") && quotation.containsKey("max")) {
                            binding.quotationvalue.text = String.format(resources.getString(R.string.quotation_between), quotation["min"], quotation["max"])
                        }
                        else if (quotation.containsKey("min")) {
                            binding.quotationvalue.text = String.format(resources.getString(R.string.quotation_more_than), quotation["min"])
                        }
                        else {
                            binding.quotationvalue.text = String.format(resources.getString(R.string.quotation_less_than), quotation["max"])
                        }
                    }
                    else {
                        binding.quotation.visibility = GONE
                    }

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
                        binding.quotation,
                        binding.resultNumber
                    ).forEach{ it.visibility = visibility }
                }
            })
        })
    }
}