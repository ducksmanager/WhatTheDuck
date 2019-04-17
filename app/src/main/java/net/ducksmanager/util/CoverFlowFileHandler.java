package net.ducksmanager.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import net.ducksmanager.apigateway.DmServer;
import net.ducksmanager.persistence.models.composite.CoverSearchIssue;
import net.ducksmanager.persistence.models.composite.CoverSearchResults;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.core.content.FileProvider;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CoverFlowFileHandler {

    private static final int MAX_COVER_DIMENSION = 1000;

    public static CoverFlowFileHandler current;
    public static String mockedResource = null;
    private static WeakReference<Activity> originActivityRef;

    private File uploadFile = null;
    private TransformationCallback callback = null;

    public CoverFlowFileHandler(WeakReference<Activity> originActivityRef) {
        CoverFlowFileHandler.originActivityRef = originActivityRef;
    }

    private static WeakReference<Activity> getOriginActivityRef() {
        return originActivityRef;
    }

    private static Activity getOriginActivity() {
        return originActivityRef.get();
    }

    public Uri createEmptyFileForCamera(Context context) {
        if (uploadFile == null) {
            File imagePath = new File(context.getFilesDir(), SearchFromCover.uploadTempDir);
            uploadFile = new File(imagePath, SearchFromCover.uploadFileName);
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

    public void resizeUntilFileSize(final TransformationCallback callback) {
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
            instance = Picasso.with(getOriginActivity()).load(mockedResource);
        }
        else {
            Picasso.with(getOriginActivity()).invalidate(uploadFile);
            instance = Picasso.with(getOriginActivity()).load(uploadFile);
        }

        instance.transform(resizeTransformation).into(new CompressPhotoTarget());
    }

    interface TransformationCallback {
        void onComplete(File outputFile);
        void onFail();
    }

    class CompressPhotoTarget implements Target {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            try {
                FileOutputStream ostream = new FileOutputStream(uploadFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                ostream.flush();
                ostream.close();

                callback.onComplete(uploadFile);
            } catch (IOException e) {
                WhatTheDuck.wtd.alert(getOriginActivityRef(), R.string.internal_error);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            WhatTheDuck.wtd.alert(getOriginActivityRef(), R.string.internal_error);
            callback.onFail();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    public static class SearchFromCover implements TransformationCallback {
        static final String uploadTempDir = "Pictures";
        static final String uploadFileName = "wtd_jpg";

        public SearchFromCover() {
        }

        @Override
        public void onComplete(File file) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);
            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData(uploadFileName, file.getName(), requestBody);
            RequestBody fileName = RequestBody.create(MediaType.parse("text/plain"), file.getName());

            System.out.println("Starting cover search : " + System.currentTimeMillis());
            DmServer.api.searchFromCover(fileToUpload, fileName).enqueue(new DmServer.Callback<CoverSearchResults>("coversearch", getOriginActivity()) {
                @Override
                public void onSuccessfulResponse(Response<CoverSearchResults> response) {
                    System.out.println("Ending cover search : " + System.currentTimeMillis());

                    if (response.body().getIssues() != null) {
                        for (CoverSearchIssue issue : response.body().getIssues().values()) {
                            issue.setCoverFullUrl(WhatTheDuckApplication.config.getProperty(WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL) + "/cover-id/download/" + issue.getCoverId());
                        }
                        WhatTheDuck.appDB.coverSearchIssueDao().deleteAll();
                        WhatTheDuck.appDB.coverSearchIssueDao().insertList(new ArrayList<>(response.body().getIssues().values()));
                        getOriginActivity().startActivity(new Intent(getOriginActivity(), CoverFlowActivity.class));
                    }
                    else {
                        if (response.body().getType() != null) {
                            switch(response.body().getType()) {
                                case "SEARCH_RESULTS":
                                    WhatTheDuck.wtd.alert(new WeakReference<>(getOriginActivity()), R.string.add_cover_no_results);
                                    break;
                                default:
                                    WhatTheDuck.wtd.alert(new WeakReference<>(getOriginActivity()), response.body().getType());
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull Throwable t) {
                    if (t.getMessage().contains("exceeds your upload")) {
                        WhatTheDuck.wtd.alert(new WeakReference<>(getOriginActivity()), R.string.add_cover_error_file_too_big);
                    }
                    else {
                        WhatTheDuck.wtd.alert(new WeakReference<>(getOriginActivity()), R.string.internal_error);
                    }
                }
            });
        }

        @Override
        public void onFail() {
            getOriginActivity().findViewById(R.id.addToCollectionWrapper).setVisibility(VISIBLE);
            getOriginActivity().findViewById(R.id.progressBar).setVisibility(GONE);
        }
    }
}
