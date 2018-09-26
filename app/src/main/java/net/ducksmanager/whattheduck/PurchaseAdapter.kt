package net.ducksmanager.whattheduck

import android.app.Activity
import android.view.View
import android.widget.TextView

import net.igenius.customcheckbox.CustomCheckBox

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Comparator
import java.util.Date
import java.util.HashMap
import java.util.Locale

class PurchaseAdapter internal constructor(activity: Activity, items: HashMap<String, Purchase>) : ItemAdapter<PurchaseAdapter.Purchase>(activity, R.layout.row_purchase, ArrayList(items.values)) {

    protected override val onClickListener: View.OnClickListener?
        get() = null

    protected override val comparator: Comparator<Purchase>
        get() = { purchase1, purchase2 ->
            this@PurchaseAdapter.getComparatorText(purchase2)!!
                    .compareTo(this@PurchaseAdapter.getComparatorText(purchase1))
        }

    abstract class Purchase {
        internal var isNoPurchase: Boolean? = null
    }

    internal class SpecialPurchase(noPurchase: Boolean?) : Purchase() {
        init {
            this.isNoPurchase = noPurchase
        }
    }

    class PurchaseWithDate(val id: Int?, internal val purchaseDate: Date, internal val purchaseName: String) : Purchase() {

        init {
            this.isNoPurchase = java.lang.Boolean.FALSE
        }
    }

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    internal inner class ViewHolder(v: View) : ItemAdapter.ViewHolder(v), View.OnClickListener {
        val purchaseCheck: CustomCheckBox
        val purchaseDate: TextView
        val purchaseTitle: TextView
        val noPurchaseTitle: TextView

        init {

            purchaseCheck = v.findViewById(R.id.purchasecheck)
            purchaseDate = v.findViewById(R.id.purchasedate)
            purchaseTitle = getTitleTextView(v)
            noPurchaseTitle = v.findViewById(R.id.nopurchase)
        }

        override fun onClick(view: View) {
            (view.findViewById<View>(R.id.purchasecheck) as CustomCheckBox).isChecked = true
        }
    }

    override fun onBindViewHolder(holder: ItemAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val purchaseHolder = holder as ViewHolder

        val purchase = getItem(position)
        if (purchase != null) {
            val isNoPurchase = purchase.isNoPurchase

            purchaseHolder.purchaseDate.visibility = if ((!isNoPurchase)!!) View.VISIBLE else View.GONE
            purchaseHolder.purchaseTitle.visibility = if ((!isNoPurchase)!!) View.VISIBLE else View.GONE
            purchaseHolder.purchaseCheck.visibility = View.VISIBLE
            purchaseHolder.noPurchaseTitle.visibility = if (isNoPurchase) View.VISIBLE else View.GONE

            v.minimumHeight = 40
            purchaseHolder.purchaseCheck.contentDescription = purchase.toString()

            purchaseHolder.purchaseCheck.setTag(R.id.check_by_user, java.lang.Boolean.FALSE)
            purchaseHolder.purchaseCheck.isChecked = purchase.toString() == AddIssue.selectedPurchaseHash
            purchaseHolder.purchaseCheck.setTag(R.id.check_by_user, null)

            if ((!isNoPurchase)!!) {
                purchaseHolder.purchaseDate.text = dateFormat.format((purchase as PurchaseWithDate).purchaseDate)
            }
        }
    }

    override fun isHighlighted(i: Purchase): Boolean {
        return false
    }

    override fun getPrefixImageResource(i: Purchase, activity: Activity): Int? {
        return null
    }

    override fun getSuffixImageResource(i: Purchase): Int? {
        return null
    }

    override fun getSuffixText(i: Purchase): String? {
        return null
    }

    override fun getText(p: Purchase): String {
        return (p as? PurchaseWithDate)?.purchaseName ?: ""
    }

    override fun getComparatorText(i: Purchase): String? {
        return if (i.isNoPurchase)
            "^"
        else
            dateFormat.format((i as PurchaseWithDate).purchaseDate)
    }

    override fun getIdentifier(i: Purchase): String {
        return if (i.isNoPurchase)
            "no"
        else
            (i as PurchaseWithDate).id.toString()
    }

    companion object {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
}
