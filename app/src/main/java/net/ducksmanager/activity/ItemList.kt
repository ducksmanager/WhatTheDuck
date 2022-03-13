package net.ducksmanager.activity

import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import net.ducksmanager.adapter.CountryAdapter
import net.ducksmanager.adapter.ItemAdapter
import net.ducksmanager.persistence.models.coa.InducksCountryName
import net.ducksmanager.persistence.models.coa.InducksPublication
import net.ducksmanager.persistence.models.composite.InducksCountryNameWithPossession
import net.ducksmanager.persistence.models.composite.InducksPublicationWithPossession
import net.ducksmanager.util.AppCompatActivityWithDrawer
import net.ducksmanager.util.ConnectionDetector
import net.ducksmanager.util.CoverFlowFileHandler
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.applicationPackage
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isNewVersionAvailable
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.isOfflineMode
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.numberOfIssues
import net.ducksmanager.whattheduck.databinding.WtdListBinding
import java.lang.ref.WeakReference

abstract class ItemList<Item> : AppCompatActivityWithDrawer() {

    companion object {
        var type = WhatTheDuck.CollectionType.USER.toString()

        const val MIN_ITEM_NUMBER_FOR_FILTER = 20
        const val MY_PERMISSIONS_REQUEST = 0

        fun isCoaList(): Boolean = type == WhatTheDuck.CollectionType.COA.toString()
    }

    private lateinit var connectionDetector: ConnectionDetector
    protected var viewModel = AndroidViewModel(application)
    abstract val AndroidViewModel.data: LiveData<List<Item>>

    @JvmField
    var data: List<Item> = ArrayList()
    lateinit var binding: WtdListBinding

    protected abstract fun hasDividers(): Boolean
    protected abstract fun isPossessedByUser(): Boolean
    protected abstract fun shouldShow(): Boolean
    protected abstract fun shouldShowNavigationCountry(): Boolean
    protected abstract fun shouldShowNavigationPublication(): Boolean
    protected abstract fun shouldShowAddToCollectionButton(): Boolean
    protected abstract fun shouldShowItemSelectionTip(): Boolean
    protected abstract fun shouldShowSelectionValidation(): Boolean
    protected abstract fun shouldShowZoom(): Boolean

    private var getCameraImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            CoverFlowFileHandler.current.resizeUntilFileSize(CoverFlowFileHandler.SearchFromCover())
            binding.addToCollectionWrapper.visibility = VISIBLE
            binding.progressBar.visibility = VISIBLE
        }
    }
    private var pickCameraImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.data != null) {
            CoverFlowFileHandler.current.uploadUri = result.data!!.data
            CoverFlowFileHandler.current.resizeUntilFileSize(CoverFlowFileHandler.SearchFromCover())
            binding.addToCollectionWrapper.visibility = VISIBLE
            binding.progressBar.visibility = VISIBLE
        }
    }

    open fun downloadAndShowList() {
        if (!viewModel.data.hasObservers()) {
            val latestVersion = appDB!!.appVersionDao().find()
            isNewVersionAvailable =
                if (latestVersion == null) false
                else getAppVersionFromString(latestVersion.version) >
                    getAppVersionFromString(WhatTheDuck.applicationVersion)

            viewModel.data.observe(this, onObserve())
        }
    }

    open fun onObserve(): (t: List<Item>) -> Unit = { items ->
        binding.warningMessage.visibility = VISIBLE
        when {
            isOfflineMode -> {
                binding.warningMessage.text = getString(R.string.offline_mode)
                binding.warningMessage.setOnClickListener {}
            }
            isNewVersionAvailable -> {
                binding.warningMessage.text = getString(R.string.new_version_available)
                binding.warningMessage.setOnClickListener {
                    val urlFallback = "https://play.google.com/store/apps/details?id=$applicationPackage"
                    val url = try {
                        this.packageManager.getPackageInfo("com.android.vending", 0)
                        "market://details?id=$applicationPackage"
                    } catch (e: Exception) {
                        urlFallback
                    }
                    val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
                    if (intent.resolveActivity(packageManager) != null)
                        this@ItemList.startActivity(intent)
                    else {
                        this@ItemList.startActivity(intent.setData(Uri.parse(urlFallback)))
                    }
                }
            }
            else -> binding.warningMessage.visibility = GONE
        }
        itemAdapter.setItems(items)
        if (itemAdapter is CountryAdapter) {
            numberOfIssues = items.sumOf { (it as InducksCountryNameWithPossession).possessedIssues }
        }
        title = String.format(getString(R.string.my_collection), numberOfIssues)

        val isEmptyList = (isCoaList() && isOfflineMode) || (!isCoaList() && items.none {
            when (it) {
                is InducksCountryNameWithPossession -> it.possessedIssues > 0
                is InducksPublicationWithPossession -> it.possessedIssues > 0
                else -> true
            }
        })
        binding.emptyList.visibility = if (isEmptyList) VISIBLE else INVISIBLE
        binding.itemList.visibility = if (isEmptyList) INVISIBLE else VISIBLE
        if (isCoaList() && isOfflineMode) {
            binding.emptyList.text = getString(R.string.offline_mode_cannot_view)
            binding.emptyList.setOnClickListener {
                type = WhatTheDuck.CollectionType.USER.toString()
                startActivity(Intent(this, CountryList::class.java))
            }
        }
        binding.addToCollectionWrapper.visibility = if (shouldShowAddToCollectionButton()) VISIBLE else INVISIBLE

        val filterEditText = binding.filter
        itemAdapter.updateFilteredList("")
        if (itemAdapter.hasEnoughItemsForFilter() && isFilterableList()) {
            itemAdapter.addOrReplaceFilterOnChangeListener(filterEditText)
        } else {
            filterEditText.visibility = GONE
        }
        binding.progressBar.visibility = GONE

        scrollToSavedPosition()
    }

    open fun scrollToSavedPosition() {}

    protected abstract fun isFilterableList(): Boolean

    protected abstract var itemAdapter: ItemAdapter<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = WtdListBinding.inflate(layoutInflater)
        registerForContextMenu(binding.root)
        setContentView(binding.root)

        connectionDetector = ConnectionDetector(lifecycleScope) {
            run {
                loadList()
            }
        }

        toggleToolbar()
        toggleNavigation()

        binding.navigationAllCountries.root.setOnClickListener { goToView(CountryList::class.java) }
        binding.navigationCountry.selected.setOnClickListener { goToView(PublicationList::class.java) }

        binding.addToCollectionWrapper.setOnClickListener {
            binding.addToCollectionByStoryTitle.visibility = if (binding.addToCollectionByStoryTitle.visibility == GONE) VISIBLE else GONE
            binding.addToCollectionFromRecentIssues.visibility = if (binding.addToCollectionFromRecentIssues.visibility == GONE) VISIBLE else GONE
            binding.addToCollectionByFileButton.visibility = if (binding.addToCollectionByFileButton.visibility == GONE) VISIBLE else GONE
            binding.addToCollectionByPhotoButton.visibility = if (binding.addToCollectionByPhotoButton.visibility == GONE) VISIBLE else GONE
            binding.addToCollectionBySelectionButton.visibility = if (binding.addToCollectionBySelectionButton.visibility == GONE) VISIBLE else GONE
        }

        binding.addToCollectionByStoryTitle.setOnClickListener { startActivity(Intent(this, Search::class.java)) }
        binding.addToCollectionFromRecentIssues.setOnClickListener { startActivity(Intent(this, RecentIssues::class.java)) }
        binding.addToCollectionByFileButton.setOnClickListener { pickCoverPicture() }
        binding.addToCollectionByPhotoButton.setOnClickListener { takeCoverPicture() }
        binding.addToCollectionBySelectionButton.setOnClickListener { goToAlternativeView() }

        binding.itemList.adapter = itemAdapter

        (application as WhatTheDuck).trackActivity(this)
        loadList()
    }

    override fun onStop() {
        super.onStop()
        connectionDetector.unregister()
    }

    private fun goToView(cls: Class<*>) {
        if (this@ItemList.javaClass != cls) {
            startActivity(Intent(this, cls))
        }
    }

    private fun goToAlternativeView() {
        type = if (isCoaList()) {
            WhatTheDuck.CollectionType.USER.toString()
        } else {
            WhatTheDuck.CollectionType.COA.toString()
        }
        (application as WhatTheDuck).trackActivity(this)
        loadList()
    }

    protected fun loadList() {
        show()
        downloadAndShowList()
    }

    protected open fun show() {
        binding.warningMessage.visibility = if (isOfflineMode) VISIBLE else GONE

        binding.addToCollectionByStoryTitle.visibility = GONE
        binding.addToCollectionFromRecentIssues.visibility = GONE
        binding.addToCollectionByFileButton.visibility = GONE
        binding.addToCollectionByPhotoButton.visibility = GONE
        binding.addToCollectionBySelectionButton.visibility = GONE

        binding.tipIssueSelection.visibility = if (shouldShowItemSelectionTip()) VISIBLE else GONE
        binding.validateSelection.visibility = if (shouldShowSelectionValidation()) VISIBLE else GONE
        binding.cancelSelection.visibility = if (shouldShowSelectionValidation()) VISIBLE else GONE
        binding.zoomWrapper.visibility = if (shouldShowZoom()) VISIBLE else GONE
    }

    private fun pickCoverPicture() {
        CoverFlowFileHandler.current = CoverFlowFileHandler(WeakReference(this))
        pickCameraImage.launch(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI))
    }

    private fun takeCoverPicture() {
        CoverFlowFileHandler.current = CoverFlowFileHandler(WeakReference(this))
        if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(CAMERA), MY_PERMISSIONS_REQUEST)
        }
        else {
            launchCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> launchCamera()
        }
    }

    private fun launchCamera() {
        val photoURI = CoverFlowFileHandler.current.createEmptyFileForCamera(this@ItemList)
        getCameraImage.launch(photoURI)
    }

    private fun toggleNavigation() {
        if (!shouldShowNavigationCountry()) {
            binding.navigationCountry.root.visibility = INVISIBLE
        }
        if (!shouldShowNavigationPublication()) {
            binding.navigationPublication.root.visibility = INVISIBLE
        }
    }

    protected fun setNavigationCountry(country: InducksCountryName) {
        val countryNavigationView = binding.navigationCountry
        val uri = "@drawable/flags_${country.countryCode}"
        var imageResource = resources.getIdentifier(uri, null, packageName)
        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown
        }

        countryNavigationView.selectedBadgeImage.setImageResource(imageResource)
        countryNavigationView.selectedText.text = country.countryName
    }

    protected fun setNavigationPublication(publication: InducksPublication) {
        val publicationNavigationView = binding.navigationPublication

        publicationNavigationView.selectedBadge.text = publication.publicationCode.split("/").toTypedArray()[1]
        publicationNavigationView.selectedText.text = publication.title
    }

    fun onBackFromAddIssueActivity() {
        if (isPossessedByUser()) {
            goToAlternativeView()
        } else {
            type = WhatTheDuck.CollectionType.USER.toString()
            startActivity(Intent(this, CountryList::class.java))
        }
    }

    private fun getAppVersionFromString(version: String): Int {
        val versions = version.split(".")
        val major = versions[0].toInt() * 10000
        val minor = versions[1].toInt() * 100
        val patch = versions[2].toInt()
        return major + minor + patch
    }
}