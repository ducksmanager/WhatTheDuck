package net.ducksmanager.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import net.ducksmanager.retrievetasks.CoverSearch;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.ORB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CoverFlowFileHandler {

    private static final int MAX_COVER_DIMENSION = 1000;

    public static CoverFlowFileHandler current;

    public static String mockedResource = null;

    public interface TransformationCallback {
        void onCompleteDescriptors(MatOfKeyPoint keypoints, Mat descriptors);

        void onCompleteRawImage(File outputFile);

        void onFail();
    }

    private File uploadFile = null;

    private final Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (WhatTheDuck.isOpenCvLoaded) {
                Mat img = new Mat();
                Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Utils.bitmapToMat(bmp32, img);
                ORB orb;
                if (!img.empty()) {
                    orb = ORB.create(2000, 1.02f, 100);
                    MatOfKeyPoint keypoints = new MatOfKeyPoint();
                    Mat descriptors = new Mat();
                    orb.detectAndCompute(img, new Mat(), keypoints, descriptors);

                    callback.onCompleteDescriptors(keypoints, descriptors);
                }
            }
            // Fallback : send raw image
            else {
                try {
                    FileOutputStream ostream = new FileOutputStream(uploadFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                    ostream.flush();
                    ostream.close();

                    callback.onCompleteRawImage(uploadFile);
                } catch (IOException e) {
                    WhatTheDuck.wtd.alert(CoverSearch.originActivityRef, R.string.internal_error);
                }
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            WhatTheDuck.wtd.alert(CoverSearch.originActivityRef, R.string.internal_error);
            callback.onFail();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    private TransformationCallback callback = null;

    public CoverFlowFileHandler() {
    }

    public Uri createEmptyFileForCamera(Context context) {
        if (uploadFile == null) {
            File imagePath = new File(context.getFilesDir(), CoverSearch.uploadTempDir);
            uploadFile = new File(imagePath, CoverSearch.uploadFileName);
        }
        uploadFile.getParentFile().mkdirs();
        try {
            if (uploadFile.exists()) {
                uploadFile.delete();
            }
            if (!uploadFile.createNewFile()) {
                WhatTheDuck.wtd.alert(R.string.internal_error);
            }
            return FileProvider.getUriForFile(context, "net.ducksmanager.whattheduck.fileprovider", uploadFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void resizeUntilFileSize(final Activity activity, final TransformationCallback callback) {
        this.callback = callback;

        RequestCreator instance;
        Transformation resizeTransformation = new Transformation() {

            @Override
            public Bitmap transform(Bitmap source) {
                Bitmap result = null;
                int height = source.getHeight();
                int width = source.getWidth();
                if (height > MAX_COVER_DIMENSION || width > MAX_COVER_DIMENSION) {
                    if (height > width) {
                        result = Bitmap.createScaledBitmap(source, width / (height/MAX_COVER_DIMENSION), MAX_COVER_DIMENSION, false);
                    } else {
                        result = Bitmap.createScaledBitmap(source, MAX_COVER_DIMENSION, height / (width/MAX_COVER_DIMENSION),false);
                    }
                }

                if (result == null) {
                    return source;
                }
                else {
                    source.recycle();
                    return result;
                }
            }

            @Override
            public String key() {
                return "resizing to maximum accepted dimensions";
            }
        };

        if (mockedResource != null) {
            instance = Picasso.with(activity).load(mockedResource);
        }
        else {
            Picasso.with(activity).invalidate(uploadFile);
            instance = Picasso.with(activity).load(uploadFile);
        }

        instance.transform(resizeTransformation).into(target);
    }
}
