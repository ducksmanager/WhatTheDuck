package net.ducksmanager.whattheduck

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserIssueAndScore
import net.ducksmanager.persistence.models.composite.IssueListToUpdate
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.whattheduck.PurchaseAdapter.NoPurchase
import retrofit2.Response
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class AddIssue : AppCompatActivity(), View.OnClickListener {
    private lateinit var purchases: MutableList<Purchase>

    companion object {
        private val myCalendar = Calendar.getInstance()
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addissue)
        downloadPurchaseList()
    }

    private fun downloadPurchaseList() {
        DmServer.api.userPurchases.enqueue(object : DmServer.Callback<List<Purchase>>("getpurchases", this, true) {
            override fun onSuccessfulResponse(response: Response<List<Purchase>>) {
                response.body()?.let {
                    WhatTheDuck.appDB.purchaseDao().deleteAll()
                    WhatTheDuck.appDB.purchaseDao().insertList(it)
                }
                setData()
            }
        })
    }

    private fun setData() {
        WhatTheDuck.appDB.purchaseDao().findAll().observe(this, Observer<List<Purchase>> { purchases: List<Purchase> ->
            this.purchases = arrayListOf(NoPurchase())
            this.purchases.addAll(purchases)

            show()
        })
    }

    private fun show() {
        findViewById<View>(R.id.noCondition).setOnClickListener(this)
        findViewById<View>(R.id.badCondition).setOnClickListener(this)
        findViewById<View>(R.id.notSoGoodCondition).setOnClickListener(this)
        findViewById<View>(R.id.goodCondition).setOnClickListener(this)

        showPurchases()
        findViewById<View>(R.id.noCondition).performClick()

        PurchaseAdapter.selectedItem = purchases[0]

        findViewById<View>(R.id.addissue_ok).setOnClickListener {
            val dmCondition = when (findViewById<RadioGroup>(R.id.condition).checkedRadioButtonId) {
                R.id.badCondition -> InducksIssueWithUserIssueAndScore.BAD_CONDITION
                R.id.notSoGoodCondition -> InducksIssueWithUserIssueAndScore.NOTSOGOOD_CONDITION
                R.id.goodCondition -> InducksIssueWithUserIssueAndScore.GOOD_CONDITION
                else -> InducksIssueWithUserIssueAndScore.NO_CONDITION
            }
            val issueListToUpdate = IssueListToUpdate(
                WhatTheDuck.selectedPublication!!, listOf(WhatTheDuck.selectedIssue!!),
                dmCondition,
                if (PurchaseAdapter.selectedItem is NoPurchase) null else PurchaseAdapter.selectedItem!!.id
            )

            DmServer.api.createUserIssues(issueListToUpdate).enqueue(object : DmServer.Callback<Any>("addissue", this@AddIssue, true) {
                override fun onSuccessfulResponse(response: Response<Any>) {
                    finish()
                    WhatTheDuck.info(WeakReference(this@AddIssue), R.string.confirmation_message__issue_inserted, Toast.LENGTH_SHORT)
                    startActivity(Intent(this@AddIssue, Login::class.java))
                }
            })
        }

        findViewById<View>(R.id.addissue_cancel).setOnClickListener { finish() }

        findViewById<View>(R.id.addpurchase).setOnClickListener {
            toggleAddPurchaseButton(false)
            showNewPurchase()
        }

        findViewById<TextView>(R.id.addIssueTitle).text = getString(R.string.insert_issue__confirm, WhatTheDuck.selectedIssue)
    }

    private fun toggleAddPurchaseButton(toggle: Boolean) {
        findViewById<View>(R.id.addpurchase).visibility =
            if (toggle)
                View.VISIBLE
            else
                View.GONE
    }

    private fun showNewPurchase() {
        val newPurchaseSection = findViewById<View>(R.id.newpurchase)
        newPurchaseSection.visibility = View.VISIBLE

        val purchaseDateNew = findViewById<EditText>(R.id.purchasedatenew)
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

        val purchaseTitleNew = findViewById<EditText>(R.id.purchasetitlenew)
        purchaseTitleNew.setOnKeyListener { _, _, _ ->
            findViewById<View>(R.id.purchasetitlenew).background.colorFilter = null
            false
        }

        findViewById<Button>(R.id.createpurchase)
            .setOnClickListener { floatingButtonView: View ->
                if (purchaseDateNew.text.toString() == "") {
                    purchaseDateNew.background.setColorFilter(ContextCompat.getColor(applicationContext, R.color.colorAccent), PorterDuff.Mode.SRC_IN)
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

        findViewById<Button>(R.id.createpurchasecancel)
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
        val rv = findViewById<RecyclerView>(R.id.purchase_list)
        rv.adapter = PurchaseAdapter(this, purchases)
        rv.layoutManager = LinearLayoutManager(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.noCondition,
            R.id.badCondition,
            R.id.notSoGoodCondition,
            R.id.goodCondition ->
                findViewById<TextView>(R.id.addissue_condition_text).text = view.contentDescription.toString()
        }
    }
}