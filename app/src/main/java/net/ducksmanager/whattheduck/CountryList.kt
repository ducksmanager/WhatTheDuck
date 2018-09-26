package net.ducksmanager.whattheduck

import android.app.AlertDialog
import android.os.Bundle

import net.ducksmanager.inducks.coa.CountryListing
import net.ducksmanager.util.ReleaseNotes
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.Collection.CollectionType

import java.lang.ref.WeakReference

class CountryList : ItemList<CountryAdapter.Country>() {

    protected override val itemAdapter: ItemAdapter<CountryAdapter.Country>
        get() = CountryAdapter(this, collection.countryList)

    override fun needsToDownloadFullList(): Boolean {
        return ItemList.type == CollectionType.COA.toString() && !CountryListing.hasFullList
    }

    override fun downloadFullList() {
        CountryListing(this
        ) { e, result -> this@CountryList.notifyCompleteList() }.fetch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_WELCOME)) {
            val builder = AlertDialog.Builder(this@CountryList)
            builder.setTitle(getString(R.string.welcomeTitle))
            builder.setMessage(getString(R.string.welcomeMessage))
            builder.setPositiveButton(R.string.ok) { dialogInterface, which ->
                ReleaseNotes.current.showOnVersionUpdate(WeakReference<Activity>(this@CountryList))
                dialogInterface.dismiss()
            }
            Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_WELCOME)
            Settings.saveSettings()
            builder.create().show()
        } else {
            ReleaseNotes.current.showOnVersionUpdate(WeakReference<Activity>(this))
        }


        WhatTheDuck.selectedCountry = null
        WhatTheDuck.selectedPublication = null
        show()
    }

    override fun userHasItemsInCollectionForCurrent(): Boolean {
        return true
    }

    override fun shouldShow(): Boolean {
        return true
    }

    override fun shouldShowNavigation(): Boolean {
        return true
    }

    override fun shouldShowToolbar(): Boolean {
        return true
    }

    override fun shouldShowAddToCollectionButton(): Boolean {
        return true
    }

    override fun shouldShowFilter(countries: List<CountryAdapter.Country>?): Boolean {
        return countries!!.size > ItemList.MIN_ITEM_NUMBER_FOR_FILTER
    }

    override fun hasDividers(): Boolean {
        return true
    }

    override fun onBackPressed() {
        if (ItemList.type == CollectionType.COA.toString()) {
            onBackFromAddIssueActivity()
        }
    }
}