package com.edu.matchvagasempresas.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.edu.matchvagasempresas.data.local.database.AppDatabase;
import com.edu.matchvagasempresas.data.local.entity.EmpresaEntity;
import com.edu.matchvagasempresas.data.remote.ApiService;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaRequest;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;
import com.edu.matchvagasempresas.domain.repository.EmpresaRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Response;

public class EmpresaRepositoryImpl implements EmpresaRepository {

    private final ApiService apiService;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public EmpresaRepositoryImpl(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getService(context);
    }

    @Override
    public void loadEmpresa(OnCached<EmpresaResponse> onCached, OnFresh<EmpresaResponse> onFresh) {
        executor.execute(() -> {
            EmpresaEntity entity = AppDatabase.get(context).empresaDao().get();
            EmpresaResponse cached = entity != null ? entity.toResponse() : null;
            mainHandler.post(() -> { if (onCached != null) onCached.onCached(cached); });

            apiService.minhaEmpresa().enqueue(new retrofit2.Callback<EmpresaResponse>() {
                @Override
                public void onResponse(@NonNull Call<EmpresaResponse> call,
                                       @NonNull Response<EmpresaResponse> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        saveEmpresa(r.body());
                        if (onFresh != null) mainHandler.post(() -> onFresh.onFresh(r.body()));
                    }
                }
                @Override
                public void onFailure(@NonNull Call<EmpresaResponse> call, @NonNull Throwable t) {}
            });
        });
    }

    @Override
    public void atualizarEmpresa(Long id, EmpresaRequest request, Callback<EmpresaResponse> callback) {
        apiService.atualizarEmpresa(id, request).enqueue(new retrofit2.Callback<EmpresaResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmpresaResponse> call,
                                   @NonNull Response<EmpresaResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    saveEmpresa(r.body());
                    callback.onSuccess(r.body());
                } else {
                    callback.onError("Erro ao salvar (código " + r.code() + ")");
                }
            }
            @Override
            public void onFailure(@NonNull Call<EmpresaResponse> call, @NonNull Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }

    @Override
    public void uploadLogo(MultipartBody.Part arquivo, Callback<EmpresaResponse> callback) {
        apiService.uploadLogo(arquivo).enqueue(new retrofit2.Callback<EmpresaResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmpresaResponse> call,
                                   @NonNull Response<EmpresaResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    saveEmpresa(r.body());
                    callback.onSuccess(r.body());
                } else {
                    callback.onError("Erro ao enviar logo (código " + r.code() + ")");
                }
            }
            @Override
            public void onFailure(@NonNull Call<EmpresaResponse> call, @NonNull Throwable t) {
                callback.onError("Falha de conexão");
            }
        });
    }

    @Override
    public void saveEmpresa(EmpresaResponse empresa) {
        executor.execute(() ->
                AppDatabase.get(context).empresaDao().insert(EmpresaEntity.fromResponse(empresa)));
    }

    @Override
    public void invalidateEmpresa() {
        executor.execute(() -> AppDatabase.get(context).empresaDao().delete());
    }
}
