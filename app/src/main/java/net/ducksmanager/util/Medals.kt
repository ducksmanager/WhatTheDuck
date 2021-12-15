package net.ducksmanager.util

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import net.ducksmanager.persistence.models.dm.ContributionTotalPoints
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck

interface Medals {
    companion object {
        val CONTRIBUTION_MEDAL_IDS = mapOf(
            "edge_photographer" to R.id.medal_edge_photographer,
            "edge_designer" to R.id.medal_edge_designer,
            "duckhunter" to R.id.medal_duckhunter,
        )

        val MEDAL_LEVELS = mapOf(
            "edge_photographer" to mapOf(1 to 50, 2 to 150, 3 to 600),
            "edge_designer" to mapOf(1 to 20, 2 to 70, 3 to 150),
            "duckhunter" to mapOf(1 to 1, 2 to 3, 3 to 15)
        )
    }

    fun AppCompatActivity.setMedalDrawable(contribution: ContributionTotalPoints, drawable: ImageView, nextLevel: Boolean = false) {
        var medalLevel = getCurrentMedalLevel(contribution)
        if (nextLevel) {
            medalLevel++
        }
        if (medalLevel < 0 || medalLevel > 3) {
            return
        }
        val language = if (WhatTheDuck.locale.matches("/fr/".toRegex())) {
            WhatTheDuck.locale
        } else {
            "en"
        }
        drawable.setImageResource(
            resources.getIdentifier(
                "medal_${contribution.contribution}_${medalLevel}_$language",
                "drawable",
                packageName
            )
        )
    }

    fun getCurrentMedalLevel(contribution: ContributionTotalPoints) : Int {
        var currentMedalLevel = 0
        for ((medalLevel, medalMinimumPoints) in MEDAL_LEVELS[contribution.contribution]!!) {
            if (contribution.totalPoints >= medalMinimumPoints) {
                currentMedalLevel = medalLevel
            }
        }
        return currentMedalLevel
    }
}