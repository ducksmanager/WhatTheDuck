package net.ducksmanager.activity

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.adapter.PublicationAdapter
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode

class PublicationList : ItemList<InducksPublicationWithPossession>() {

    override var itemAdapter: ItemAdapter<InducksPublicationWithPossession> = PublicationAdapter(this)

    override val AndroidViewModel.data: LiveData<List<InducksPublicationWithPossession>>
        get() = appDB!!.inducksPublicationDao().findByCountry(WhatTheDuck.selectedCountry!!.countryCode)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WhatTheDuck.selectedPublication = null
        setNavigationCountry(WhatTheDuck.selectedCountry!!)
    }

    override fun isPossessedByUser() = data.any { it.isPossessed }

    override fun shouldShow() = WhatTheDuck.selectedCountry != null

    override fun shouldShowNavigationCountry() = true

    override fun shouldShowNavigationPublication() = false

    override fun shouldShowToolbar() = true

    override fun shouldShowAddToCollectionButton() = !isCoaList() && !isOfflineMode

    override fun hasDividers() = true

    override fun onBackPressed() {
        if (isCoaList()) {
            onBackFromAddIssueActivity()
        } else {
            startActivity(Intent(this, CountryList::class.java))
        }
    }

    override fun shouldShowItemSelectionTip() = false

    override fun shouldShowSelectionValidation() = false
}