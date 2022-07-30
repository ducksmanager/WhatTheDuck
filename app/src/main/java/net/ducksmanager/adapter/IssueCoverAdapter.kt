package net.ducksmanager.adapter

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.tomg.exifinterfaceextended.ExifInterfaceExtended
import net.ducksmanager.activity.ItemList
import net.ducksmanager.persistence.models.coa.InducksIssueWithCoverUrl
import net.ducksmanager.persistence.models.composite.InducksIssueWithUserData
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import okhttp3.*
import okio.BufferedSink
import okio.IOException
import okio.buffer
import okio.sink
import java.io.*
import kotlin.math.sqrt


class IssueCoverAdapter internal constructor(
    itemList: ItemList<InducksIssueWithUserData>,
    private val recyclerView: RecyclerView
) : ItemAdapter<InducksIssueWithUserData>(itemList, R.layout.cell_cover) {

    companion object {
        fun getCoverUrl(i: InducksIssueWithCoverUrl): String {
            return String.format(
                "%s/%s",
                WhatTheDuck.config.getProperty(WhatTheDuck.CONFIG_KEY_COVERS_URL),
                i.coverUrl
            )
        }
    }

    override fun getViewHolder(v: View?) = ViewHolder(v)

    override val onClickListener: View.OnClickListener? = null

    inner class ViewHolder(v: View?) : ItemAdapter<InducksIssueWithUserData>.ViewHolder(v!!) {
        val coverImage: ImageView = v!!.findViewById(R.id.coverimage)
        val defaultCover: TextView = v!!.findViewById(R.id.defaultcover)
    }

    override fun onBindViewHolder(holder: ItemAdapter<InducksIssueWithUserData>.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val itemHolder = holder as ViewHolder
        itemHolder.itemView.background = null

        downloadAndShow(getItem(position), itemHolder, holder)
    }

    private fun downloadAndShow(
        item: InducksIssueWithUserData,
        itemHolder: ViewHolder,
        holder: ViewHolder
    ) {
        val itemWidth = (recyclerView.measuredWidth / (recyclerView.layoutManager as GridLayoutManager).spanCount)
        val request: Request = Request.Builder().url(getCoverUrl(item.issue)).build()
        OkHttpClient().newCall(request).enqueue(object : Callback, okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                TODO("Not yet implemented")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.body !is ResponseBody) {
                    return
                }
                val imageName = "cover-${item.issue.inducksIssueNumber}.jpg"
                val file = File.createTempFile(imageName, null, holder.itemView.context.cacheDir)
                var file2 = File.createTempFile(imageName, null, holder.itemView.context.cacheDir)
                val sink: BufferedSink = file.sink().buffer()
                sink.writeAll(response.body!!.source())
                sink.close()

                val exifInterfaceExtended = ExifInterfaceExtended(file)
                val inputStream: InputStream = FileInputStream(file)
                val outputStream: OutputStream = FileOutputStream(file2)
                try { // Remove metadata from image to prevent https://github.com/square/picasso/issues/364 on some images
                    exifInterfaceExtended.saveExclusive(inputStream, outputStream, true)
                    inputStream.close()
                    outputStream.close()
                }
                catch(e: java.io.IOException) { // Cannot detect MIME type
                    file2 = file
                }

                val uiHandler = Handler(Looper.getMainLooper())
                uiHandler.post {
                    Picasso
                        .with(holder.itemView.context)
                        .load(file2)
                        .resize(0, itemWidth)
                        .into(itemHolder.coverImage, object : Callback {
                            override fun onSuccess() {
                                itemHolder.defaultCover.visibility = View.GONE
                                file.delete()
                                if (file !== file2) {
                                    file2.delete()
                                }
                            }

                            override fun onError() {
                                showDefaultCover()
                            }
                        })
                }
            }

            override fun onSuccess() {
                TODO("Not yet implemented")
            }

            override fun onError() {
                showDefaultCover()
            }

            private fun showDefaultCover() {
                itemHolder.defaultCover.minHeight = (itemWidth * sqrt(2.0)).toInt()
                itemHolder.defaultCover.text =
                    recyclerView.context.getString(R.string.issue_no_cover)
                        .format(item.issue.inducksIssueNumber)
                itemHolder.defaultCover.visibility = View.VISIBLE
            }
        })

    }

    override fun isPossessed(item: InducksIssueWithUserData) = item.userIssue != null

    override fun getCheckboxImageResource(i: InducksIssueWithUserData, activity: Activity): Int? = null

    override fun getPrefixImageResource(i: InducksIssueWithUserData, activity: Activity): Int? = null

    override fun getSuffixImageResource(i: InducksIssueWithUserData): Int? = null

    override fun getDescriptionText(i: InducksIssueWithUserData) : String? = null

    override fun getSuffixText(i: InducksIssueWithUserData): String? = null

    override fun getIdentifier(i: InducksIssueWithUserData): String? = null

    override fun getText(i: InducksIssueWithUserData): String = i.issue.inducksIssueNumber

    override fun getComparatorText(i: InducksIssueWithUserData): String = i.issue.inducksIssueNumber
}