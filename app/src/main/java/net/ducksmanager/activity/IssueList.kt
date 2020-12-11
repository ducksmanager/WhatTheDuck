package net.ducksmanager.activity

import android.app.AlertDialog
import android.content.*
import android.content.res.Configuration
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.adapter.IssueAdapter
import net.ducksmanager.adapter.IssueEdgeAdapter
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.coa.InducksIssue
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.UserSetting
import net.ducksmanager.util.DraggableRelativeLayout
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.R.string.*
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedCountry
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedIssues
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedPublication
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.*


class IssueList : ItemList<InducksIssueWithUserIssueAndScore>() {

    override lateinit var itemAdapter: ItemAdapter<InducksIssueWithUserIssueAndScore>

    companion object {
        var viewType = ViewType.LIST_VIEW
    }

    fun getPublicationCode(): String {
        return selectedPublication!!.publicationCode
    }

    override val AndroidViewModel.data: LiveData<List<InducksIssueWithUserIssueAndScore>>
        get() = appDB!!.inducksIssueDao().findByPublicationCode(getPublicationCode())

    override fun downloadAndShowList() {
        DmServer.api.getIssues(getPublicationCode()).enqueue(object : DmServer.Callback<HashMap<String, String>>("getInducksIssues", this, false) {
            override fun onFailureFailover() {
                viewModel.data.observe(this@IssueList, { existingInducksIssues ->
                    if (existingInducksIssues.isEmpty()) {
                        // Create fake Inducks issues in the local DB corresponding to the user's issues
                        appDB!!.issueDao().findByPublicationCode(getPublicationCode()).observe(this@IssueList, { userIssues ->
                            appDB!!.inducksIssueDao().insertList(userIssues.map { issue ->
                                InducksIssue(getPublicationCode(), issue.issueNumber, "")
                            })
                            super@IssueList.downloadAndShowList()
                        })
                    } else {
                        super@IssueList.downloadAndShowList()
                    }
                })
            }

            override fun onSuccessfulResponse(response: Response<HashMap<String, String>>) {
                appDB!!.inducksIssueDao().deleteByPublicationCode(getPublicationCode())
                appDB!!.inducksIssueDao().insertList(response.body()!!.map { (issueNumber, title) ->
                    InducksIssue(getPublicationCode(), issueNumber, title)
                })
                super@IssueList.downloadAndShowList()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val retValue = super.onCreateOptionsMenu(menu)
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            menuInflater.inflate(R.menu.menu_issue_list, menu)

        }
        return retValue
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (setOf(getString(issue_list_copy_missing), getString(issue_list_copy_possessed)).contains(item.title)) {
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                lateinit var title: String
                lateinit var contents: String
                when (item.title) {
                    getString(issue_list_copy_possessed) -> {
                        title = getString(issue_list_copy_possessed_title) + selectedPublication!!.title
                        contents = getListAsText(true)
                    }
                    getString(issue_list_copy_missing) -> {
                        title = getString(issue_list_copy_missing_title) + selectedPublication!!.title
                        contents = getListAsText(false)
                    }
                }
                clipboard.setPrimaryClip(ClipData.newPlainText(title, "$title : $contents"))
                WhatTheDuck.info(WeakReference(this), issue_list_copied, 1000)
            }
        }
        else {
            super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getListAsText(possessed: Boolean): String {
        val filteredItems = itemAdapter.items.filter { possessed == (it.userIssue != null) }.map { it.issue.inducksIssueNumber }
        if (filteredItems.isEmpty()) {
            return getString(no_issue)
        }
        return filteredItems.toString()
    }

    override fun onObserve(): (t: List<InducksIssueWithUserIssueAndScore>) -> Unit {
        binding.switchViewWrapper.visibility = if (isOfflineMode || isCoaList()) GONE else VISIBLE
        return super.onObserve()
    }

    private fun updateAdapter() {
        itemAdapter = if (viewType == ViewType.EDGE_VIEW) {
            val deviceOrientation = resources.configuration.orientation
            IssueEdgeAdapter(this, recyclerView, deviceOrientation)
        } else {
            IssueAdapter(this)
        }
    }

    enum class ViewType {
        LIST_VIEW, EDGE_VIEW
    }

    override fun show() {
        binding.switchView.isChecked = viewType == ViewType.EDGE_VIEW
        if (isCoaList()) {
            viewType = ViewType.LIST_VIEW
        }
        updateAdapter()
        binding.itemList.adapter = itemAdapter
        super.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        updateAdapter()
        super.onCreate(savedInstanceState)
        setNavigationCountry(selectedCountry!!)
        setNavigationPublication(selectedPublication!!)

        selectedIssues = mutableSetOf()
        DraggableRelativeLayout.makeDraggable(binding.switchViewWrapper)

        binding.tipIssueSelectionOK.setOnClickListener {
            appDB!!.userSettingDao().insert(UserSetting(UserSetting.SETTING_KEY_ISSUE_SELECTION_TIP_ENABLED, "0"))
            binding.tipIssueSelection.visibility = GONE
        }
        binding.cancelSelection.setOnClickListener {
            selectedIssues.clear()
            itemAdapter.notifyDataSetChanged()
        }
        binding.validateSelection.setOnClickListener {
            if (selectedIssues.isEmpty()) {
                WhatTheDuck.info(WeakReference(this), input_error__no_issue_selected, Toast.LENGTH_SHORT)
            } else {
                this.startActivity(Intent(this, AddIssues::class.java))
            }
        }

        val switchView = binding.switchView
        switchView.setOnClickListener {
            if (switchView.isChecked) {
                if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_DATA_CONSUMPTION) && WhatTheDuck.isMobileConnection) {
                    val builder = AlertDialog.Builder(this@IssueList)
                    builder.setTitle(getString(bookcase_view_title))
                    builder.setMessage(getString(bookcase_view_message))
                    builder.setNegativeButton(cancel) { dialogInterface: DialogInterface, _: Int ->
                        switchView.toggle()
                        dialogInterface.dismiss()
                    }
                    builder.setPositiveButton(ok) { dialogInterface: DialogInterface, _: Int ->
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

        if (viewType == ViewType.EDGE_VIEW) {
            val deviceOrientation = resources.configuration.orientation
            val listOrientation = if (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL

            if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW)
                && listOrientation == RecyclerView.VERTICAL) {
                WhatTheDuck.info(WeakReference(this), welcome_bookcase_view_portrait, Toast.LENGTH_LONG)
                Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW)
            }
            recyclerView.layoutManager = LinearLayoutManager(this, listOrientation, false)
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        }
    }

    override fun shouldShowItemSelectionTip(): Boolean = isCoaList() && ! appDB!!.userSettingDao().findByKey(UserSetting.SETTING_KEY_ISSUE_SELECTION_TIP_ENABLED)?.value.equals("0")

    override fun shouldShowSelectionValidation(): Boolean = isCoaList() && !isOfflineMode

    override fun hasDividers() = viewType != ViewType.EDGE_VIEW

    override fun shouldShow() = selectedCountry != null && selectedPublication != null

    override fun shouldShowNavigationCountry() = !isLandscapeEdgeView

    override fun shouldShowNavigationPublication() = !isLandscapeEdgeView

    override fun shouldShowToolbar() = !isLandscapeEdgeView

    override fun shouldShowAddToCollectionButton() = !isCoaList() && !isOfflineMode && !isLandscapeEdgeView

    private val recyclerView: RecyclerView
        get() {
            return findViewById(R.id.itemList)
        }

    private val isLandscapeEdgeView: Boolean
        get() = viewType == ViewType.EDGE_VIEW && resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE

    private fun switchBetweenViews() {
        WhatTheDuck.trackEvent("issuelist/switchview")
        viewType = if (findViewById<SwitchCompat>(R.id.switchView).isChecked)
            ViewType.EDGE_VIEW
        else
            ViewType.LIST_VIEW

        updateAdapter()
        binding.itemList.adapter = itemAdapter
        loadList()
    }

    override fun isPossessedByUser() = data.any { it.userIssue != null }

    override fun onBackPressed() {
        if (isCoaList()) {
            onBackFromAddIssueActivity()
        } else {
            startActivity(Intent(this, PublicationList::class.java))
        }
    }
}