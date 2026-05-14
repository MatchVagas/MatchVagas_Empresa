package com.edu.matchvagasempresas.network;

import android.content.Context;

import com.edu.matchvagasempresas.util.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Use 10.0.2.2 para acessar localhost do host a partir do emulador Android.
    // Para dispositivo físico, substitua pelo IP da máquina na mesma rede.
    public static final String BASE_URL = "https://backend-tgi8.onrender.com/";

    private static ApiService apiService;

    public static synchronized ApiService getService(Context context) {
        if (apiService != null) return apiService;

        SessionManager session = new SessionManager(context);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = session.getToken();
                    Request original = chain.request();
                    Request request = token != null
                            ? original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build()
                            : original;
                    return chain.proceed(request);
                })
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
        return apiService;
    }

    public static void reset() {
        apiService = null;
    }
}
