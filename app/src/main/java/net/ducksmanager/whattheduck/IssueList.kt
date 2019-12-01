package net.ducksmanager.whattheduck

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.Switch
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.coa.InducksIssue
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueDetails
import net.ducksmanager.util.DraggableRelativeLayout
import net.ducksmanager.util.Settings
import retrofit2.Response
import java.lang.ref.WeakReference

class IssueList : ItemList<InducksIssueWithUserIssueDetails>() {

    companion object {
        @JvmField
        var viewType = ViewType.LIST_VIEW
    }

    enum class ViewType {
        LIST_VIEW, EDGE_VIEW
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        show()
    }

    override fun hasList(): Boolean {
        return false // FIXME
    }

    override fun downloadList(currentActivity: Activity) {
        DmServer.api.getIssues(WhatTheDuck.selectedPublication!!).enqueue(object : DmServer.Callback<List<String>>("getInducksIssues", currentActivity) {
            override fun onSuccessfulResponse(response: Response<List<String>>) {
                val issues: List<InducksIssue> = response.body()!!.map { issueNumber ->
                    InducksIssue(WhatTheDuck.selectedPublication!!, issueNumber)
                }
                WhatTheDuck.appDB.inducksIssueDao().insertList(issues)
                setData()
            }
        })
    }

    override fun hasDividers() = viewType != ViewType.EDGE_VIEW

    override fun shouldShow() =
        WhatTheDuck.selectedCountry != null && WhatTheDuck.selectedPublication != null

    override fun shouldShowNavigationCountry() = !isLandscapeEdgeView

    override fun shouldShowNavigationPublication() = !isLandscapeEdgeView

    override fun shouldShowToolbar() = !isLandscapeEdgeView

    override fun shouldShowAddToCollectionButton() = !isLandscapeEdgeView

    override fun shouldShowFilter(items: List<InducksIssueWithUserIssueDetails>) =
        items.size > MIN_ITEM_NUMBER_FOR_FILTER && viewType == ViewType.LIST_VIEW

    override val itemAdapter: ItemAdapter<InducksIssueWithUserIssueDetails>
        get() {
            val switchViewWrapper = findViewById<RelativeLayout>(R.id.switchViewWrapper)

            DraggableRelativeLayout.makeDraggable(switchViewWrapper)
            if (type == WhatTheDuck.CollectionType.COA.toString()) {
                viewType = ViewType.LIST_VIEW
                switchViewWrapper.visibility = View.GONE
            } else {
                val switchView = switchViewWrapper.findViewById<Switch>(R.id.switchView)
                switchViewWrapper.visibility = View.VISIBLE
                switchView.isChecked = viewType == ViewType.EDGE_VIEW

                switchView.setOnClickListener {
                    if (switchView.isChecked) {
                        if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_DATA_CONSUMPTION) && WhatTheDuck.isMobileConnection) {
                            val builder = AlertDialog.Builder(this@IssueList)
                            builder.setTitle(getString(R.string.bookcaseViewTitle))
                            builder.setMessage(getString(R.string.bookcaseViewMessage))
                            builder.setNegativeButton(R.string.cancel) { dialogInterface: DialogInterface, _: Int ->
                                switchView.toggle()
                                dialogInterface.dismiss()
                            }
                            builder.setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
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

            val recyclerView = findViewById<RecyclerView>(R.id.itemList)
            return if (viewType == ViewType.EDGE_VIEW) {
                val deviceOrientation = resources.configuration.orientation
                val listOrientation = if (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL

                if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW)
                    && listOrientation == RecyclerView.VERTICAL) {
                    WhatTheDuck.info(WeakReference(this), R.string.welcomeBookcaseViewPortrait, Toast.LENGTH_LONG)
                    Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW)
                }
                recyclerView.layoutManager = LinearLayoutManager(this, listOrientation, false)
                IssueEdgeAdapter(this, data, recyclerView, deviceOrientation)
            } else {
                recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                IssueAdapter(this, data)
            }
        }

    private val isLandscapeEdgeView: Boolean
        get() {
            val deviceOrientation = resources.configuration.orientation
            return viewType == ViewType.EDGE_VIEW && deviceOrientation == Configuration.ORIENTATION_LANDSCAPE
        }

    private fun switchBetweenViews() {
        WhatTheDuck.trackEvent("issuelist/switchview")
        viewType = if (findViewById<Switch>(R.id.switchView).isChecked)
            ViewType.EDGE_VIEW
        else
            ViewType.LIST_VIEW

        loadList()
        show()
    }

    override val isPossessedByUser: Boolean
        get() {
            return data.any { it.userIssue != null }
        }

    override fun setData() {
        WhatTheDuck.appDB.inducksIssueDao().findByPublicationCode(WhatTheDuck.selectedPublication!!)
            .observe(this, Observer { items: List<InducksIssueWithUserIssueDetails> ->
                storeItemList(items)
            })
    }

    override fun onBackPressed() {
        if (type == WhatTheDuck.CollectionType.COA.toString()) {
            onBackFromAddIssueActivity()
        } else {
            startActivity(Intent(this, PublicationList::class.java))
        }
    }
}