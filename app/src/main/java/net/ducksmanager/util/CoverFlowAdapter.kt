package net.ducksmanager.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import net.ducksmanager.persistence.models.composite.CoverSearchIssueWithUserIssueAndScore
import net.ducksmanager.whattheduck.databinding.ItemCoverflowBinding

internal class CoverFlowAdapter(private val context: Context) : BaseAdapter() {
    private lateinit var binding: ItemCoverflowBinding

    private var data: List<CoverSearchIssueWithUserIssueAndScore>? = null

    fun setData(data: List<CoverSearchIssueWithUserIssueAndScore>?) {
        this.data = data
    }

    override fun getCount(): Int = data!!.size

    override fun getItem(pos: Int): Any = data!![pos]

    override fun getItemId(pos: Int): Long = pos.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView = convertView
        val viewHolder: ViewHolder

        if (rowView == null) {
            val coverFullUrl = data!![position].coverSearchIssue.coverUrl

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = ItemCoverflowBinding.inflate(inflater, parent, false)
            rowView = binding.root

            viewHolder = ViewHolder()
            viewHolder.text = binding.label

            viewHolder.image = binding.image
            viewHolder.image.tag = coverFullUrl

            viewHolder.progressBar = binding.progressBar

            Picasso
                .with(rowView.context)
                .load(coverFullUrl)
                .into(viewHolder.image, object: Callback {
                    override fun onSuccess() {
                        viewHolder.progressBar.visibility = View.GONE
                        viewHolder.image.visibility = View.VISIBLE
                    }

                    override fun onError() {
                        viewHolder.progressBar.visibility = View.GONE
                        viewHolder.image.visibility = View.VISIBLE
                    }
                })

            rowView.tag = viewHolder
        } else {
            viewHolder = rowView.tag as ViewHolder
        }
        viewHolder.text.text = data!![position].coverSearchIssue.coverIssueNumber
        return rowView
    }

    private class ViewHolder {
        lateinit var text: TextView
        lateinit var image: ImageView
        lateinit var progressBar: ProgressBar
    }
}