package net.ducksmanager.whattheduck

import android.content.Intent
import android.os.Bundle

import net.ducksmanager.inducks.coa.PublicationListing

class PublicationList : ItemList<PublicationAdapter.Publication>() {

    protected override val itemAdapter: ItemAdapter<PublicationAdapter.Publication>
        get() = PublicationAdapter(this, collection.getPublicationList(WhatTheDuck.selectedCountry))

    override fun needsToDownloadFullList(): Boolean {
        return !PublicationListing.hasFullList(WhatTheDuck.selectedCountry)
    }

    override fun downloadFullList() {
        PublicationListing(this, WhatTheDuck.selectedCountry
        ) { e, result -> this@PublicationList.notifyCompleteList() }.fetch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WhatTheDuck.selectedPublication = null
        show()
    }

    override fun userHasItemsInCollectionForCurrent(): Boolean {
        return WhatTheDuck.userCollection.hasCountry(WhatTheDuck.selectedCountry)
    }

    override fun shouldShow(): Boolean {
        return WhatTheDuck.selectedCountry != null
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

    override fun shouldShowFilter(publications: List<PublicationAdapter.Publication>?): Boolean {
        return publications!!.size > ItemList.MIN_ITEM_NUMBER_FOR_FILTER
    }

    override fun hasDividers(): Boolean {
        return true
    }

    override fun onBackPressed() {
        if (ItemList.type == Collection.CollectionType.COA.toString()) {
            onBackFromAddIssueActivity()
        } else {
            startActivity(Intent(WhatTheDuck.wtd, CountryList::class.java))
        }
    }
}
