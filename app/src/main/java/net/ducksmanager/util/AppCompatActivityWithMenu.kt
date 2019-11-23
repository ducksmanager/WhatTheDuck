package net.ducksmanager.util

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import net.ducksmanager.whattheduck.*
import net.ducksmanager.whattheduck.Settings

abstract class AppCompatActivityWithMenu : AppCompatActivity() {

    protected abstract fun shouldShowToolbar(): Boolean

    protected open fun showToolbarIfExists() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (shouldShowToolbar()) {
            toolbar.visibility = View.VISIBLE
            setSupportActionBar(toolbar)
        } else {
            toolbar.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                WhatTheDuckApplication.appDB.userDao().deleteAll()
                startActivity(Intent(this, Login::class.java))
            }
            R.id.action_settings -> startActivity(Intent(this, Settings::class.java))
            R.id.action_suggestions -> {
                if (!this.javaClass.equals(Suggestions::class.java)) {
                    startActivity(Intent(this, Suggestions::class.java))
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }
}