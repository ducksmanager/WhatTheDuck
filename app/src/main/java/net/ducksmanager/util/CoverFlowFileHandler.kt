package net.ducksmanager.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v4.content.FileProvider

import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation

import net.ducksmanager.retrievetasks.CoverSearch
import net.ducksmanager.whattheduck.R
import net.ducksmanager.whattheduck.WhatTheDuck

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CoverFlowFileHandler {

    private var uploadFile: File? = null

    private val target = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            try {
                val ostream = FileOutputStream(uploadFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream)
                ostream.flush()
                ostream.close()

                callback!!.onComplete(uploadFile)
            } catch (e: IOException) {
                WhatTheDuck.wtd!!.alert(CoverSearch.originActivityRef, R.string.internal_error)
            }

        }

        override fun onBitmapFailed(errorDrawable: Drawable) {
            WhatTheDuck.wtd!!.alert(CoverSearch.originActivityRef, R.string.internal_error)
            callback!!.onFail()
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable) {

        }
    }

    private var callback: TransformationCallback? = null

    interface TransformationCallback {
        fun onComplete(outputFile: File?)

        fun onFail()
    }

    fun createEmptyFileForCamera(context: Context): Uri? {
        if (uploadFile == null) {
            val imagePath = File(context.filesDir, CoverSearch.uploadTempDir)
            uploadFile = File(imagePath, CoverSearch.uploadFileName)
        }
        uploadFile!!.parentFile.mkdirs()
        try {
            if (uploadFile!!.exists()) {
                uploadFile!!.delete()
            }
            if (!uploadFile!!.createNewFile()) {
                WhatTheDuck.wtd!!.alert(R.string.internal_error)
            }
            return FileProvider.getUriForFile(context, "net.ducksmanager.whattheduck.fileprovider", uploadFile!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    fun resizeUntilFileSize(activity: Activity, callback: TransformationCallback) {
        this.callback = callback

        val instance: RequestCreator
        val resizeTransformation = object : Transformation {

            override fun transform(source: Bitmap): Bitmap {
                var result: Bitmap? = null
                val height = source.height
                val width = source.width
                if (height > MAX_COVER_DIMENSION || width > MAX_COVER_DIMENSION) {
                    if (height > width) {
                        result = Bitmap.createScaledBitmap(source, width / (height / MAX_COVER_DIMENSION), MAX_COVER_DIMENSION, false)
                    } else {
                        result = Bitmap.createScaledBitmap(source, MAX_COVER_DIMENSION, height / (width / MAX_COVER_DIMENSION), false)
                    }
                }

                if (result == null) {
                    return source
                } else {
                    source.recycle()
                    return result
                }
            }

            override fun key(): String {
                return "resizing to maximum accepted dimensions"
            }
        }

        if (mockedResource != null) {
            instance = Picasso.with(activity).load(mockedResource)
        } else {
            Picasso.with(activity).invalidate(uploadFile!!)
            instance = Picasso.with(activity).load(uploadFile)
        }

        instance.transform(resizeTransformation).into(target)
    }

    companion object {

        private val MAX_COVER_DIMENSION = 1000

        var current: CoverFlowFileHandler? = null

        var mockedResource: String? = null
    }
}
