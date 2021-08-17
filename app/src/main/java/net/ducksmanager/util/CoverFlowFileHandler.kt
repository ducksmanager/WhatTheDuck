package net.ducksmanager.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import androidx.core.content.FileProvider
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import io.sentry.Sentry
import net.ducksmanager.activity.CoverFlowActivity
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.CoverSearchResults
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.appDB
import net.ducksmanager.whattheduck.WhatTheDuck.Companion.applicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class CoverFlowFileHandler(originActivityRef: WeakReference<Activity>) {
    companion object {
        private const val MAX_COVER_DIMENSION: Double = 300.0

        lateinit var current: CoverFlowFileHandler

        var mockedRequestResource: String? = null

        private lateinit var originActivityRef: WeakReference<Activity>

        private val originActivity: Activity?
            get() = originActivityRef.get()
    }

    init {
        Companion.originActivityRef = originActivityRef
    }

    private lateinit var uploadFile: File
    internal var uploadUri: Uri? = null

    private var callback: TransformationCallback? = null
    private val target: Target = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
            try {
                val f = File.createTempFile("temp_file_name", ".jpg", applicationContext!!.cacheDir)
                val outputStream = FileOutputStream(f)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                outputStream.flush()
                outputStream.close()
                callback!!.onComplete(f)
            } catch (e: IOException) {
                e.message?.let { WhatTheDuck.alert(originActivityRef, R.string.internal_error, it) }
                Sentry.captureException(e)
            }
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            WhatTheDuck.alert(originActivityRef, R.string.internal_error, R.string.error__cannot_create_upload_file)
            callback!!.onFail()
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
    }

    fun createEmptyFileForCamera(context: Context): Uri? {
        val imagePath = File(context.filesDir, SearchFromCover.uploadTempDir)
        uploadFile = File(imagePath, SearchFromCover.uploadFileName)
        uploadFile.parentFile.mkdirs()
        try {
            if (uploadFile.exists()) {
                uploadFile.delete()
            }
            if (!uploadFile.createNewFile()) {
                WhatTheDuck.alert(originActivityRef, R.string.internal_error, context.getString(R.string.error__could_not_create_empty_file))
            }
            return FileProvider.getUriForFile(context, "net.ducksmanager.whattheduck.fileprovider", uploadFile)
        } catch (e: IOException) {
            Sentry.captureException(e)
        }
        return null
    }

    fun resizeUntilFileSize(callback: TransformationCallback?) {
        this.callback = callback
        val resizeTransformation: Transformation = object : Transformation {
            override fun transform(source: Bitmap): Bitmap {
                var result: Bitmap? = null
                val height = source.height.toDouble()
                val width = source.width.toDouble()
                if (height > MAX_COVER_DIMENSION || width > MAX_COVER_DIMENSION) {
                    result = if (height > width) {
                        Bitmap.createScaledBitmap(source, (width / (height / MAX_COVER_DIMENSION)).toInt(), MAX_COVER_DIMENSION.toInt(), true)
                    } else {
                        Bitmap.createScaledBitmap(source, MAX_COVER_DIMENSION.toInt(), (height / (width / MAX_COVER_DIMENSION)).toInt(), true)
                    }
                }
                return if (result == null) {
                    source
                } else {
                    source.recycle()
                    result
                }
            }

            override fun key(): String = "resizing to maximum accepted dimensions"
        }

        val instance: RequestCreator = if (mockedRequestResource != null) {
            Picasso.with(originActivity).load(mockedRequestResource)
        } else {
            if (uploadUri != null) {
                Picasso.with(originActivity).invalidate(uploadUri)
                Picasso.with(originActivity).load(uploadUri)
            } else {
                Picasso.with(originActivity).invalidate(uploadFile)
                Picasso.with(originActivity).load(uploadFile)
            }

        }
        instance.transform(resizeTransformation).into(target)
    }

    interface TransformationCallback {
        fun onComplete(outputFile: File)
        fun onFail()
    }

    class SearchFromCover : TransformationCallback {
        override fun onComplete(outputFile: File) {
            val requestBody: RequestBody = outputFile.asRequestBody("*/*".toMediaTypeOrNull())
            val fileToUpload = MultipartBody.Part.createFormData(uploadFileName, outputFile.name, requestBody)
            val fileName = outputFile.name.toRequestBody("text/plain".toMediaTypeOrNull())

            println("Starting cover search : " + System.currentTimeMillis())
            DmServer.api.searchFromCover(fileToUpload, fileName).enqueue(object : DmServer.Callback<CoverSearchResults>("coversearch", originActivity!!, true) {
                override fun onSuccessfulResponse(response: Response<CoverSearchResults>) {
                    println("Ending cover search : " + System.currentTimeMillis())
                    if (response.body()!!.issues.values.isEmpty()) {
                        WhatTheDuck.alert(originActivityRef, R.string.add_cover_no_results)
                        originActivityRef.get()!!.findViewById<View?>(R.id.progressBar)?.visibility = ProgressBar.GONE
                    }
                    else {
                        appDB!!.coverSearchIssueDao().deleteAll()
                        appDB!!.coverSearchIssueDao().insertList(ArrayList(response.body()!!.issues.values))
                        originActivity!!.findViewById<View>(R.id.progressBar).visibility = View.GONE
                        originActivity!!.startActivity(Intent(originActivity, CoverFlowActivity::class.java))
                    }
                }

                override fun onFailure(call: Call<CoverSearchResults>, t: Throwable) {
                    if (t.message!!.contains("exceeds your upload")) {
                        WhatTheDuck.alert(originActivityRef, R.string.add_cover_error_file_too_big)
                    }
                    WhatTheDuck.alert(originActivityRef, t.message.toString())
                }
            })
        }

        override fun onFail() {
            originActivity!!.findViewById<View>(R.id.addToCollectionWrapper).visibility = View.VISIBLE
            originActivity!!.findViewById<View>(R.id.progressBar).visibility = View.GONE
        }

        companion object {
            const val uploadTempDir = "Pictures"
            const val uploadFileName = "wtd_jpg"
        }
    }
}