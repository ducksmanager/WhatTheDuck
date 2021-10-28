package net.ducksmanager.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mig35.carousellayoutmanager.CarouselLayoutManager
import com.mig35.carousellayoutmanager.CarouselZoomPostLayoutListener
import com.mig35.carousellayoutmanager.CenterScrollListener
import com.mig35.carousellayoutmanager.DefaultChildSelectionListener
import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithDetails
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData.Companion.issueConditionToResourceId
import net.ducksmanager.util.CoverFlowAdapter
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
        val layoutManager = CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false)
        layoutManager.setPostLayoutListener(CarouselZoomPostLayoutListener())

        appDB!!.coverSearchIssueDao().findAll().observe(this, { searchIssues: List<CoverSearchIssueWithDetails> ->
            if (searchIssues.isEmpty()) {
                return@observe
            }
            data = searchIssues
            adapter = CoverFlowAdapter(this)
            adapter.setData(searchIssues)

            binding.listHorizontal.layoutManager = layoutManager
            binding.listHorizontal.setHasFixedSize(false)
            binding.listHorizontal.adapter = adapter
            binding.listHorizontal.addOnScrollListener(CenterScrollListener())
            DefaultChildSelectionListener.initCenterItemListener({ recyclerView, _, v ->
                val position = recyclerView.getChildLayoutPosition(v)
                val currentSuggestion = data[position]
                if (currentSuggestion.userIssue == null) {
                    appDB!!.inducksCountryDao().findByCountryCode(currentSuggestion.coverSearchIssue.coverCountryCode).observe(this, { country ->
                        selectedCountry = country
                        appDB!!.inducksPublicationDao().findByPublicationCode(currentSuggestion.coverSearchIssue.coverPublicationCode).observe(this, { publication ->
                            selectedPublication = publication
                            selectedIssues = mutableListOf(currentSuggestion.coverSearchIssue.coverIssueNumber)
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
            }, binding.listHorizontal, layoutManager)

            layoutManager.addOnItemSelectionListener { position ->
                if (CarouselLayoutManager.INVALID_POSITION == position) {
                    toggleInfoVisibility(View.INVISIBLE)
                }
                else {
                    val currentSuggestion = data[position]
                    val uri = "@drawable/flags_${currentSuggestion.coverSearchIssue.coverCountryCode}"
                    var countryImageResource = resources.getIdentifier(uri, null, packageName)
                    if (countryImageResource == 0) {
                        countryImageResource = R.drawable.flags_unknown
                    }

                    toggleInfoVisibility(VISIBLE)

                    binding.issue.countrybadge.setImageResource(countryImageResource)

                    val condition = currentSuggestion.userIssue?.condition
                    if (condition != null) {
                        val conditionResourceId = issueConditionToResourceId(condition)
                        if (conditionResourceId != null) {
                            binding.issue.conditionbadge.setImageResource(conditionResourceId)
                        }
                        binding.clickToAdd.visibility = INVISIBLE
                    }
                    else {
                        binding.issue.conditionbadge.visibility = GONE
                    }

                    if (currentSuggestion.suggestionScore <= 0) {
                        binding.score.root.visibility = GONE
                    }
                    binding.score.scorevalue.text = currentSuggestion.suggestionScore.toString()
                    binding.score.scorevalue.textSize = 20F

                    binding.popularity.text = String.format(getString(R.string.number_of_users_own_issue), currentSuggestion.coverSearchIssue.popularity)

                    val quotation = currentSuggestion.coverSearchIssue.quotation
                    if (quotation != null) {
                        binding.quotation.visibility = VISIBLE
                        if (quotation.containsKey("min") && quotation.containsKey("max")) {
                            binding.quotationvalue.text = String.format(
                                resources.getString(R.string.quotation_between),
                                quotation["min"],
                                quotation["max"]
                            )
                        } else if (quotation.containsKey("min")) {
                            binding.quotationvalue.text = String.format(
                                resources.getString(R.string.quotation_more_than),
                                quotation["min"]
                            )
                        } else {
                            binding.quotationvalue.text = String.format(
                                resources.getString(R.string.quotation_less_than),
                                quotation["max"]
                            )
                        }
                    } else {
                        binding.quotation.visibility = GONE
                    }

                    binding.resultNumber.text = resources.getString(
                        R.string.coversearch_result_template,
                        resources.getString(R.string.result),
                        position + 1,
                        data.size
                    )

                    binding.issue.issuetitle.text = resources.getString(
                        R.string.title_template,
                        data[position].coverSearchIssue.coverPublicationTitle,
                        data[position].coverSearchIssue.coverIssueNumber
                    )
                }
            }
        })
    }

    private fun toggleInfoVisibility(visibility: Int) {
        listOf(
            binding.issue.countrybadge,
            binding.issue.issuetitle,
            binding.clickToAdd,
            binding.score.root,
            binding.quotation,
            binding.resultNumber
        ).forEach{ it.visibility = visibility }
    }
}