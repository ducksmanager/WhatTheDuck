package net.ducksmanager.activity

import android.os.Bundle
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.github.aachartmodel.aainfographics.aaoptionsmodel.AAScrollablePlotArea
import com.github.aachartmodel.aainfographics.aatools.AAColor
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData.Companion.issueConditionToStringId
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.databinding.StatsBinding
import java.lang.Integer.max
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class Stats : AppCompatActivityWithDrawer() {
    private lateinit var binding: StatsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = StatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggleToolbar()

        appDB!!.issueDao().countDistinct().observe(this, { collectionCount ->
            binding.countryCount.text = "%d".format(collectionCount.countries)
            binding.publicationCount.text = "%d".format(collectionCount.publications)
            binding.issueCount.text = "%d".format(collectionCount.issues)
            if (collectionCount.countries <= 1) {
                binding.countryLabel.text = getString(R.string.countries_singular)
            }
            if (collectionCount.publications <= 1) {
                binding.publicationLabel.text = getString(R.string.publications_singular)
            }
            if (collectionCount.issues <= 1) {
                binding.issueLabel.text = getString(R.string.issues_singular)
            }

            if (collectionCount.issues.equals(0)) {
                return@observe
            }

            appDB!!.issueDao().countPerCondition().observe(this, { issuesPerCondition ->
                val colors = issuesPerCondition.map {
                    when (it.condition) {
                        InducksIssueWithUserData.BAD_CONDITION -> AAColor.Red
                        InducksIssueWithUserData.NOTSOGOOD_CONDITION -> AAColor.Orange
                        InducksIssueWithUserData.GOOD_CONDITION -> AAColor.Green
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

            appDB!!.issueDao().countPerMonthAndPublication()
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
                    val mostOwnedPublicationNames = appDB!!.inducksPublicationDao().findByPublicationCodes(mostOwnedPublications)
                    val allPublications = hashSetOf(getString(R.string.other)).plus(mostOwnedPublicationNames.map { it.title })

                    val series = allPublications.associateWith { mutableMapOf<Int, Any>() }.toMutableMap()
                    val oldestMonth = issuesPerMonthAndPublication.find { it.month != "-0001-1" }?.month
                    val hasUnknownDate = issuesPerMonthAndPublication.any { it.month == "-0001-1"}

                    val allMonths = if (hasUnknownDate) { mutableListOf(getString(R.string.unknown_date)) } else { mutableListOf() }
                    if (oldestMonth != null) {
                        var currentDate = LocalDate.parse("$oldestMonth-01", DateTimeFormatter.ISO_DATE)
                        do {
                            allMonths.add(currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                            currentDate = currentDate.plusMonths(1)
                        } while (currentDate <= LocalDate.now())
                    }

                    val allMonthsArray = allMonths.toTypedArray()
                    val cumulatedSumPerPublication = allPublications.associateWith { 0 }.toMutableMap()

                    issuesPerMonthAndPublication.forEach {
                        val monthIndex = max(allMonthsArray.indexOf(it.month), 0)
                        val publicationcode = it.publicationcode
                        val isMostOwnedPublication = mostOwnedPublications.contains(publicationcode)
                        val targetPublicationName = if (isMostOwnedPublication) {
                            mostOwnedPublicationNames.find { publication -> publication.publicationCode == publicationcode }!!.title
                        } else {
                            getString(R.string.other)
                        }
                        cumulatedSumPerPublication.computeIfPresent(targetPublicationName) { _, v -> v + it.count }
                        for (month in monthIndex until allMonthsArray.size) {
                            series[targetPublicationName]?.set(
                                month,
                                cumulatedSumPerPublication[targetPublicationName]!!
                            )
                        }
                    }

                    val monthsArrayLastYearOnly = allMonthsArray.slice(max(0, allMonthsArray.size - 12) until allMonthsArray.size).toTypedArray()
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
                                series.map { (publicationname, data) ->
                                    var seriesElement = AASeriesElement().name(publicationname).data(data.values.toTypedArray())
                                    if (publicationname == getString(R.string.other)) {
                                        seriesElement = seriesElement.color("#999")
                                    }
                                    seriesElement
                                }.toTypedArray()
                            )
                        if (scrollable && monthsArray.size > 12) {
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

                    binding.showPurchaseHistorySinceForever.setOnCheckedChangeListener { buttonView, isChecked ->
                        buttonView.isChecked = isChecked
                        binding.showPurchaseHistoryInThePastYear.isChecked = !isChecked
                        binding.purchaseProgressChart.aa_drawChartWithChartOptions(getChartModelOptions(series, allMonthsArray))
                    }

                    binding.showPurchaseHistoryInThePastYear.setOnCheckedChangeListener { buttonView, isChecked ->
                        buttonView.isChecked = isChecked
                        binding.showPurchaseHistorySinceForever.isChecked = !isChecked
                        binding.purchaseProgressChart.aa_drawChartWithChartOptions(getChartModelOptions(seriesLastYearOnly, monthsArrayLastYearOnly, false))

                    }
                })
        })
    }
}
