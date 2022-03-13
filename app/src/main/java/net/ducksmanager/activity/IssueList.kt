package net.ducksmanager.activity

import android.app.AlertDialog
import android.content.*
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import net.ducksmanager.adapter.IssueAdapter
import net.ducksmanager.adapter.IssueCoverAdapter
import net.ducksmanager.adapter.IssueEdgeAdapter
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.coa.InducksIssueWithCoverUrl
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData
import net.ducksmanager.persistence.models.composite.UserSetting
import net.ducksmanager.persistence.models.dm.IssuePopularity
import net.ducksmanager.persistence.models.edge.Edge
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.R.string.*
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.issueToScrollTo
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedCountry
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedIssues
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedPublication
import net.ducksmanager.whattheduck.databinding.WtdListBinding
import retrofit2.Response
import java.lang.ref.WeakReference


class IssueList : ItemList<InducksIssueWithUserData>() {

    override lateinit var itemAdapter: ItemAdapter<InducksIssueWithUserData>

    companion object {
        var zoomLevel = 0
    }

    fun getPublicationCode(): String {
        return selectedPublication!!.publicationCode
    }

    override val AndroidViewModel.data: LiveData<List<InducksIssueWithUserData>>
        get() = appDB!!.inducksIssueDao().findByPublicationCode(getPublicationCode())

    override fun downloadAndShowList() {
        DmServer.api.getIssues(getPublicationCode()).enqueue(object : DmServer.Callback<List<InducksIssueWithCoverUrl>>("getInducksIssues", this, false) {
            override fun onFailureFailover() {
                viewModel.data.observe(this@IssueList) { existingInducksIssues ->
                    if (existingInducksIssues.isEmpty()) {
                        // Create fake Inducks issues in the local DB corresponding to the user's issues
                        appDB!!.issueDao().findByPublicationCode(getPublicationCode())
                            .observe(this@IssueList) { userIssues ->
                                appDB!!.inducksIssueDao().insertList(userIssues.map { issue ->
                                    InducksIssueWithCoverUrl(
                                        getPublicationCode(),
                                        issue.issueNumber,
                                        "",
                                        ""
                                    )
                                })
                                super@IssueList.downloadAndShowList()
                            }
                    } else {
                        super@IssueList.downloadAndShowList()
                    }
                }
            }

            override fun onSuccessfulResponse(response: Response<List<InducksIssueWithCoverUrl>>) {
                appDB!!.inducksIssueDao().deleteByPublicationCode(getPublicationCode())
                appDB!!.inducksIssueDao().insertList(response.body()!!.map { issue ->
                    InducksIssueWithCoverUrl(getPublicationCode(), issue.inducksIssueNumber, issue.title, issue.coverUrl)
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

    override fun onObserve(): (t: List<InducksIssueWithUserData>) -> Unit {
        binding.zoomWrapper.visibility = if (isOfflineMode || isCoaList()) GONE else VISIBLE
        return super.onObserve()
    }

    private fun updateAdapter() {
        for (i in 0 until binding.itemList.itemDecorationCount) {
            binding.itemList.removeItemDecorationAt(i)
        }
        itemAdapter = when (if (isCoaList()) { 0 } else { zoomLevel }) {
            0 -> {
                binding.itemList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                IssueAdapter(this)
            }
            1 -> {
                val listOrientation = getListOrientation()

                if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW)
                    && listOrientation == RecyclerView.VERTICAL) {
                    WhatTheDuck.info(WeakReference(this), welcome_bookcase_view_portrait, Toast.LENGTH_LONG)
                    Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_WELCOME_BOOKCASE_VIEW)
                }
                binding.itemList.layoutManager = LinearLayoutManager(this, getListOrientation(), false)
                IssueEdgeAdapter(this, binding.itemList, resources.configuration.orientation)
            }
            else -> {
                val spanCount = 5 - zoomLevel
                val spacing = 20
                binding.itemList.setPadding(spacing, spacing, spacing, spacing)
                binding.itemList.addItemDecoration(object : ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State
                    ) {
                        outRect.set(spacing, spacing, spacing, spacing)
                    }
                })
                binding.itemList.layoutManager = GridLayoutManager(this, spanCount)
                IssueCoverAdapter(this, binding.itemList)
            }
        }
    }

    override fun isFilterableList(): Boolean {
        return zoomLevel == 0
    }

    override fun show() {
        super.show()
        val adapter = binding.itemList.adapter
        if (!isCoaList() && adapter is IssueEdgeAdapter) {
            DmServer.api.getEdgeList(getPublicationCode())
                .enqueue(object : DmServer.Callback<List<Edge>>("getEdges", this, false) {
                    override fun onSuccessfulResponse(response: Response<List<Edge>>) {
                        IssueEdgeAdapter.existingEdges = response.body()!!
                        DmServer.api.getIssuePopularities().enqueue(object : DmServer.Callback<List<IssuePopularity>>("getIssuePopularities", this@IssueList, false) {
                            override fun onSuccessfulResponse(response: Response<List<IssuePopularity>>) {
                                adapter.issuePopularities = response.body()!!
                            }
                        })
                    }
                })
        }
        updateAdapter()
        binding.itemList.adapter = itemAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = WtdListBinding.inflate(layoutInflater)
        updateAdapter()
        super.onCreate(savedInstanceState)
        setNavigationCountry(selectedCountry!!)
        setNavigationPublication(selectedPublication!!)

        selectedIssues = mutableListOf()

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

        binding.viewSeekBar.progress = zoomLevel

        binding.viewSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seek: SeekBar,
                progress: Int, fromUser: Boolean
            ) {
                zoomLevel = progress
                if (progress > 0) {
                    if (Settings.shouldShowMessage(Settings.MESSAGE_KEY_DATA_CONSUMPTION) && WhatTheDuck.isMobileConnection) {
                        val builder = AlertDialog.Builder(this@IssueList)
                        builder.setTitle(getString(bookcase_view_title))
                        builder.setMessage(getString(bookcase_view_message))
                        builder.setNegativeButton(cancel) { dialogInterface: DialogInterface, _: Int ->
                            binding.viewSeekBar.progress = 0
                            dialogInterface.dismiss()
                        }
                        builder.setPositiveButton(ok) { dialogInterface: DialogInterface, _: Int ->
                            Settings.addToMessagesAlreadyShown(Settings.MESSAGE_KEY_DATA_CONSUMPTION)
                            dialogInterface.dismiss()
                            switchBetweenViews()
                        }
                        builder.create().show()
                    }
                }
                switchBetweenViews()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun getListOrientation() =
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL

    override fun shouldShowItemSelectionTip(): Boolean = isCoaList() && ! appDB!!.userSettingDao().findByKey(UserSetting.SETTING_KEY_ISSUE_SELECTION_TIP_ENABLED)?.value.equals("0")

    override fun shouldShowSelectionValidation(): Boolean = isCoaList() && !isOfflineMode

    override fun hasDividers() = zoomLevel == 0

    override fun shouldShow() = selectedCountry != null && selectedPublication != null

    override fun shouldShowNavigationCountry() = true

    override fun shouldShowNavigationPublication() = true

    override fun shouldShowAddToCollectionButton() = !isCoaList() && !isOfflineMode

    override fun shouldShowZoom() = !isCoaList() && !isOfflineMode

    private fun switchBetweenViews() {
        WhatTheDuck.trackEvent("issuelist/switchview")
        binding.suggestionMessage.visibility = GONE
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

    override fun scrollToSavedPosition() {
        if (issueToScrollTo != null) {
            binding.itemList.post {
                binding.itemList.scrollToPosition(itemAdapter.items.indexOfFirst { it.issue.inducksIssueNumber == issueToScrollTo })
                issueToScrollTo = null
            }
        }
    }
}