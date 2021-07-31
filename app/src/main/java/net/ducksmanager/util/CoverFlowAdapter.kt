package net.ducksmanager.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithDetails
import net.ducksmanager.whattheduck.databinding.ItemCoverflowBinding

class CoverFlowAdapter(private val context: Context) : RecyclerView.Adapter<CoverflowViewHolder>() {
    private var data: List<CoverSearchIssueWithDetails>? = null
    private var height: Int? = null

    fun setData(data: List<CoverSearchIssueWithDetails>?) {
        this.data = data
    }

    override fun getItemCount(): Int = data!!.size

    override fun getItemId(pos: Int): Long = pos.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoverflowViewHolder {
        height = parent.height
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return CoverflowViewHolder(ItemCoverflowBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: CoverflowViewHolder, position: Int) {
        val item = data!![position]
        holder.binding.label.text = data!![position].coverSearchIssue.coverIssueNumber

        Picasso
            .with(context)
            .load(item.coverSearchIssue.coverUrl).resize(0, height!!)
            .into(holder.binding.image, object: Callback {
                override fun onSuccess() {
                    holder.binding.progressBar.visibility = View.GONE
                    holder.binding.image.visibility = View.VISIBLE
                }

                override fun onError() {
                    holder.binding.progressBar.visibility = View.GONE
                    holder.binding.image.visibility = View.VISIBLE
                }
            })
    }
}

class CoverflowViewHolder internal constructor(val binding: ItemCoverflowBinding) :
    RecyclerView.ViewHolder(binding.root)