package net.ducksmanager.apigateway;

import android.app.Activity;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ducksmanager.whattheduck.R;
import net.ducksmanager.whattheduck.WhatTheDuck;

import org.jetbrains.annotations.NotNull;

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

import static net.ducksmanager.whattheduck.WhatTheDuck.trackEvent;
import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_API_ENDPOINT_URL;
import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_ROLE_NAME;
import static net.ducksmanager.whattheduck.WhatTheDuckApplication.CONFIG_KEY_ROLE_PASSWORD;
import static net.ducksmanager.whattheduck.WhatTheDuckApplication.config;

public class DmServer {
    public static DmServerApi api;
    public static String apiDmUser;
    public static String apiDmPassword;

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
                    .header("Authorization", Credentials.basic(config.getProperty(CONFIG_KEY_ROLE_NAME), config.getProperty(CONFIG_KEY_ROLE_PASSWORD)))
                    .header("x-dm-version", WhatTheDuck.wtd.getApplicationVersion());

                if (getApiDmUser() != null) {
                    builder
                        .header("x-dm-user", getApiDmUser())
                        .header("x-dm-pass", getApiDmPassword());
                }

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            })
            .addInterceptor(interceptor)
            .build();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(config.getProperty(CONFIG_KEY_API_ENDPOINT_URL))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

        api = retrofit.create(DmServerApi.class);
    }

    private static String getApiDmUser() {
        return apiDmUser;
    }

    public static void setApiDmUser(String apiDmUser) {
        DmServer.apiDmUser = apiDmUser;
    }

    private static String getApiDmPassword() {
        return apiDmPassword;
    }

    public static void setApiDmPassword(String apiDmPassword) {
        DmServer.apiDmPassword = apiDmPassword;
    }

    public abstract static class Callback<T> implements retrofit2.Callback<T> {

        private final String eventName;
        private final WeakReference<Activity> originActivityRef;

        protected Callback(String eventName, Activity originActivity) {
            this.eventName = eventName;
            trackEvent(eventName + "/start");

            this.originActivityRef = new WeakReference<>(originActivity);
            if (originActivityRef.get().findViewById(R.id.progressBar) != null) {
                originActivityRef.get().findViewById(R.id.progressBar).setVisibility(ProgressBar.VISIBLE);
            }
        }

        public abstract void onSuccessfulResponse(Response<T> response);

        @Override
        public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response) {
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
        public void onFailure(@NotNull Call call, @NotNull Throwable t) {
            WhatTheDuck.wtd.alert(originActivityRef, R.string.error);
            System.err.println(t.getMessage());
            onFinished();
        }

        private void onFinished() {
            trackEvent(eventName + "/finish");
            if (originActivityRef.get().findViewById(R.id.progressBar) != null) {
                originActivityRef.get().findViewById(R.id.progressBar).setVisibility(ProgressBar.GONE);
            }
        }
    }
}
