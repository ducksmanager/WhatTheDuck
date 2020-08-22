package net.ducksmanager.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.ducksmanager.persistence.models.dm.Purchase
import net.ducksmanager.whattheduck.R

class PurchaseAdapter internal constructor(private val context: Context, private val items: List<Purchase>) : RecyclerView.Adapter<PurchaseAdapter.ViewHolder>() {

    internal class NoPurchase : Purchase()

    override fun onBindViewHolder(purchaseHolder: ViewHolder, i: Int) {
        val purchase = items[i]
        purchaseHolder.purchaseCheck.isChecked = purchase === selectedItem

        val isNoPurchase = purchase is NoPurchase
        purchaseHolder.purchaseDate.visibility = if (!isNoPurchase) View.VISIBLE else View.GONE
        purchaseHolder.purchaseName.visibility = if (!isNoPurchase) View.VISIBLE else View.GONE
        purchaseHolder.purchaseCheck.visibility = View.VISIBLE
        purchaseHolder.noPurchaseTitle.visibility = if (isNoPurchase) View.VISIBLE else View.GONE
        purchaseHolder.purchaseCheck.contentDescription = purchase.toString()
        if (!isNoPurchase) {
            purchaseHolder.purchaseDate.text = purchase.date
            purchaseHolder.purchaseName.text = purchase.description
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_purchase, viewGroup, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val purchaseCheck: RadioButton = v.findViewById(R.id.purchasecheck)
        val purchaseDate: TextView = v.findViewById(R.id.purchasedate)
        val purchaseName: TextView = v.findViewById(R.id.purchasename)
        val noPurchaseTitle: TextView = v.findViewById(R.id.nopurchase)

        init {
            val clickListener = View.OnClickListener {
                selectedItem = items[adapterPosition]
                notifyItemRangeChanged(0, items.size)
            }
            itemView.setOnClickListener(clickListener)
            purchaseCheck.setOnClickListener(clickListener)
        }
    }

    companion object {
        var selectedItem: Purchase? = null
    }

}