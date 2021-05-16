package net.ducksmanager.activity

import android.os.Bundle
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartStackingType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAStyle
import com.github.aachartmodel.aainfographics.aatools.AAColor
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToStringId
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.databinding.StatsBinding


class Stats : AppCompatActivityWithDrawer() {
    private lateinit var binding: StatsBinding

    override fun shouldShowToolbar() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = StatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggleToolbar()

        WhatTheDuck.appDB!!.issueDao().countDistinct().observe(this, { collectionCount ->
            binding.countryCount.text = "%d".format(collectionCount.countries)
            binding.publicationCount.text = "%d".format(collectionCount.publications)
            binding.issueCount.text = "%d".format(collectionCount.issues)
        })

        WhatTheDuck.appDB!!.issueDao().countPerCondition().observe(this, { issuesPerCondition ->
            val colors = issuesPerCondition.map {
                when (it.condition) {
                    InducksIssueWithUserIssueAndScore.BAD_CONDITION -> AAColor.Red
                    InducksIssueWithUserIssueAndScore.NOTSOGOOD_CONDITION -> AAColor.Orange
                    InducksIssueWithUserIssueAndScore.GOOD_CONDITION -> AAColor.Green
                    else -> AAColor.Gray
                }
            }
            val conditionModel = AAChartModel()
                .chartType(AAChartType.Pie)
                .backgroundColor(R.color.cardview_dark_background)
                .title("Issue conditions")
                .titleStyle(AAStyle().color(AAColor.Gray))
                .colorsTheme(colors.toTypedArray())
                .legendEnabled(false)
                .series(
                    arrayOf(
                        AASeriesElement()
                            .color(AAColor.White)
                            .colorByPoint(true)
                            .data(
                                issuesPerCondition.map {
                                    arrayOf(
                                        getString(issueConditionToStringId(it.condition)),
                                        it.count
                                    )
                                }
                                    .toTypedArray()
                            )
                    )
                )
            binding.issueConditionChart.aa_drawChartWithChartModel(conditionModel)
        })

        val purchaseProgressModel = AAChartModel()
            .chartType(AAChartType.Area)
            .title("Collection progression")
            .titleStyle(AAStyle().color(AAColor.Gray))
            .legendEnabled(false)
            .backgroundColor(R.color.cardview_dark_background)
            .stacking(AAChartStackingType.Normal)
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Tokyo")
                        .data(
                            arrayOf(
                                7.0,
                                6.9,
                                9.5,
                                14.5,
                                18.2,
                                21.5,
                                25.2,
                                26.5,
                                23.3,
                                18.3,
                                13.9,
                                9.6
                            )
                        ),
                    AASeriesElement()
                        .name("NewYork")
                        .data(
                            arrayOf(
                                0.2,
                                0.8,
                                5.7,
                                11.3,
                                17.0,
                                22.0,
                                24.8,
                                24.1,
                                20.1,
                                14.1,
                                8.6,
                                2.5
                            )
                        ),
                    AASeriesElement()
                        .name("London")
                        .data(
                            arrayOf(
                                0.9,
                                0.6,
                                3.5,
                                8.4,
                                13.5,
                                17.0,
                                18.6,
                                17.9,
                                14.3,
                                9.0,
                                3.9,
                                1.0
                            )
                        ),
                    AASeriesElement()
                        .name("Berlin")
                        .data(
                            arrayOf(
                                3.9,
                                4.2,
                                5.7,
                                8.5,
                                11.9,
                                15.2,
                                17.0,
                                16.6,
                                14.2,
                                10.3,
                                6.6,
                                4.8
                            )
                        )
                )
            )

        binding.purchaseProgressChart.aa_drawChartWithChartModel(purchaseProgressModel)
    }
}
