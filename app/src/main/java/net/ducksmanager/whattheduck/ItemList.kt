package net.ducksmanager.whattheduck


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu

import net.ducksmanager.inducks.coa.CountryListing
import net.ducksmanager.inducks.coa.PublicationListing
import net.ducksmanager.retrievetasks.CoverSearch
import net.ducksmanager.util.CoverFlowFileHandler
import net.ducksmanager.util.Settings
import net.ducksmanager.whattheduck.Collection.CollectionType

import java.io.File
import java.lang.ref.WeakReference

import android.view.View.GONE

abstract class ItemList<Item> : AppCompatActivity() {

    private var requiresDataDownload: Boolean? = false

    protected abstract val itemAdapter: ItemAdapter<Item>

    internal val collection: Collection
        get() = if (type == null || type == CollectionType.USER.toString())
            WhatTheDuck.userCollection
        else
            WhatTheDuck.coaCollection

    protected abstract fun needsToDownloadFullList(): Boolean
    protected abstract fun downloadFullList()
    protected abstract fun hasDividers(): Boolean

    protected abstract fun userHasItemsInCollectionForCurrent(): Boolean

    protected abstract fun shouldShow(): Boolean
    protected abstract fun shouldShowNavigation(): Boolean
    protected abstract fun shouldShowToolbar(): Boolean
    protected abstract fun shouldShowAddToCollectionButton(): Boolean
    protected abstract fun shouldShowFilter(items: List<Item>?): Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.wtd_list)

        val navigationAllCountries = this.findViewById<View>(R.id.navigationAllCountries)
        navigationAllCountries?.setOnClickListener { view -> goToView(CountryList::class.java) }

        val navigationCurrentCountry = this.findViewById<View>(R.id.navigationCountry)
        navigationCurrentCountry?.findViewById<View>(R.id.selected)?.setOnClickListener { view -> goToView(PublicationList::class.java) }

        loadList()
    }

    internal fun loadList() {
        (application as WhatTheDuckApplication).trackActivity(this)

        if (needsToDownloadFullList()) {
            this.requiresDataDownload = true
            downloadFullList()
        }

        val actionBar = supportActionBar
        if (actionBar != null) {
            if (type == CollectionType.USER.toString()) {
                actionBar.setDisplayHomeAsUpEnabled(false)
            } else {
                actionBar.setDisplayHomeAsUpEnabled(true)
                (findViewById<View>(R.id.toolbar) as Toolbar).setNavigationOnClickListener { v -> onBackFromAddIssueActivity() }
            }
        }

        title = if (type == CollectionType.USER.toString())
            getString(R.string.my_collection)
        else
            getString(R.string.add_issue)
    }

    private fun goToView(cls: Class<*>) {
        if (this@ItemList.javaClass != cls) {
            startActivity(Intent(WhatTheDuck.wtd, cls))
        }
    }

    internal fun goToAlternativeView(collectionType: String) {
        type = collectionType
        loadList()
        show()
    }

    internal fun show() {
        if (!shouldShow()) {
            return
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (shouldShowToolbar()) {
            toolbar.visibility = View.VISIBLE
            setSupportActionBar(toolbar)
        } else {
            toolbar.visibility = GONE
        }

        if (shouldShowNavigation()) {
            setNavigation(WhatTheDuck.selectedCountry, WhatTheDuck.selectedPublication)
        } else {
            hideNavigation()
        }

        val addToCollection = this.findViewById<FloatingActionMenu>(R.id.addToCollectionWrapper)
        if (shouldShowAddToCollectionButton()) {
            if (addToCollection != null) {
                addToCollection.setMenuButtonColorNormalResId(R.color.holo_green_dark)
                addToCollection.setMenuButtonColorPressedResId(R.color.holo_green_dark)
                addToCollection.visibility = if (type == CollectionType.USER.toString()) View.VISIBLE else GONE
                addToCollection.close(false)

                if (type == CollectionType.USER.toString()) {
                    val addToCollectionByPhotoButton = this.findViewById<FloatingActionButton>(R.id.addToCollectionByPhotoButton)
                    addToCollectionByPhotoButton.setOnClickListener { view -> takeCoverPicture() }

                    val addToCollectionBySelectionButton = this.findViewById<FloatingActionButton>(R.id.addToCollectionBySelectionButton)
                    addToCollectionBySelectionButton.setOnClickListener { view ->
                        addToCollection.visibility = GONE
                        this@ItemList.goToAlternativeView(CollectionType.COA.toString())
                    }

                    Settings.saveSettings()
                }
            }
        } else {
            addToCollection!!.visibility = GONE
        }

        val itemAdapter = itemAdapter
        if (this.requiresDataDownload!!) {
            itemAdapter.resetItems()
        }

        val items = itemAdapter.items
        val emptyListText = this.findViewById<TextView>(R.id.emptyList)
        if (emptyListText != null) {
            emptyListText.visibility = if (this.requiresDataDownload!! || items!!.size > 0) TextView.INVISIBLE else TextView.VISIBLE
        }

        val recyclerView = findViewById<RecyclerView>(R.id.itemList)
        recyclerView.adapter = itemAdapter

        val filterEditText = this.findViewById<EditText>(R.id.filter)
        if (shouldShowFilter(items)) {
            itemAdapter.addOrReplaceFilterOnChangeListener(filterEditText)
        } else {
            filterEditText.visibility = GONE
        }

        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }
        if (hasDividers()) {
            val dividerDecoration = DividerItemDecoration(
                    recyclerView.context,
                    LinearLayoutManager(this).orientation
            )
            recyclerView.addItemDecoration(dividerDecoration)
        }
    }

    private fun takeCoverPicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            CoverFlowFileHandler.current = CoverFlowFileHandler()
            val photoURI = CoverFlowFileHandler.current!!.createEmptyFileForCamera(this@ItemList)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            this.findViewById<View>(R.id.addToCollectionWrapper).visibility = GONE
            this.findViewById<View>(R.id.progressBar).visibility = View.VISIBLE

            CoverFlowFileHandler.current!!.resizeUntilFileSize(this, object : CoverFlowFileHandler.TransformationCallback {
                override fun onComplete(fileToUpload: File?) {
                    CoverSearch(WeakReference<Activity>(this@ItemList), fileToUpload).execute()
                }

                override fun onFail() {
                    this@ItemList.findViewById<View>(R.id.addToCollectionWrapper).visibility = View.VISIBLE
                    this@ItemList.findViewById<View>(R.id.progressBar).visibility = GONE
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_user, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var i: Intent? = null

        when (item.itemId) {
            R.id.action_logout -> {
                WhatTheDuck.userCollection = Collection()
                WhatTheDuck.coaCollection = Collection()
                Settings.username = null
                Settings.setPassword(null)
                Settings.saveSettings()
                i = Intent(WhatTheDuck.wtd, WhatTheDuck::class.java)
            }
            R.id.action_about -> WhatTheDuck.showAbout(this)
        }
        if (i == null) {
            return super.onOptionsItemSelected(item)
        } else {
            startActivity(i)
            return true
        }
    }

    private fun hideNavigation() {
        this.findViewById<View>(R.id.navigation).visibility = GONE
    }

    private fun setNavigation(selectedCountry: String?, selectedPublication: String?) {
        val generalNavigationView = this.findViewById<View>(R.id.navigation)
        val countryNavigationView = this.findViewById<View>(R.id.navigationCountry)
        val publicationNavigationView = this.findViewById<View>(R.id.navigationPublication)

        if (generalNavigationView != null) {
            generalNavigationView.visibility = if (selectedCountry == null) GONE else View.VISIBLE
            publicationNavigationView.visibility = if (selectedPublication == null) View.INVISIBLE else View.VISIBLE

            if (selectedCountry != null) {
                val countryFullName = CountryListing.getCountryFullName(selectedCountry)

                val uri = "@drawable/flags_$selectedCountry"
                var imageResource = resources.getIdentifier(uri, null, packageName)

                if (imageResource == 0) {
                    imageResource = R.drawable.flags_unknown
                }

                val currentCountryFlag = countryNavigationView.findViewById<ImageView>(R.id.selectedBadgeImage)
                currentCountryFlag.setImageResource(imageResource)

                val currentCountryText = countryNavigationView.findViewById<TextView>(R.id.selectedText)
                currentCountryText.text = countryFullName
            }

            if (selectedPublication != null) {
                val publicationFullName = PublicationListing.getPublicationFullName(selectedCountry, selectedPublication)

                val currentPublicationBadgeText = publicationNavigationView.findViewById<TextView>(R.id.selectedBadge)
                currentPublicationBadgeText.setText(selectedPublication.split("/".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1])

                val currentPublicationText = publicationNavigationView.findViewById<TextView>(R.id.selectedText)
                currentPublicationText.text = publicationFullName
            }
        }
    }

    internal fun notifyCompleteList() {
        this.requiresDataDownload = false
        this.show()
    }

    internal fun onBackFromAddIssueActivity() {
        if (userHasItemsInCollectionForCurrent()) {
            goToAlternativeView(CollectionType.USER.toString())
        } else {
            type = CollectionType.USER.toString()
            startActivity(Intent(WhatTheDuck.wtd, CountryList::class.java))
        }
    }

    companion object {
        var type: String? = CollectionType.USER.toString()
        protected val MIN_ITEM_NUMBER_FOR_FILTER = 20
        private val REQUEST_IMAGE_CAPTURE = 1
    }
}
