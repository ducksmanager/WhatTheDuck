package net.ducksmanager.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import net.ducksmanager.adapter.CountryAdapter
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.api.DmServer
import net.ducksmanager.api.DmServer.Companion.EVENT_RETRIEVE_ALL_COUNTRIES
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.applicationVersion
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedFilter
import retrofit2.Response


class CountryList : ItemList<InducksCountryNameWithPossession>() {

    override var itemAdapter: ItemAdapter<InducksCountryNameWithPossession> = CountryAdapter(this)

    override val AndroidViewModel.data: LiveData<List<InducksCountryNameWithPossession>>
        get() = when(selectedFilter) {
            getString(R.string.filter_to_read) -> appDB!!.inducksCountryDao().findAllToReadWithPossession()
            else -> appDB!!.inducksCountryDao().findAllWithPossession()
        }

    override fun downloadAndShowList() {
        if (!isOfflineMode && Login.isObsoleteSync(appDB!!.syncDao().findLatest(applicationVersion))) {
            DmServer.api.getCountries(WhatTheDuck.locale).enqueue(object : DmServer.Callback<HashMap<String, String>>(EVENT_RETRIEVE_ALL_COUNTRIES, this, true) {
                override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                    appDB!!.inducksCountryDao().deleteAll()
                    appDB!!.inducksCountryDao().insertList( response.body()!!.keys.map { countryCode ->
                        InducksCountryName(countryCode, response.body()!![countryCode]!!)
                    })
                    super@CountryList.downloadAndShowList()
                }
            })
        }
        else {
            super@CountryList.downloadAndShowList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_WELCOME)) {
            val builder = AlertDialog.Builder(this@CountryList)
            builder.setTitle(getString(R.string.welcome_title))
            builder.setMessage(getString(R.string.welcome_message))
            builder.setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_WELCOME)
            builder.create().show()
        }
        WhatTheDuck.selectedCountry = null
        WhatTheDuck.selectedPublication = null
    }

    override fun isPossessedByUser() = true

    override fun shouldShow() = true

    override fun shouldShowNavigationCountry() = false

    override fun shouldShowNavigationPublication() =  false

    override fun shouldShowAddToCollectionButton() = !isCoaList() && !isOfflineMode

    override fun hasDividers() = true

    override fun onBackPressed() {
        if (isCoaList()) {
            onBackFromAddIssueActivity()
        }
    }

    override fun shouldShowItemSelectionTip() = false

    override fun shouldShowSelectionValidation() = false

    override fun shouldShowZoom() = false

    override fun isFilterableList() = true
}