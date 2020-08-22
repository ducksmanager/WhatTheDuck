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
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.currentUser

abstract class AppCompatActivityWithDrawer : AppCompatActivity() {

    companion object {
        private val menuActions: HashMap<Int, List<Class<*>>> = hashMapOf(
            R.id.action_collection to listOf(CountryList::class.java, PublicationList::class.java, IssueList::class.java),
            R.id.action_settings to listOf(Settings::class.java),
            R.id.action_suggestions to listOf(Suggestions::class.java)
        )
    }

    protected var isOfflineMode = false

    private val drawerLayout: DrawerLayout?
        get() = findViewById(R.id.drawerLayout)

    protected abstract fun shouldShowToolbar(): Boolean

    protected open fun showToolbarIfExists() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (shouldShowToolbar()) {
            toolbar.visibility = View.VISIBLE
            setSupportActionBar(toolbar)
            createNavigationDrawer()
        } else {
            toolbar.visibility = View.GONE
        }
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
        logoutButton.visibility = if (WhatTheDuck.isOfflineMode) View.GONE else View.VISIBLE
        logoutButton.setOnClickListener{
            WhatTheDuck.unregisterFromNotifications()
            WhatTheDuck.appDB!!.userDao().deleteAll()
            this.startActivity(Intent(this, Login::class.java))
        }

        drawerNavigation.getHeaderView(0).findViewById<TextView>(R.id.username).text = currentUser?.username

        drawerNavigation
            .setNavigationItemSelectedListener { menuItem ->
                val target = menuActions[menuItem.itemId]?.get(0)
                if (this.javaClass != target) {
                    startActivity(Intent(this, target))
                }

                drawerLayout!!.closeDrawers()

                true
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        drawerLayout!!.openDrawer(GravityCompat.START)
        return true
    }
}