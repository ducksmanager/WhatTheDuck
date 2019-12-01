package net.ducksmanager.whattheduck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.util.CoverFlowFileHandler
import net.ducksmanager.util.CoverFlowFileHandler.SearchFromCover
import java.lang.ref.WeakReference
import java.util.*

abstract class ItemList<Item> : AppCompatActivityWithDrawer() {

    companion object {
        @JvmField
        var type = WhatTheDuck.CollectionType.USER.toString()
        const val MIN_ITEM_NUMBER_FOR_FILTER = 20
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    private var requiresDataDownload = false

    @JvmField
    var data: List<Item> = ArrayList()

    protected abstract fun hasList(): Boolean
    protected abstract fun downloadList(currentActivity: Activity)
    protected abstract fun hasDividers(): Boolean
    protected abstract val isPossessedByUser: Boolean
    protected abstract fun setData()
    protected abstract fun shouldShow(): Boolean
    protected abstract fun shouldShowNavigationCountry(): Boolean
    protected abstract fun shouldShowNavigationPublication(): Boolean
    protected abstract fun shouldShowAddToCollectionButton(): Boolean
    protected abstract fun shouldShowFilter(items: List<Item>): Boolean

    protected abstract val itemAdapter: ItemAdapter<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.wtd_list)

        showToolbarIfExists()

        findViewById<View>(R.id.navigationAllCountries)
            ?.setOnClickListener { _: View? -> goToView(CountryList::class.java) }

        findViewById<View>(R.id.navigationCountry)
            ?.findViewById<View>(R.id.selected)?.setOnClickListener { _: View? -> goToView(PublicationList::class.java) }

        loadList()
    }

    fun loadList() {
        (application as WhatTheDuck).trackActivity(this)
        if (!hasList()) {
            requiresDataDownload = true
            downloadList(this)
        } else {
            setData()
        }
    }

    private fun goToView(cls: Class<*>) {
        if (this@ItemList.javaClass != cls) {
            startActivity(Intent(this, cls))
        }
    }

    private fun goToAlternativeView() {
        type = if (type == WhatTheDuck.CollectionType.USER.toString())
            WhatTheDuck.CollectionType.COA.toString()
        else
            WhatTheDuck.CollectionType.USER.toString()
        loadList()
        show()
    }

    fun show() {
        if (!shouldShow()) {
            return
        }
        showNavigation()
        val addToCollection = findViewById<FloatingActionMenu>(R.id.addToCollectionWrapper)
        if (shouldShowAddToCollectionButton()) {
            if (addToCollection != null) {
                addToCollection.setMenuButtonColorNormalResId(R.color.holo_green_dark)
                addToCollection.setMenuButtonColorPressedResId(R.color.holo_green_dark)
                addToCollection.visibility = if (type == WhatTheDuck.CollectionType.USER.toString()) View.VISIBLE else View.GONE
                addToCollection.close(false)

                if (type == WhatTheDuck.CollectionType.USER.toString()) {
                    findViewById<FloatingActionButton>(R.id.addToCollectionByPhotoButton)
                        .setOnClickListener { takeCoverPicture() }

                    findViewById<FloatingActionButton>(R.id.addToCollectionBySelectionButton)
                        .setOnClickListener {
                            addToCollection.visibility = View.GONE
                            goToAlternativeView()
                        }
                }
            }
        } else {
            addToCollection!!.visibility = View.GONE
        }
        val itemAdapter = itemAdapter

        if (requiresDataDownload) {
            itemAdapter.resetItems()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.itemList)
        recyclerView.adapter = itemAdapter

        val filterEditText = findViewById<EditText>(R.id.filter)
        if (shouldShowFilter(itemAdapter.items)) {
            itemAdapter.addOrReplaceFilterOnChangeListener(filterEditText)
        } else {
            filterEditText.visibility = View.GONE
            itemAdapter.updateFilteredList("")
        }
        findViewById<TextView>(R.id.emptyList)
            ?.visibility = if (requiresDataDownload || itemAdapter.itemCount > 0) View.INVISIBLE else View.VISIBLE

        while (recyclerView.itemDecorationCount > 0) {
            recyclerView.removeItemDecorationAt(0)
        }
        if (hasDividers()) {
            recyclerView.addItemDecoration(DividerItemDecoration(
                recyclerView.context,
                LinearLayoutManager(this).orientation
            ))
        }
    }

    private fun takeCoverPicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            CoverFlowFileHandler.current = CoverFlowFileHandler(WeakReference(this))

            val photoURI = CoverFlowFileHandler.current.createEmptyFileForCamera(this@ItemList)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            findViewById<View>(R.id.addToCollectionWrapper).visibility = View.VISIBLE
            findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
            CoverFlowFileHandler.current.resizeUntilFileSize(SearchFromCover())
        }
    }

    private fun showNavigation() {
        if (shouldShowNavigationCountry()) {
            WhatTheDuck.appDB.inducksCountryDao().findByCountryCode(WhatTheDuck.selectedCountry)
                .observe(this, Observer { inducksCountryName: InducksCountryName ->
                    setNavigationCountry(inducksCountryName.countryCode, inducksCountryName.countryName)
                })
        } else {
            findViewById<View>(R.id.navigationCountry).visibility = View.INVISIBLE
        }
        if (shouldShowNavigationPublication()) {
            WhatTheDuck.appDB.inducksPublicationDao().findByPublicationCode(WhatTheDuck.selectedPublication!!)
                .observe(this, Observer { inducksPublication: InducksPublication ->
                    setNavigationPublication(inducksPublication.publicationCode, inducksPublication.title)
                })
        } else {
            findViewById<View>(R.id.navigationPublication).visibility = View.INVISIBLE
        }
    }

    private fun setNavigationCountry(selectedCountry: String, countryFullName: String) {
        val countryNavigationView = findViewById<View>(R.id.navigationCountry)
        val uri = "@drawable/flags_$selectedCountry"
        var imageResource = resources.getIdentifier(uri, null, packageName)
        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown
        }

        countryNavigationView.findViewById<ImageView>(R.id.selectedBadgeImage)
            .setImageResource(imageResource)
        countryNavigationView.findViewById<TextView>(R.id.selectedText)
            .text = countryFullName
    }

    private fun setNavigationPublication(selectedPublication: String, publicationFullName: String) {
        val publicationNavigationView = findViewById<View>(R.id.navigationPublication)

        publicationNavigationView.findViewById<TextView>(R.id.selectedBadge)
            .text = selectedPublication.split("/").toTypedArray()[1]

        publicationNavigationView.findViewById<TextView>(R.id.selectedText)
            .text = publicationFullName
    }

    fun storeItemList(items: List<Item>) {
        data = items
        requiresDataDownload = false
        show()
    }

    fun onBackFromAddIssueActivity() {
        if (isPossessedByUser) {
            goToAlternativeView()
        } else {
            type = WhatTheDuck.CollectionType.USER.toString()
            startActivity(Intent(this, CountryList::class.java))
        }
    }
}