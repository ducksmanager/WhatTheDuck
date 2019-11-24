package net.ducksmanager.util

import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import net.ducksmanager.whattheduck.*
import net.ducksmanager.whattheduck.Settings

abstract class AppCompatActivityWithDrawer : AppCompatActivity() {

    companion object {
        private val menuActions: HashMap<Int, Class<*>> = hashMapOf(
            R.id.action_collection to CountryList::class.java,
            R.id.action_settings to Settings::class.java,
            R.id.action_suggestions to Suggestions::class.java,
            R.id.action_logout to Login::class.java
        )
    }

    private val drawerLayout: DrawerLayout?
        get() {
            return findViewById(R.id.drawerLayout)
        }

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

        drawerNavigation.setCheckedItem(menuActions.filterValues { it == this.javaClass }.keys.first())

        drawerNavigation
            .setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId == R.id.action_logout) {
                    WhatTheDuckApplication.appDB.userDao().deleteAll()
                }

                val target = menuActions[menuItem.itemId]
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