package net.ducksmanager.activity

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import net.ducksmanager.adapter.PurchaseAdapter
import net.ducksmanager.adapter.PurchaseAdapter.NoPurchase
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.IssueListToUpdate
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedIssues
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.selectedPublication
import net.ducksmanager.whattheduck.databinding.AddissuesBinding
import retrofit2.Response
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class AddIssues : AppCompatActivity(), View.OnClickListener {
    private lateinit var purchases: MutableList<Purchase>
    private lateinit var binding: AddissuesBinding

    companion object {
        private val myCalendar = Calendar.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddissuesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        downloadPurchaseList()
    }

    private fun downloadPurchaseList() {
        DmServer.api.userPurchases.enqueue(object : DmServer.Callback<List<Purchase>>("getpurchases", this, true) {
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

            show()
        })
    }

    private fun show() {
        binding.noCondition.setOnClickListener(this)
        binding.badCondition.setOnClickListener(this)
        binding.notSoGoodCondition.setOnClickListener(this)
        binding.goodCondition.setOnClickListener(this)

        showPurchases()
        binding.noCondition.performClick()

        PurchaseAdapter.selectedItem = purchases[0]

        binding.addissueOk.setOnClickListener {
            val dmCondition = when (binding.condition.checkedRadioButtonId) {
                R.id.badCondition -> InducksIssueWithUserIssueAndScore.BAD_CONDITION
                R.id.notSoGoodCondition -> InducksIssueWithUserIssueAndScore.NOTSOGOOD_CONDITION
                R.id.goodCondition -> InducksIssueWithUserIssueAndScore.GOOD_CONDITION
                else -> InducksIssueWithUserIssueAndScore.NO_CONDITION
            }
            val issueListToUpdate = IssueListToUpdate(
                selectedPublication!!.publicationCode,
                selectedIssues,
                dmCondition,
                if (PurchaseAdapter.selectedItem is NoPurchase) null else PurchaseAdapter.selectedItem!!.id
            )

            DmServer.api.createUserIssues(issueListToUpdate).enqueue(object : DmServer.Callback<Any>("addissue", this@AddIssues, true) {
                override fun onSuccessfulResponse(response: Response<Any>) {
                    finish()
                    WhatTheDuck.info(WeakReference(this@AddIssues), R.string.confirmation_message__issue_inserted, Toast.LENGTH_SHORT)
                    startActivity(Intent(this@AddIssues, Login::class.java))
                }
            })
        }

        binding.addissueCancel.setOnClickListener { finish() }

        binding.addpurchase.setOnClickListener {
            toggleAddPurchaseButton(false)
            showNewPurchase()
        }

        when (selectedIssues.size) {
            1 -> {
                binding.addIssueTitle.text = getString(R.string.insert_issue__title_single_issue, selectedIssues.first())
            }
            2 -> {
                binding.addIssueTitle.text = getString(R.string.insert_issue__title_multiple_issues_singular, selectedIssues.first())
            }
            else -> {
                binding.addIssueTitle.text = getString(R.string.insert_issue__title_multiple_issues_plural, selectedIssues.first(), selectedIssues.size - 1)
            }
        }
        binding.progressBar.visibility = GONE
    }

    private fun toggleAddPurchaseButton(toggle: Boolean) {
        binding.addpurchase.visibility =
            if (toggle)
                View.VISIBLE
            else
                View.GONE
    }

    private fun showNewPurchase() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val newPurchaseSection = binding.newpurchase
        newPurchaseSection.visibility = View.VISIBLE

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
                    purchaseDateNew.background.setColorFilter(ContextCompat.getColor(applicationContext, R.color.fab_color), PorterDuff.Mode.SRC_IN)
                    return@setOnClickListener
                }

                hideKeyboard(floatingButtonView)

                val newPurchase = Purchase(purchaseDateNew.text.toString(), purchaseTitleNew.text.toString())
                DmServer.api.createUserPurchase(newPurchase).enqueue(object : DmServer.Callback<Void>("createPurchase", this, true) {
                    override fun onSuccessfulResponse(response: Response<Void>) {
                        downloadPurchaseList()
                        toggleAddPurchaseButton(true)
                        showPurchases()
                        newPurchaseSection.visibility = View.GONE
                    }
                })
            }

        binding.createpurchasecancel
            .setOnClickListener { floatingButtonView: View ->
                hideKeyboard(floatingButtonView)
                newPurchaseSection.visibility = View.GONE
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
            R.id.noCondition,
            R.id.badCondition,
            R.id.notSoGoodCondition,
            R.id.goodCondition ->
                binding.addissueConditionText.text = view.contentDescription.toString()
        }
    }
}