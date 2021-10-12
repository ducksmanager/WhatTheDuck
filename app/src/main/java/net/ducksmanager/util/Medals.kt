package net.ducksmanager.util

import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import net.ducksmanager.persistence.models.dm.ContributionTotalPoints
import net.ducksmanager.whattheduck.R

interface Medals {
    companion object {
        val CONTRIBUTION_MEDAL_IDS = mapOf(
            "edge_photographer" to R.id.medal_edge_photographer,
            "edge_designer" to R.id.medal_edge_designer,
            "duckhunter" to R.id.medal_duckhunter,
        )

        val MEDAL_LEVELS = mapOf(
            R.id.medal_edge_photographer to mapOf(1 to 50, 2 to 150, 3 to 600),
            R.id.medal_edge_designer to mapOf(1 to 20, 2 to 70, 3 to 150),
            R.id.medal_duckhunter to mapOf(1 to 1, 2 to 3, 3 to 15)
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
        drawable.setImageResource(
            resources.getIdentifier(
                "medal_${contribution.contribution}_${medalLevel}_${resources.configuration.locales[0].language}",
                "drawable",
                packageName
            )
        )
    }

    fun getCurrentMedalLevel(contribution: ContributionTotalPoints) : Int {
        var currentMedalLevel = 0
        val medalImageId = CONTRIBUTION_MEDAL_IDS[contribution.contribution]!!
        for ((medalLevel, medalMinimumPoints) in MEDAL_LEVELS[medalImageId]!!) {
            if (contribution.totalPoints >= medalMinimumPoints) {
                currentMedalLevel = medalLevel
            }
        }
        return currentMedalLevel
    }
}