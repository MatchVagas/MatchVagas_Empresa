package com.edu.matchvagasempresas.network;

import android.content.Context;

import com.edu.matchvagasempresas.BuildConfig;
import com.edu.matchvagasempresas.util.SessionManager;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static final String BASE_URL = "https://backend-tgi8.onrender.com";

    private static final long CACHE_SIZE = 5L * 1024 * 1024; // 5 MB
    private static final int  CONNECT_TIMEOUT = 10;
    private static final int  READ_TIMEOUT    = 30;
    private static final int  WRITE_TIMEOUT   = 30;

    private static ApiService apiService;

    public static synchronized ApiService getService(Context context) {
        if (apiService != null) return apiService;

        SessionManager session = new SessionManager(context);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT,    TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT,  TimeUnit.SECONDS)
                .cache(new Cache(new File(context.getCacheDir(), "http_cache"), CACHE_SIZE))
                // Application interceptor: adiciona o token nas chamadas ao nosso backend.
                .addInterceptor(chain -> {
                    String token = session.getToken();
                    Request original = chain.request();
                    if (token == null) return chain.proceed(original);
                    return chain.proceed(original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build());
                });

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }

        apiService = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);

        return apiService;
    }

    public static void reset() {
        apiService = null;
    }
}
