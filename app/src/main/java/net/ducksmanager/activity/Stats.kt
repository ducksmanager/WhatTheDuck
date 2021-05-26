package net.ducksmanager.activity

import android.os.Bundle
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAScrollablePlotArea
import com.github.aachartmodel.aainfographics.aatools.AAColor
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore.Companion.issueConditionToStringId
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.databinding.StatsBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


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
            if (collectionCount.countries <= 1) {
                binding.countryLabel.text = getString(R.string.countries_singular)
            }
            if (collectionCount.publications <= 1) {
                binding.countryLabel.text = getString(R.string.publications_singular)
            }
            if (collectionCount.issues <= 1) {
                binding.countryLabel.text = getString(R.string.issues_singular)
            }

            if (collectionCount.issues.equals(0)) {
                return@observe
            }

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
                    .axesTextColor("white")
                    .colorsTheme(colors.toTypedArray())
                    .legendEnabled(false)
                    .series(
                        arrayOf(
                            AASeriesElement()
                                .color(AAColor.White)
                                .colorByPoint(true)
                                .name(getString(R.string.in_this_condition))
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

            WhatTheDuck.appDB!!.issueDao().countPerMonthAndPublication()
                .observe(this, { issuesPerMonthAndPublication ->

                    val countPerPublication =
                        issuesPerMonthAndPublication
                            .groupingBy { it.publicationcode }
                            .aggregate { _, acc: Int?, element, first ->
                                if (first)
                                    element.count
                                else
                                    acc!! + element.count
                            }

                    val mostOwnedPublications = countPerPublication.filter { it.value >= (collectionCount.issues.toFloat() / 10) }.keys
                    val allPublications = hashSetOf(getString(R.string.other)).plus(mostOwnedPublications)

                    val series = allPublications.associateWith { mutableMapOf<Int, Any>() }.toMutableMap()
                    val oldestMonth = issuesPerMonthAndPublication.find { it.month != "-0001-1" }?.month

                    val allMonths = mutableListOf(getString(R.string.unknown_date))
                    if (oldestMonth != null) {
                        var currentDate = LocalDate.parse("$oldestMonth-01", DateTimeFormatter.ISO_DATE)
                        do {
                            allMonths.add(currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                            currentDate = currentDate.plusMonths(1)
                        } while (currentDate <= LocalDate.now())
                    }

                    val allMonthsArray = allMonths.toTypedArray()
                    val cumulatedSumPerPublication = allPublications.associateWith { 0 }.toMutableMap()
                    val cumulatedSumPerMonth = allMonths.associateWith { 0 }.toMutableMap()

                    issuesPerMonthAndPublication.forEach {
                        val monthIndex = allMonthsArray.indexOf(it.month)
                        val isMostOwnedPublication = mostOwnedPublications.contains(it.publicationcode)
                        val targetPublication = if (isMostOwnedPublication) {
                            it.publicationcode
                        } else getString(R.string.other)
                        cumulatedSumPerPublication.computeIfPresent(targetPublication) { _, v -> v + it.count }
                        for (month in monthIndex until allMonthsArray.size - 1) {
                            series[targetPublication]?.set(
                                month,
                                cumulatedSumPerPublication[targetPublication]!!
                            )
                            cumulatedSumPerMonth.computeIfPresent(it.month) { _, v -> v + it.count }
                        }
                    }

                    val monthsArrayLastYearOnly = allMonthsArray.slice(allMonthsArray.size - 12 until allMonthsArray.size).toTypedArray()
                    val seriesLastYearOnly = series.mapValues { it.value.filterKeys { monthIndex: Int -> monthIndex != -1 && monthsArrayLastYearOnly.contains(allMonthsArray[monthIndex]) } }

                    fun getChartModelOptions(
                        series: Map<String, Map<Int, Any>>,
                        monthsArray: Array<String>,
                        scrollable: Boolean = true
                    ): AAOptions {
                        val model = AAChartModel()
                            .chartType(AAChartType.Area)
                            .legendEnabled(false)
                            .axesTextColor("white")
                            .backgroundColor(R.color.cardview_dark_background)
                            .stacking(AAChartStackingType.Normal)
                            .markerRadius(0.0f)
                            .yAxisTitle(getString(R.string.collection_size))
                            .categories(monthsArray)
                            .series(
                                series.map { (publicationcode, data) ->
                                    AASeriesElement()
                                        .name(publicationcode).data(data.values.toTypedArray())
                                }.toTypedArray()
                            )
                        if (scrollable) {
                            model.scrollablePlotArea(
                                AAScrollablePlotArea()
                                    .minWidth(3000)
                                    .scrollPositionX(1f)
                            )
                        }
                        val aaOptions = model.aa_toAAOptions()
                        aaOptions.tooltip
                            ?.shared(false)
                            ?.formatter("""
                                function () {
                                    return '<b>' + this.x + '</b><br/>'
                                    + this.series.name+ ': '+ this.y + '<br/>'
                                    + 'Total: ' + this.point.stackTotal;
                                }""".trimIndent()
                            )
                        return aaOptions
                    }

                    val purchaseProgressModel = getChartModelOptions(series, allMonthsArray)

                    binding.purchaseProgressChart.aa_drawChartWithChartOptions(purchaseProgressModel)

                    binding.showPurchaseHistorySinceForever.setOnClickListener {
                        binding.purchaseProgressChart.aa_drawChartWithChartOptions(getChartModelOptions(series, allMonthsArray))
                    }

                    binding.showPurchaseHistoryInThePastYear.setOnClickListener {
                        binding.purchaseProgressChart.aa_drawChartWithChartOptions(getChartModelOptions(seriesLastYearOnly, monthsArrayLastYearOnly, false))
                    }
                })
        })
    }
}
