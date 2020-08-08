package net.ducksmanager.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.wtd_list_navigation_country.view.*
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.util.CoverFlowFileHandler
import net.ducksmanager.util.CoverFlowFileHandler.SearchFromCover
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.databinding.WtdListBinding
import java.lang.ref.WeakReference
import java.util.*

abstract class ItemList<Item> : AppCompatActivityWithDrawer() {

    companion object {
        @JvmField
        var type = WhatTheDuck.CollectionType.USER.toString()
        const val MIN_ITEM_NUMBER_FOR_FILTER = 20
        private const val REQUEST_IMAGE_CAPTURE = 1

        fun isCoaList() : Boolean {
            return type == WhatTheDuck.CollectionType.COA.toString()
        }
    }

    private var requiresDataDownload = false

    @JvmField
    var data: List<Item> = ArrayList()
    private lateinit var binding: WtdListBinding

    protected abstract fun getList(): LiveData<List<Item>>
    protected abstract fun downloadList(currentActivity: Activity)
    protected abstract fun hasDividers(): Boolean
    protected abstract fun isPossessedByUser(): Boolean
    protected abstract fun shouldShow(): Boolean
    protected abstract fun shouldShowNavigationCountry(): Boolean
    protected abstract fun shouldShowNavigationPublication(): Boolean
    protected abstract fun shouldShowAddToCollectionButton(): Boolean
    protected abstract fun shouldShowFilter(items: List<Item>): Boolean
    protected abstract fun shouldShowItemSelectionTip(): Boolean
    protected abstract fun shouldShowSelectionValidation(): Boolean

    protected abstract val itemAdapter: ItemAdapter<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =  WtdListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showToolbarIfExists()

        binding.navigationAllCountries.root.setOnClickListener { goToView(CountryList::class.java) }

        binding.navigationCountry.root.selected?.setOnClickListener { _: View? -> goToView(PublicationList::class.java) }

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
        type = if (isCoaList())
            WhatTheDuck.CollectionType.USER.toString()
        else
            WhatTheDuck.CollectionType.COA.toString()
        loadList()
        show()
    }

    private fun hasList(): Boolean {
        val list = getList().value
        return list !== null && list.isNotEmpty()
    }

    protected fun setData() {
        getList().observe(this, Observer { items ->
            data = items
            requiresDataDownload = false
            show()
        })
    }

    fun show() {
        if (!shouldShow()) {
            return
        }
        showNavigation()
        val addToCollection = binding.addToCollectionWrapper
        if (shouldShowAddToCollectionButton()) {
            addToCollection.visibility = if (isCoaList()) GONE else VISIBLE
            binding.addToCollectionByPhotoButton.visibility = GONE
            binding.addToCollectionBySelectionButton.visibility = GONE

            addToCollection.setOnClickListener {
                binding.addToCollectionByPhotoButton.visibility = if (binding.addToCollectionByPhotoButton.visibility == GONE) VISIBLE else GONE
                binding.addToCollectionBySelectionButton.visibility = if (binding.addToCollectionBySelectionButton.visibility == GONE) VISIBLE else GONE
            }

            if (!isCoaList()) {
                binding.addToCollectionByPhotoButton
                    .setOnClickListener { takeCoverPicture() }

                binding.addToCollectionBySelectionButton
                    .setOnClickListener {
                        addToCollection.visibility = GONE
                        goToAlternativeView()
                    }
            }
        } else {
            addToCollection.visibility = GONE
        }
        val itemAdapter = itemAdapter

        if (requiresDataDownload) {
            itemAdapter.resetItems()
        }

        val recyclerView = binding.itemList
        recyclerView.adapter = itemAdapter

        val filterEditText = binding.filter
        if (shouldShowFilter(itemAdapter.items)) {
            itemAdapter.addOrReplaceFilterOnChangeListener(filterEditText)
        } else {
            filterEditText.visibility = GONE
            itemAdapter.updateFilteredList("")
        }
        binding.tipIssueSelection.visibility = if (shouldShowItemSelectionTip()) VISIBLE else GONE
        binding.validateSelection.visibility = if (shouldShowSelectionValidation()) VISIBLE else GONE
        binding.cancelSelection.visibility = if (shouldShowSelectionValidation()) VISIBLE else GONE

        binding.emptyList.visibility = if (requiresDataDownload || itemAdapter.itemCount > 0) INVISIBLE else VISIBLE

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
            binding.addToCollectionWrapper.visibility = VISIBLE
            binding.progressBar.visibility = VISIBLE
            CoverFlowFileHandler.current.resizeUntilFileSize(SearchFromCover())
        }
    }

    private fun showNavigation() {
        if (shouldShowNavigationCountry()) {
            WhatTheDuck.appDB!!.inducksCountryDao().findByCountryCode(WhatTheDuck.selectedCountry)
                .observe(this, Observer { inducksCountryName: InducksCountryName ->
                    setNavigationCountry(inducksCountryName.countryCode, inducksCountryName.countryName)
                })
        } else {
            binding.navigationCountry.root.visibility = INVISIBLE
        }
        if (shouldShowNavigationPublication()) {
            WhatTheDuck.appDB!!.inducksPublicationDao().findByPublicationCode(WhatTheDuck.selectedPublication!!)
                .observe(this, Observer { inducksPublication: InducksPublication ->
                    setNavigationPublication(inducksPublication.publicationCode, inducksPublication.title)
                })
        } else {
            binding.navigationPublication.root.visibility = INVISIBLE
        }
    }

    private fun setNavigationCountry(selectedCountry: String, countryFullName: String) {
        val countryNavigationView = binding.navigationCountry
        val uri = "@drawable/flags_$selectedCountry"
        var imageResource = resources.getIdentifier(uri, null, packageName)
        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown
        }

        countryNavigationView.selectedBadgeImage.setImageResource(imageResource)
        countryNavigationView.selectedText.text = countryFullName
    }

    private fun setNavigationPublication(selectedPublication: String, publicationFullName: String) {
        val publicationNavigationView = binding.navigationPublication

        publicationNavigationView.selectedBadge.text = selectedPublication.split("/").toTypedArray()[1]
        publicationNavigationView.selectedText.text = publicationFullName
    }

    fun onBackFromAddIssueActivity() {
        if (isPossessedByUser()) {
            goToAlternativeView()
        } else {
            type = WhatTheDuck.CollectionType.USER.toString()
            startActivity(Intent(this, CountryList::class.java))
        }
    }
}