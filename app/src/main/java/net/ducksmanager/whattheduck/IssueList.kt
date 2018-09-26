package net.ducksmanager.whattheduck

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.Toast

import net.ducksmanager.inducks.coa.IssueListing
import net.ducksmanager.util.DraggableRelativeLayout
import net.ducksmanager.util.Settings

import java.lang.ref.WeakReference
import java.util.ArrayList

class IssueList : ItemList<Issue>() {

    protected override val itemAdapter: ItemAdapter<Issue>
        get() {
            val switchViewWrapper = this.findViewById<RelativeLayout>(R.id.switchViewWrapper)
            DraggableRelativeLayout.makeDraggable(switchViewWrapper)

            val switchView = switchViewWrapper.findViewById<Switch>(R.id.switchView)

            if (ItemList.type == Collection.CollectionType.COA.toString()) {
                viewType = ViewType.LIST_VIEW
                switchViewWrapper.visibility = View.GONE
            } else {
                switchViewWrapper.visibility = View.VISIBLE
                switchView.isChecked = viewType == ViewType.EDGE_VIEW
                switchView.setOnClickListener { view ->
                    if (switchView.isChecked) {
                        if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_DATA_CONSUMPTION) && WhatTheDuck.isMobileConnection) {
                            val builder = AlertDialog.Builder(this@IssueList)
                            builder.setTitle(getString(R.string.bookcaseViewTitle))
                            builder.setMessage(getString(R.string.bookcaseViewMessage))
                            builder.setNegativeButton(R.string.cancel) { dialogInterface, which ->
                                switchView.toggle()
                                dialogInterface.dismiss()
                            }
                            builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
                                Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_DATA_CONSUMPTION)
                                dialogInterface.dismiss()
                                switchBetweenViews()
                            }
                            builder.create().show()
                        } else {
                            switchBetweenViews()
                        }
                    } else {
                        switchBetweenViews()
                    }
                }
            }

            val issueList = collection.getIssueList(
                    WhatTheDuck.selectedCountry,
                    WhatTheDuck.selectedPublication
            )

            val recyclerView = this.findViewById<RecyclerView>(R.id.itemList)

            if (viewType == ViewType.EDGE_VIEW) {
                val deviceOrientation = resources.configuration.orientation
                val listOrientation = if (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE)
                    LinearLayoutManager.HORIZONTAL
                else
                    LinearLayoutManager.VERTICAL

                if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW) && listOrientation == LinearLayoutManager.VERTICAL) {
                    WhatTheDuck.wtd!!.info(WeakReference<Activity>(this), R.string.welcomeBookcaseViewPortrait, Toast.LENGTH_LONG)
                    Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW)
                }

                recyclerView.layoutManager = LinearLayoutManager(this, listOrientation, false)

                return IssueEdgeAdapter(this, issueList, recyclerView, deviceOrientation)
            } else {
                recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                return IssueAdapter(this, issueList)
            }
        }

    private val isLandscapeEdgeView: Boolean
        get() {
            val deviceOrientation = resources.configuration.orientation
            return viewType == ViewType.EDGE_VIEW && deviceOrientation == Configuration.ORIENTATION_LANDSCAPE
        }

    enum class ViewType {
        LIST_VIEW,
        EDGE_VIEW
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        show()
    }

    override fun needsToDownloadFullList(): Boolean {
        return !IssueListing.hasFullList(WhatTheDuck.selectedPublication)
    }

    override fun downloadFullList() {
        IssueListing(this, WhatTheDuck.selectedCountry, WhatTheDuck.selectedPublication
        ) { e, result -> this@IssueList.notifyCompleteList() }.fetch()
    }

    override fun hasDividers(): Boolean {
        return viewType != ViewType.EDGE_VIEW
    }

    override fun shouldShow(): Boolean {
        return WhatTheDuck.selectedCountry != null && WhatTheDuck.selectedPublication != null
    }

    override fun shouldShowNavigation(): Boolean {
        return !isLandscapeEdgeView
    }

    override fun shouldShowToolbar(): Boolean {
        return !isLandscapeEdgeView
    }

    override fun shouldShowAddToCollectionButton(): Boolean {
        return !isLandscapeEdgeView
    }

    override fun shouldShowFilter(issues: List<Issue>?): Boolean {
        return issues!!.size > ItemList.MIN_ITEM_NUMBER_FOR_FILTER && viewType == ViewType.LIST_VIEW
    }

    private fun switchBetweenViews() {
        WhatTheDuck.trackEvent("issuelist/switchview")
        viewType = if ((this.findViewById<View>(R.id.switchView) as Switch).isChecked) ViewType.EDGE_VIEW else ViewType.LIST_VIEW
        loadList()
        show()
    }

    override fun userHasItemsInCollectionForCurrent(): Boolean {
        return WhatTheDuck.userCollection.hasPublication(WhatTheDuck.selectedCountry, WhatTheDuck.selectedPublication)
    }

    override fun onBackPressed() {
        if (ItemList.type == Collection.CollectionType.COA.toString()) {
            onBackFromAddIssueActivity()
        } else {
            startActivity(Intent(WhatTheDuck.wtd, PublicationList::class.java))
        }
    }

    companion object {

        var viewType = ViewType.LIST_VIEW
    }
}
