package net.ducksmanager.util


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import net.ducksmanager.whattheduck.IssueWithFullUrl
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList
import java.util.HashMap

internal class CoverFlowAdapter(private val mContext: Context) : BaseAdapter() {

    private var mData = ArrayList<IssueWithFullUrl>(0)

    fun setData(data: ArrayList<IssueWithFullUrl>) {
        mData = data
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(pos: Int): Any {
        return mData[pos]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {

        var rowView: View? = convertView

        val viewHolder: ViewHolder
        if (rowView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowView = inflater.inflate(R.layout.item_coverflow, parent, false)

            viewHolder = ViewHolder()
            viewHolder.text = rowView!!.findViewById(R.id.label)
            viewHolder.image = rowView.findViewById(R.id.image)
            viewHolder.image!!.tag = mData[position].fullUrl
            viewHolder.progressBar = rowView.findViewById(R.id.progressBar)

            val task = DownloadImagesTask(viewHolder.progressBar)
            task.execute(viewHolder.image)

            rowView.tag = viewHolder
        } else {
            viewHolder = rowView.tag as ViewHolder
        }

        viewHolder.text!!.text = mData[position].issueNumber

        return rowView
    }


    private class ViewHolder {
        internal var text: TextView? = null
        internal var image: ImageView? = null
        internal var progressBar: ProgressBar? = null
    }

    private class DownloadImagesTask internal constructor(progressBar: ProgressBar) : AsyncTask<ImageView, Void, Bitmap>() {

        internal val progressBarRef: WeakReference<ProgressBar>
        internal var imageViewRef: WeakReference<ImageView>? = null

        init {
            this.progressBarRef = WeakReference(progressBar)
        }

        override fun doInBackground(vararg imageViews: ImageView): Bitmap? {
            this.imageViewRef = WeakReference(imageViews[0])

            return downloadImage(imageViewRef!!.get().getTag() as String)
        }

        override fun onPostExecute(result: Bitmap) {
            val imageView = imageViewRef!!.get()
            imageView.setVisibility(View.VISIBLE)
            imageView.setImageBitmap(result)
            progressBarRef.get().setVisibility(View.GONE)
        }

        private fun downloadImage(url: String): Bitmap? {
            if (imageCache.containsKey(url)) {
                return imageCache[url]
            }

            val bmp: Bitmap?
            try {
                val ulrn = URL(url)
                val con = ulrn.openConnection() as HttpURLConnection
                con.setRequestProperty("X-Dm-Version", WhatTheDuck.wtd!!.applicationVersion)

                val `is` = con.inputStream
                val bos = ByteArrayOutputStream()
                val tmpBuffer = ByteArray(256)
                var n: Int
                while ((n = `is`.read(tmpBuffer)) >= 0) {
                    bos.write(tmpBuffer, 0, n)
                }

                bmp = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, bos.toByteArray().size)
                if (null != bmp) {
                    imageCache[url] = bmp
                    return bmp
                }

            } catch (e: Exception) {
                System.err.println(e.message)
            }

            return null
        }
    }

    companion object {

        private val imageCache = HashMap<String, Bitmap>()
    }
}

