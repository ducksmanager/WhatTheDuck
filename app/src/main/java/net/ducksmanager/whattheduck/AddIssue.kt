package net.ducksmanager.whattheduck

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import net.ducksmanager.retrievetasks.CreatePurchase
import net.ducksmanager.retrievetasks.GetPurchaseList
import net.ducksmanager.util.MultipleCustomCheckboxes

import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.Locale

class AddIssue : AppCompatActivity() {


    private val myCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.addissue)

        purchases = WhatTheDuck.userCollection.purchasesWithEmptyItem
        show()
    }

    private fun show() {
        val conditionCheckboxes = MultipleCustomCheckboxes(
                WeakReference(findViewById(R.id.condition_selector)),
                { view ->
                    selectedCondition = view.contentDescription.toString()
                    (findViewById<View>(R.id.addissue_condition_text) as TextView).text = selectedCondition
                },
                { view ->
                    selectedCondition = null
                    (findViewById<View>(R.id.addissue_condition_text) as TextView).text = ""
                }

        )
        conditionCheckboxes.initClickEvents()
        conditionCheckboxes.checkInitialCheckbox { checkbox -> checkbox.id == R.id.nocondition }

        showPurchases(true)

        findViewById<View>(R.id.addissue_ok).setOnClickListener { view ->
            val appContext = WhatTheDuck.wtd!!.applicationContext
            val dmCondition: String
            if (selectedCondition == appContext.getString(R.string.condition_none))
                dmCondition = Issue.NO_CONDITION
            else if (selectedCondition == appContext.getString(R.string.condition_bad))
                dmCondition = Issue.BAD_CONDITION
            else if (selectedCondition == appContext.getString(R.string.condition_notsogood))
                dmCondition = Issue.NOTSOGOOD_CONDITION
            else
                dmCondition = Issue.GOOD_CONDITION

            val selectedPurchase = purchases!!.get(selectedPurchaseHash)

            net.ducksmanager.retrievetasks.AddIssue(
                    WeakReference(this@AddIssue),
                    WhatTheDuck.selectedPublication,
                    Issue(
                            WhatTheDuck.selectedIssue,
                            dmCondition,
                            selectedPurchase as? PurchaseAdapter.PurchaseWithDate
                    )
            ).execute()
        }

        findViewById<View>(R.id.addissue_cancel).setOnClickListener { view -> finish() }

        findViewById<View>(R.id.addpurchase).setOnClickListener { view ->
            toggleAddPurchaseButton(false)
            showNewPurchase()
        }

        title = getString(R.string.insert_issue__confirm, WhatTheDuck.selectedIssue)
    }

    private fun toggleAddPurchaseButton(toggle: Boolean?) {
        findViewById<View>(R.id.addpurchase).isEnabled = toggle!!
    }

    private fun showNewPurchase() {
        val newPurchaseSection = findViewById<View>(R.id.newpurchase)
        val purchaseDateNew = findViewById<EditText>(R.id.purchasedatenew)
        val purchaseTitleNew = findViewById<EditText>(R.id.purchasetitlenew)
        val purchaseCreate = findViewById<Button>(R.id.createpurchase)
        val purchaseCreateCancel = findViewById<Button>(R.id.createpurchasecancel)

        newPurchaseSection.visibility = View.VISIBLE
        purchaseDateNew.requestFocus()
        purchaseDateNew.setText(dateFormat.format(Date()))
        purchaseDateNew.keyListener = null

        val date = { datePicker, year, monthOfYear, dayOfMonth ->
            myCalendar.set(year, monthOfYear, dayOfMonth)
            purchaseDateNew.setText(dateFormat.format(myCalendar.time))
        }
        purchaseDateNew.setOnClickListener { v1 ->
            hideKeyboard(v1)
            DatePickerDialog(this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        purchaseTitleNew.setOnKeyListener { view, i, keyEvent ->
            findViewById<View>(R.id.purchasetitlenew).background.colorFilter = null
            false
        }

        purchaseCreate.setOnClickListener { floatingButtonView ->
            if (purchaseDateNew.text.toString() == "") {
                purchaseDateNew.background.setColorFilter(resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN)
                return@purchaseCreate.setOnClickListener
            }

            hideKeyboard(floatingButtonView)

            try {
                object : CreatePurchase(WeakReference(this), purchaseDateNew.text.toString(), purchaseTitleNew.text.toString()) {
                    override fun afterDataHandling() {
                        object : GetPurchaseList(CreatePurchase.originActivityRef) {

                            protected override val originActivity: WeakReference<Activity>
                                get() = WeakReference(this@AddIssue)

                            override fun afterDataHandling() {
                                AddIssue.purchases = WhatTheDuck.userCollection.purchasesWithEmptyItem
                                this@AddIssue.toggleAddPurchaseButton(true)
                                this@AddIssue.showPurchases(false)
                                hideKeyboard(floatingButtonView)
                                newPurchaseSection.visibility = View.GONE
                            }
                        }.execute()
                    }
                }.execute()
            } catch (e: UnsupportedEncodingException) {
                WhatTheDuck.wtd!!.alert(WeakReference(this@AddIssue), R.string.internal_error, R.string.internal_error__purchase_creation_failed, "")
            }


        }

        purchaseCreateCancel.setOnClickListener { floatingButtonView ->
            hideKeyboard(floatingButtonView)

            newPurchaseSection.visibility = View.GONE
            toggleAddPurchaseButton(true)
        }
    }

    private fun hideKeyboard(floatingButtonView: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(floatingButtonView.windowToken, 0)
    }

    private fun showPurchases(checkNoPurchaseItem: Boolean?) {
        val rv = findViewById<RecyclerView>(R.id.purchase_list)
        rv.adapter = PurchaseAdapter(this, purchases!!)
        rv.layoutManager = LinearLayoutManager(this)

        val purchaseDateCheckboxes = MultipleCustomCheckboxes(
                WeakReference(rv),
                { view -> selectedPurchaseHash = view.contentDescription.toString() },
                { view -> selectedPurchaseHash = null }
        )
        rv.post {
            purchaseDateCheckboxes.initClickEvents()
            if (checkNoPurchaseItem!!) {
                purchaseDateCheckboxes.checkInitialCheckbox { checkbox -> checkbox.contentDescription.toString().contains(PurchaseAdapter.SpecialPurchase::class.java!!.getSimpleName()) }
            }
        }
    }

    companion object {

        private var purchases: HashMap<String, PurchaseAdapter.Purchase>? = null

        private var selectedCondition: String? = null
        internal var selectedPurchaseHash: String? = null
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
}
