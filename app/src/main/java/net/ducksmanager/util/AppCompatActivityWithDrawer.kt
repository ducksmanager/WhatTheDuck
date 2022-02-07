package net.ducksmanager.util

import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import net.ducksmanager.activity.*
import net.ducksmanager.activity.Settings
import net.ducksmanager.util.Medals.Companion.CONTRIBUTION_MEDAL_IDS
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.currentUser
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode

abstract class AppCompatActivityWithDrawer : AppCompatActivity(), Medals {

    companion object {
        private val menuActions: HashMap<Int, List<Class<*>>> = hashMapOf(
            R.id.action_search to listOf(Search::class.java),
            R.id.action_collection to listOf(CountryList::class.java, PublicationList::class.java, IssueList::class.java),
            R.id.action_favorite_authors to listOf(Authors::class.java),
            R.id.action_stats to listOf(Stats::class.java),
            R.id.action_settings to listOf(Settings::class.java),
            R.id.action_suggestions to listOf(Suggestions::class.java)
        )
    }

    private val drawerLayout: DrawerLayout?
        get() = findViewById(R.id.drawerLayout)

    protected open fun toggleToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.visibility = View.VISIBLE
        setSupportActionBar(toolbar)
        createNavigationDrawer()
    }

    private fun createNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, findViewById(R.id.toolbar), R.string.ok, R.string.cancel)
        drawerLayout!!.addDrawerListener(toggle)
        toggle.syncState()

        val drawerNavigation = findViewById<NavigationView>(R.id.drawerNavigation)

        val keys = menuActions.filterValues { it.contains(this.javaClass) }.keys
        if (keys.isNotEmpty()) {
            drawerNavigation.setCheckedItem(keys.first())
        }

        val logoutButton = drawerNavigation.findViewById<LinearLayout>(R.id.action_logout)
        logoutButton.visibility = if (isOfflineMode) View.GONE else View.VISIBLE
        logoutButton.setOnClickListener{
            WhatTheDuck.unregisterFromNotifications()
            appDB!!.userDao().deleteAll()
            this.startActivity(Intent(this, Login::class.java))
        }

        val reportButton = drawerNavigation.findViewById<LinearLayout>(R.id.action_report)
        reportButton.visibility = if (isOfflineMode) View.GONE else View.VISIBLE
        reportButton.setOnClickListener {
            this.startActivity(Intent(this, Report::class.java))
        }

        val drawerContents = drawerNavigation.getHeaderView(0)
        drawerContents.findViewById<TextView>(R.id.username).text = currentUser?.username

        drawerNavigation
            .setNavigationItemSelectedListener { menuItem ->
                ItemList.type = WhatTheDuck.CollectionType.USER.toString()
                startActivity(Intent(this, menuActions[menuItem.itemId]?.get(0)))

                drawerLayout!!.closeDrawers()

                true
            }

        appDB!!.contributionTotalPointsDao().contributions.observe(this) { contributions ->
            contributions.forEach {
                val medalImageId = CONTRIBUTION_MEDAL_IDS[it.contribution]!!
                setMedalDrawable(it, drawerContents.findViewById(medalImageId))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        drawerLayout!!.openDrawer(GravityCompat.START)
        return true
    }
}