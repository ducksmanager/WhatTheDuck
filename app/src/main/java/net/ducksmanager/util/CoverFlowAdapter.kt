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
import net.ducksmanager.whattheduck.R

internal class CoverFlowAdapter(private val context: Context) : BaseAdapter() {
    private var data: List<CoverSearchIssueWithUserIssueAndScore>? = null

    fun setData(data: List<CoverSearchIssueWithUserIssueAndScore>?) {
        this.data = data
    }

    override fun getCount(): Int {
        return data!!.size
    }

    override fun getItem(pos: Int): Any {
        return data!![pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var rowView = convertView
        val viewHolder: ViewHolder

        if (rowView == null) {
            val coverFullUrl = data!![position].coverSearchIssue.coverUrl

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.item_coverflow, parent, false)
            viewHolder = ViewHolder()
            viewHolder.text = rowView.findViewById(R.id.label)

            viewHolder.image = rowView.findViewById(R.id.image)
            viewHolder.image.tag = coverFullUrl

            viewHolder.progressBar = rowView.findViewById(R.id.progressBar)

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