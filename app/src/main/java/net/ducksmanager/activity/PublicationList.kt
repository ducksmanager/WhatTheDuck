package net.ducksmanager.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.adapter.PublicationAdapter
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import retrofit2.Response
import java.util.*

class PublicationList : ItemList<InducksPublicationWithPossession>() {

    override val itemAdapter: ItemAdapter<InducksPublicationWithPossession>
        get() = PublicationAdapter(this, data)

    override fun getList() = appDB!!.inducksPublicationDao().findByCountry(WhatTheDuck.selectedCountry!!)

    override fun downloadList(currentActivity: Activity) {
        DmServer.api.getPublications(WhatTheDuck.selectedCountry!!).enqueue(object : DmServer.Callback<HashMap<String, String>>("getInducksPublications", currentActivity) {
            override val isFailureAllowed = true

            override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                val publications: List<InducksPublication> = response.body()!!.keys.map { publicationCode ->
                    InducksPublication(publicationCode, response.body()!![publicationCode]!!)
                }
                appDB!!.inducksPublicationDao().insertList(publications)
                setData()
            }

            override fun onFailureFailover() {
                isOfflineMode = true
                findViewById<LinearLayout>(R.id.action_logout).visibility = View.GONE
                setData()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WhatTheDuck.selectedPublication = null
        show()
    }

    override fun isPossessedByUser() = data.any { it.isPossessed }

    override fun shouldShow() = WhatTheDuck.selectedCountry != null

    override fun shouldShowNavigationCountry() = true

    override fun shouldShowNavigationPublication() = false

    override fun shouldShowToolbar() = true

    override fun shouldShowAddToCollectionButton() = !isOfflineMode

    override fun shouldShowFilter(items: List<InducksPublicationWithPossession>) = items.size > MIN_ITEM_NUMBER_FOR_FILTER

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