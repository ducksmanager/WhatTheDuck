package net.ducksmanager.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import net.ducksmanager.adapter.CountryAdapter
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession
import net.ducksmanager.util.ReleaseNotes
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import java.lang.ref.WeakReference

class CountryList : ItemList<InducksCountryNameWithPossession>() {

    override var itemAdapter: ItemAdapter<InducksCountryNameWithPossession> = CountryAdapter(this)

    override val AndroidViewModel.data: LiveData<List<InducksCountryNameWithPossession>>
        get() = WhatTheDuck.appDB!!.inducksCountryDao().findAllWithPossession()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_WELCOME)) {
            val builder = AlertDialog.Builder(this@CountryList)
            builder.setTitle(getString(R.string.welcomeTitle))
            builder.setMessage(getString(R.string.welcomeMessage))
            builder.setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                ReleaseNotes.current.showOnVersionUpdate(WeakReference(this@CountryList))
                dialogInterface.dismiss()
            }
            Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_WELCOME)
            builder.create().show()
        } else {
            ReleaseNotes.current.showOnVersionUpdate(WeakReference(this))
        }
        WhatTheDuck.selectedCountry = null
        WhatTheDuck.selectedPublication = null
    }

    override fun isPossessedByUser() = true

    override fun shouldShow() = true

    override fun shouldShowNavigationCountry() = false

    override fun shouldShowNavigationPublication() =  false

    override fun shouldShowToolbar() = true

    override fun shouldShowAddToCollectionButton() = !isOfflineMode

    override fun shouldShowFilter(items: List<InducksCountryNameWithPossession>) = items.size > MIN_ITEM_NUMBER_FOR_FILTER

    override fun hasDividers() = true

    override fun onBackPressed() {
        if (isCoaList()) {
            onBackFromAddIssueActivity()
        }
    }

    override fun shouldShowItemSelectionTip() = false

    override fun shouldShowSelectionValidation() = false
}