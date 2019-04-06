package net.ducksmanager.apigateway;

import android.app.Activity;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ducksmanager.util.Settings;
import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;
import net.ducksmanager.whattheduck.WhatTheDuckApplication;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL;
import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_ROLE_NAME;
import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_ROLE_PASSWORD;

public class DmServer {
    public static DmServerApi api;

    public static void initApi() {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .addInterceptor(chain -> {
                Request originalRequest = chain.request();

                Request.Builder builder = originalRequest.newBuilder()
                    .header("Authorization", Credentials.basic(WhatTheDuckApplication.config.getProperty(CONFIG_KEY_ROLE_NAME), WhatTheDuckApplication.config.getProperty(CONFIG_KEY_ROLE_PASSWORD)))
                    .header("x-dm-version", WhatTheDuck.wtd.getApplicationVersion())
                    .header("x-dm-user", Settings.getUsername())
                    .header("x-dm-pass", Settings.getEncryptedPassword());

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            })
            .addInterceptor(interceptor)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(WhatTheDuckApplication.config.getProperty(CONFIG_KEY_API_ENDPOINT_URL))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

        api = retrofit.create(DmServerApi.class);
    }

    public abstract static class Callback<T> implements retrofit2.Callback<T> {

        private WeakReference<Activity> originActivityRef;

        protected Callback(Activity originActivity) {
            this.originActivityRef = new WeakReference<>(originActivity);
        }

        public abstract void onSuccessfulResponse(Response<T> response);

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (response.isSuccessful()) {
                this.onSuccessfulResponse(response);
            }
            else {
                switch(response.code()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        WhatTheDuck.wtd.alert(originActivityRef, R.string.input_error__invalid_credentials);
                    default:
                        WhatTheDuck.wtd.alert(originActivityRef, R.string.error);
                }
            }
            onFinished();
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            System.err.println(t.getMessage());
            onFinished();
        }

        private void onFinished() {
            if (originActivityRef.get().findViewById(R.id.progressBar) != null) {
                originActivityRef.get().findViewById(R.id.progressBar).setVisibility(ProgressBar.GONE);
            }
        }
    }
}
