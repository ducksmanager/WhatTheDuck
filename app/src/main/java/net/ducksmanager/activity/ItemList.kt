package net.ducksmanager.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.wtd_list_navigation_country.view.*
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.util.CoverFlowFileHandler
import net.ducksmanager.util.CoverFlowFileHandler.SearchFromCover
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
import net.ducksmanager.whattheduck.databinding.WtdListBinding
import java.lang.ref.WeakReference

abstract class ItemList<Item> : AppCompatActivityWithDrawer() {

    companion object {
        const val COUNTRY_NAME_INTENT_EXTRA = "countryName"
        const val PUBLICATION_TITLE_INTENT_EXTRA = "publicationTitle"

        @JvmField
        var type = WhatTheDuck.CollectionType.USER.toString()

        fun isCoaList() : Boolean = type == WhatTheDuck.CollectionType.COA.toString()

        const val MIN_ITEM_NUMBER_FOR_FILTER = 20
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    private lateinit var viewModel: AndroidViewModel
    abstract val AndroidViewModel.data: LiveData<List<Item>>

    @JvmField
    var data: List<Item> = ArrayList()
    private lateinit var binding: WtdListBinding

    protected abstract fun hasDividers(): Boolean
    protected abstract fun isPossessedByUser(): Boolean
    protected abstract fun shouldShow(): Boolean
    protected abstract fun shouldShowNavigationCountry(): Boolean
    protected abstract fun shouldShowNavigationPublication(): Boolean
    protected abstract fun shouldShowAddToCollectionButton(): Boolean
    protected abstract fun shouldShowFilter(items: List<Item>): Boolean
    protected abstract fun shouldShowItemSelectionTip(): Boolean
    protected abstract fun shouldShowSelectionValidation(): Boolean

    open fun downloadList() {}

    protected abstract var itemAdapter: ItemAdapter<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =  WtdListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showToolbarIfExists()

        binding.navigationAllCountries.root.setOnClickListener { goToView(CountryList::class.java) }

        binding.navigationCountry.root.selected?.setOnClickListener { _: View? -> goToView(PublicationList::class.java) }

        loadList()
    }

    fun goToView(cls: Class<*>) {
        if (this@ItemList.javaClass != cls) {
            val outgoingIntent = Intent(this, cls)
            outgoingIntent.putExtra(COUNTRY_NAME_INTENT_EXTRA, binding.navigationCountry.selectedText.text)
            outgoingIntent.putExtra(PUBLICATION_TITLE_INTENT_EXTRA, binding.navigationPublication.selectedText.text)
            startActivity(outgoingIntent)
        }
    }

    private fun goToAlternativeView() {
        type = if (isCoaList())
            WhatTheDuck.CollectionType.USER.toString()
        else
            WhatTheDuck.CollectionType.COA.toString()
        loadList()
    }

    protected fun loadList() {
        (application as WhatTheDuck).trackActivity(this)
        binding.itemList.adapter = itemAdapter
        show()

        viewModel = AndroidViewModel(application)
        val viewModelData = viewModel.data
        if (viewModelData.value == null) {
            binding.progressBar.visibility = VISIBLE
        }
        viewModelData.observe(this, { items ->
            if (items.isEmpty()) {
                downloadList()
            }
            else {
                itemAdapter.setItems(items)
                binding.emptyList.visibility = if (items.isNotEmpty()) INVISIBLE else VISIBLE
                binding.progressBar.visibility = GONE

                val filterEditText = binding.filter
                if (shouldShowFilter(itemAdapter.items)) {
                    itemAdapter.addOrReplaceFilterOnChangeListener(filterEditText)
                } else {
                    filterEditText.visibility = GONE
                    itemAdapter.updateFilteredList("")
                }
            }
        })
    }

    private fun show() {
        if (!shouldShow()) {
            return
        }
        toggleNavigation()
        binding.offlineMode.visibility = if (isOfflineMode) VISIBLE else GONE

        binding.addToCollectionByPhotoButton.visibility = GONE
        binding.addToCollectionBySelectionButton.visibility = GONE
        val addToCollection = binding.addToCollectionWrapper
        if (shouldShowAddToCollectionButton()) {
            addToCollection.visibility = if (isCoaList()) GONE else VISIBLE

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

        val recyclerView = binding.itemList
        binding.tipIssueSelection.visibility = if (shouldShowItemSelectionTip()) VISIBLE else GONE
        binding.validateSelection.visibility = if (shouldShowSelectionValidation()) VISIBLE else GONE
        binding.cancelSelection.visibility = if (shouldShowSelectionValidation()) VISIBLE else GONE

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

    private fun toggleNavigation() {
        if (!shouldShowNavigationCountry()) {
            binding.navigationCountry.root.visibility = INVISIBLE
        }
        if (!shouldShowNavigationPublication()) {
            binding.navigationPublication.root.visibility = INVISIBLE
        }
    }

    protected fun setNavigationCountry(selectedCountry: String, countryFullName: String) {
        val countryNavigationView = binding.navigationCountry
        val uri = "@drawable/flags_$selectedCountry"
        var imageResource = resources.getIdentifier(uri, null, packageName)
        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown
        }

        countryNavigationView.selectedBadgeImage.setImageResource(imageResource)
        countryNavigationView.selectedText.text = countryFullName
    }

    protected fun setNavigationPublication(selectedPublication: String, publicationFullName: String) {
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