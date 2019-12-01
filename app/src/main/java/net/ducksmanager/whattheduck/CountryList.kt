package net.ducksmanager.whattheduck

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.lifecycle.Observer
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession
import net.ducksmanager.util.ReleaseNotes
import net.ducksmanager.util.Settings
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.*

class CountryList : ItemList<InducksCountryNameWithPossession>() {

    override fun downloadList(currentActivity: Activity) {
        downloadList(currentActivity, Runnable { setData() })
    }

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
        show()
    }

    override val isPossessedByUser: Boolean
        get() = true

    override fun hasList() = hasFullList

    override fun shouldShow() = true

    override fun shouldShowNavigationCountry() = false

    override fun shouldShowNavigationPublication() =  false

    override fun shouldShowToolbar() = true

    override fun shouldShowAddToCollectionButton() = true

    override fun shouldShowFilter(items: List<InducksCountryNameWithPossession>) = items.size > MIN_ITEM_NUMBER_FOR_FILTER

    override fun hasDividers() = true

    override val itemAdapter: ItemAdapter<InducksCountryNameWithPossession>
        get() = CountryAdapter(this, data)

    override fun setData() {
        WhatTheDuck.appDB.inducksCountryDao().findAllWithPossession().observe(this@CountryList, Observer { items: List<InducksCountryNameWithPossession>? -> storeItemList(items!!) })
    }

    override fun onBackPressed() {
        if (type == WhatTheDuck.CollectionType.COA.toString()) {
            onBackFromAddIssueActivity()
        }
    }

    companion object {
        private const val DUMMY_COUNTRY_CODE = "zz"
        @JvmField
        var hasFullList = false
        fun downloadList(currentActivity: Activity, hasDataCallback: Runnable) {
            DmServer.api.getCountries(WhatTheDuck.locale).enqueue(object : DmServer.Callback<HashMap<String, String>>("getInducksCountries", currentActivity) {
                override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                    val countries: MutableList<InducksCountryName> = ArrayList()
                    for (countryCode in response.body()!!.keys) {
                        if (countryCode != DUMMY_COUNTRY_CODE) {
                            countries.add(InducksCountryName(countryCode, response.body()!![countryCode]!!))
                        }
                    }
                    WhatTheDuck.appDB.inducksCountryDao().deleteAll()
                    WhatTheDuck.appDB.inducksCountryDao().insertList(countries)
                    hasFullList = true
                    hasDataCallback.run()
                }
            })
        }
    }
}