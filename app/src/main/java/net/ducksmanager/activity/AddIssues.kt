package net.ducksmanager.activity

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout.*
import kotlinx.android.synthetic.main.addissues.*
import net.ducksmanager.adapter.PurchaseAdapter
import net.ducksmanager.adapter.PurchaseAdapter.NoPurchase
import net.ducksmanager.api.DmServer
import net.ducksmanager.api.DmServer.Companion.api
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData
import net.ducksmanager.persistence.models.composite.IssueCopiesToUpdate
import net.ducksmanager.persistence.models.composite.IssueListToUpdate
import net.ducksmanager.persistence.models.dm.Issue
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.info
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedIssues
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedPublication
import net.ducksmanager.whattheduck.databinding.AddissuesBinding
import retrofit2.Response
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class AddIssues : AppCompatActivity(), OnClickListener {
    private lateinit var purchases: MutableList<Purchase>
    private lateinit var binding: AddissuesBinding

    private var copies: IssueCopiesToUpdate? = null

    companion object {
        private val myCalendar = Calendar.getInstance()
        private const val MAX_COPIES = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddissuesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val issueNumber = selectedIssues.first()
        when (selectedIssues.toSet().size) {
            1 -> {
                binding.addIssueTitle.text = getString(R.string.insert_issue__title_single_issue, issueNumber)
            }
            2 -> {
                binding.addIssueTitle.text = getString(R.string.insert_issue__title_multiple_issues_singular, issueNumber)
            }
            else -> {
                binding.addIssueTitle.text = getString(R.string.insert_issue__title_multiple_issues_plural, issueNumber, selectedIssues.size - 1)
            }
        }

        downloadPurchaseList()
    }

    private fun downloadPurchaseList() {
        api.userPurchases.enqueue(object : DmServer.Callback<List<Purchase>>("getpurchases", this, true) {
            override fun onSuccessfulResponse(response: Response<List<Purchase>>) {
                response.body()?.let {
                    appDB!!.purchaseDao().deleteAll()
                    appDB!!.purchaseDao().insertList(it)
                }
                setData()
            }
        })
    }

    private fun setData() {
        appDB!!.purchaseDao().findAll().observe(this, { purchases: List<Purchase> ->
            this.purchases = arrayListOf(NoPurchase())
            this.purchases.addAll(purchases)

            val publicationCode = selectedPublication!!.publicationCode
            appDB!!.inducksIssueDao().findUserOwnedByPublicationCodeAndIssueNumber(publicationCode, selectedIssues.first()).observe(this, { dbCopies: List<InducksIssueWithUserData> ->
                if (selectedIssues.toSet().size > 1) {
                    this.binding.issueCopies.visibility = GONE
                    show(dbCopies.firstOrNull()?.userIssue)
                } else {
                    copies = IssueCopiesToUpdate(
                        publicationCode,
                        selectedIssues.toSet(),
                        dbCopies.map { it.userIssue?.condition }.toMutableList(),
                        dbCopies.map { it.userIssue?.purchaseId }.toMutableList()
                    )
                    show()
                }
            })
        })
    }

    private fun onSuccessfulUpdate() {
        finish()
        info(WeakReference(this@AddIssues), R.string.confirmation_message__collection_updated, Toast.LENGTH_SHORT)
        WhatTheDuck.currentUser = null // Force to re-trigger a collection fetch
        startActivity(Intent(this@AddIssues, Login::class.java))
    }

    private fun show(firstSelectedIssue: Issue? = null) {
        binding.missing.setOnClickListener(this)
        binding.noCondition.setOnClickListener(this)
        binding.badCondition.setOnClickListener(this)
        binding.notSoGoodCondition.setOnClickListener(this)
        binding.goodCondition.setOnClickListener(this)

        showPurchases()

        binding.addissueOk.setOnClickListener {
            if (copies != null) {
                saveCopyForCurrentTab(issueCopies.getTabAt(issueCopies.selectedTabPosition)!!)
                api.updateUserIssueCopies(copies!!).enqueue(object : DmServer.Callback<Any>("updateIssues", this@AddIssues, true) {
                    override fun onSuccessfulResponse(response: Response<Any>) {
                        onSuccessfulUpdate()
                    }
                })
            } else {
                val condition = getConditionApiId()
                val issueListToUpdate = IssueListToUpdate(
                    selectedPublication!!.publicationCode,
                    selectedIssues.toSet(),
                    condition,
                    getPurchaseId()
                )

                api.updateUserIssues(issueListToUpdate).enqueue(object : DmServer.Callback<Any>("updateIssues", this@AddIssues, true) {
                    override fun onSuccessfulResponse(response: Response<Any>) {
                        onSuccessfulUpdate()
                    }
                })
            }
        }

        binding.addissueCancel.setOnClickListener { finish() }

        binding.addpurchase.setOnClickListener {
            toggleAddPurchaseButton(false)
            showNewPurchase()
        }
        binding.progressBar.visibility = GONE

        if (copies == null) {
            if (firstSelectedIssue != null) {
                setFromConditionApiId(firstSelectedIssue.condition)
                setFromPurchaseId(firstSelectedIssue.purchaseId)
            }
        } else {
            copies!!.purchaseIds.ifEmpty { mutableListOf(null) }
                .forEachIndexed { index, _ ->
                    val copyTab = binding.issueCopies.newTab()
                    copyTab.text = getString(R.string.copy, index + 1)
                    binding.issueCopies.addTab(copyTab)
                }

            val addCopyTab = binding.issueCopies.newTab()
            addCopyTab.text = getString(R.string.add_copy)
            binding.issueCopies.addTab(addCopyTab)

            binding.issueCopies.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: Tab?) {
                    if (tab!!.text == getString(R.string.add_copy)) {
                        if (copies!!.purchaseIds.size >= MAX_COPIES) {
                            info(WeakReference(this@AddIssues), R.string.max_copies_info, 1000)
                            binding.issueCopies.getTabAt(0)!!.select()
                            return
                        } else {
                            val firstCopyWithMissingCondition = copies!!.conditions.indexOfFirst { it == InducksIssueWithUserData.MISSING }
                            if (firstCopyWithMissingCondition > -1) {
                                info(WeakReference(this@AddIssues), R.string.set_conditions_on_existing_copies_before_adding_new, 2000)
                                binding.issueCopies.getTabAt(firstCopyWithMissingCondition)!!.select()
                                return
                            }
                        }
                    }
                    onCopyTabSelected(tab)
                }

                override fun onTabUnselected(tab: Tab?) {
                    if (tab!!.text != getString(R.string.add_copy)) {
                        saveCopyForCurrentTab(tab)
                    }
                }

                override fun onTabReselected(tab: Tab?) {}
            })

            onCopyTabSelected(binding.issueCopies.getTabAt(0))
        }
    }

    private fun saveCopyForCurrentTab(tab: Tab) {
        copies!!.conditions[tab.position] = getConditionApiId()
        copies!!.purchaseIds[tab.position] = getPurchaseId()
    }

    private fun onCopyTabSelected(tab: Tab?) {
        if (tab!!.text == getString(R.string.add_copy)) {
            val tabPosition = binding.issueCopies.tabCount - 1
            val copyTab = binding.issueCopies.newTab()
            copyTab.text = getString(R.string.copy, (tabPosition + 1))
            binding.issueCopies.addTab(copyTab, tabPosition, true)
        } else {
            if (copies!!.conditions.size <= tab.position) {
                copies!!.conditions.add(tab.position, InducksIssueWithUserData.NO_CONDITION)
                copies!!.purchaseIds.add(tab.position, null)
            }
            setFromConditionApiId(copies!!.conditions[tab.position])
            setFromPurchaseId(copies!!.purchaseIds[tab.position])
        }
    }

    private fun getPurchaseId() = if (PurchaseAdapter.selectedItem is NoPurchase) null else PurchaseAdapter.selectedItem?.id

    private fun setFromPurchaseId(purchaseId: Int?) {
        val selectedItemPosition = when (purchaseId) {
            null, -2 -> 0
            else -> purchases.indexOfFirst { it.id == purchaseId }
        }
        PurchaseAdapter.selectedItem = purchases[selectedItemPosition.run { if (this == -1) 0 else this }]
        showPurchases()
    }

    private fun getConditionApiId() = when (binding.condition.checkedRadioButtonId) {
        R.id.missing -> InducksIssueWithUserData.MISSING
        R.id.badCondition -> InducksIssueWithUserData.BAD_CONDITION
        R.id.notSoGoodCondition -> InducksIssueWithUserData.NOTSOGOOD_CONDITION
        R.id.goodCondition -> InducksIssueWithUserData.GOOD_CONDITION
        else -> InducksIssueWithUserData.NO_CONDITION
    }

    private fun setFromConditionApiId(condition: String?) {
        when (condition) {
            InducksIssueWithUserData.BAD_CONDITION -> binding.badCondition.performClick()
            InducksIssueWithUserData.NOTSOGOOD_CONDITION -> binding.notSoGoodCondition.performClick()
            InducksIssueWithUserData.GOOD_CONDITION -> binding.goodCondition.performClick()
            InducksIssueWithUserData.NO_CONDITION -> binding.noCondition.performClick()
            else -> binding.missing.performClick()
        }
    }

    private fun toggleAddPurchaseButton(toggle: Boolean) {
        binding.addpurchase.visibility = if (toggle) VISIBLE else GONE
    }

    private fun showNewPurchase() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val newPurchaseSection = binding.newpurchase
        newPurchaseSection.visibility = VISIBLE

        val purchaseDateNew = binding.purchasedatenew
        purchaseDateNew.requestFocus()
        purchaseDateNew.setText(dateFormat.format(Date()))
        purchaseDateNew.keyListener = null
        val date = OnDateSetListener { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
            myCalendar[year, monthOfYear] = dayOfMonth
            purchaseDateNew.setText(dateFormat.format(myCalendar.time))
        }
        purchaseDateNew.setOnClickListener { v1: View ->
            hideKeyboard(v1)
            DatePickerDialog(this, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]).show()
        }

        val purchaseTitleNew = binding.purchasetitlenew
        purchaseTitleNew.setOnKeyListener { _, _, _ ->
            binding.purchasetitlenew.background.colorFilter = null
            false
        }

        binding.createpurchase
            .setOnClickListener { floatingButtonView: View ->
                if (purchaseDateNew.text.toString() == "") {
                    purchaseDateNew.background.colorFilter = BlendModeColorFilter(ContextCompat.getColor(applicationContext, R.color.fab_color), BlendMode.SRC_IN)
                    return@setOnClickListener
                }

                hideKeyboard(floatingButtonView)

                val newPurchase = Purchase(purchaseDateNew.text.toString(), purchaseTitleNew.text.toString())
                api.createUserPurchase(newPurchase).enqueue(object : DmServer.Callback<Void>("createPurchase", this, true) {
                    override fun onSuccessfulResponse(response: Response<Void>) {
                        downloadPurchaseList()
                        toggleAddPurchaseButton(true)
                        showPurchases()
                        newPurchaseSection.visibility = GONE
                    }
                })
            }

        binding.createpurchasecancel
            .setOnClickListener { floatingButtonView: View ->
                hideKeyboard(floatingButtonView)
                newPurchaseSection.visibility = GONE
                toggleAddPurchaseButton(true)
            }
    }

    private fun hideKeyboard(floatingButtonView: View) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(floatingButtonView.windowToken, 0)
    }

    private fun showPurchases() {
        val rv = binding.purchaseList
        rv.adapter = PurchaseAdapter(this, purchases)
        rv.layoutManager = LinearLayoutManager(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.missing,
            R.id.noCondition,
            R.id.badCondition,
            R.id.notSoGoodCondition,
            R.id.goodCondition ->
                binding.addissueConditionText.text = view.contentDescription.toString()
        }

        binding.purchasesection.visibility = if (view.id == R.id.missing) INVISIBLE else VISIBLE
    }
}