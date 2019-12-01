package net.ducksmanager.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import net.ducksmanager.api.DmServer
import net.ducksmanager.persistence.models.composite.CoverSearchResults
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck
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

class CoverFlowFileHandler(originActivityRef: WeakReference<Activity>?) {
    companion object {
        private const val MAX_COVER_DIMENSION = 1000

        lateinit var current: CoverFlowFileHandler

        @JvmField
        var mockedResource: String? = null

        private var originActivityRef: WeakReference<Activity>? = null

        private val originActivity: Activity?
            get() = originActivityRef!!.get()
    }

    init {
        Companion.originActivityRef = originActivityRef
    }

    private var uploadFile: File? = null
    private var callback: TransformationCallback? = null
    private val target: Target = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
            try {
                val ostream = FileOutputStream(uploadFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream)
                ostream.flush()
                ostream.close()
                callback!!.onComplete(uploadFile)
            } catch (e: IOException) {
                WhatTheDuck.alert(originActivityRef, R.string.internal_error)
            }
        }

        override fun onBitmapFailed(errorDrawable: Drawable?) {
            WhatTheDuck.alert(originActivityRef, R.string.internal_error)
            callback!!.onFail()
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
    }

    fun createEmptyFileForCamera(context: Context): Uri? {
        if (uploadFile == null) {
            val imagePath = File(context.filesDir, SearchFromCover.uploadTempDir)
            uploadFile = File(imagePath, SearchFromCover.uploadFileName)
        }
        uploadFile!!.parentFile.mkdirs()
        try {
            if (uploadFile!!.exists()) {
                uploadFile!!.delete()
            }
            if (!uploadFile!!.createNewFile()) {
                WhatTheDuck.alert(originActivityRef, R.string.internal_error)
            }
            return FileProvider.getUriForFile(context, "net.ducksmanager.whattheduck.fileprovider", uploadFile!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun resizeUntilFileSize(callback: TransformationCallback?) {
        this.callback = callback
        val resizeTransformation: Transformation = object : Transformation {
            override fun transform(source: Bitmap): Bitmap {
                var result: Bitmap? = null
                val height = source.height
                val width = source.width
                if (height > MAX_COVER_DIMENSION || width > MAX_COVER_DIMENSION) {
                    result = if (height > width) {
                        Bitmap.createScaledBitmap(source, width / (height / MAX_COVER_DIMENSION), MAX_COVER_DIMENSION, false)
                    } else {
                        Bitmap.createScaledBitmap(source, MAX_COVER_DIMENSION, height / (width / MAX_COVER_DIMENSION), false)
                    }
                }
                return if (result == null) {
                    source
                } else {
                    source.recycle()
                    result
                }
            }

            override fun key(): String {
                return "resizing to maximum accepted dimensions"
            }
        }

        val instance: RequestCreator = if (mockedResource != null) {
            Picasso.with(originActivity).load(mockedResource)
        } else {
            Picasso.with(originActivity).invalidate(uploadFile)
            Picasso.with(originActivity).load(uploadFile)
        }
        instance.transform(resizeTransformation).into(target)
    }

    interface TransformationCallback {
        fun onComplete(outputFile: File?)
        fun onFail()
    }

    class SearchFromCover : TransformationCallback {
        override fun onComplete(outputFile: File?) {
            if (outputFile == null) {
                return
            }
            val requestBody: RequestBody = outputFile.asRequestBody("*/*".toMediaTypeOrNull())
            val fileToUpload = MultipartBody.Part.createFormData(uploadFileName, outputFile.name, requestBody)
            val fileName = outputFile.name.toRequestBody("text/plain".toMediaTypeOrNull())

            println("Starting cover search : " + System.currentTimeMillis())
            DmServer.api.searchFromCover(fileToUpload, fileName).enqueue(object : DmServer.Callback<CoverSearchResults>("coversearch", originActivity!!, true) {
                override fun onSuccessfulResponse(response: Response<CoverSearchResults>) {
                    println("Ending cover search : " + System.currentTimeMillis())
                    if (response.body()!!.issues.values.isEmpty()) {
                        WhatTheDuck.alert(WeakReference(originActivity), R.string.add_cover_no_results)
                    }
                    else {
                        WhatTheDuck.appDB.coverSearchIssueDao().deleteAll()
                        WhatTheDuck.appDB.coverSearchIssueDao().insertList(ArrayList(response.body()!!.issues.values))
                        originActivity!!.startActivity(Intent(originActivity, CoverFlowActivity::class.java))
                    }
                }

                override fun onFailure(call: Call<CoverSearchResults>, t: Throwable) {
                    WhatTheDuck.alert(
                        WeakReference(originActivity),
                        if (t.message!!.contains("exceeds your upload"))
                            R.string.add_cover_error_file_too_big
                        else
                            R.string.internal_error
                    )
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