package com.edu.matchvagasempresas.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.edu.matchvagasempresas.data.local.database.AppDatabase;
import com.edu.matchvagasempresas.data.local.entity.VagaEntity;
import com.edu.matchvagasempresas.data.remote.ApiService;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.edu.matchvagasempresas.data.remote.dto.VagaRequest;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;
import com.edu.matchvagasempresas.domain.repository.VagaRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class VagaRepositoryImpl implements VagaRepository {

    private final ApiService apiService;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public VagaRepositoryImpl(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getService(context);
    }

    @Override
    public void loadVagas(OnCached<List<VagaResponse>> onCached, OnFresh<List<VagaResponse>> onFresh) {
        executor.execute(() -> {
            List<VagaEntity> entities = AppDatabase.get(context).vagaDao().getAll();
            List<VagaResponse> cached = VagaEntity.toResponseList(entities);
            mainHandler.post(() -> {
                if (onCached != null) onCached.onCached(cached.isEmpty() ? null : cached);
            });

            apiService.minhasVagas().enqueue(new retrofit2.Callback<List<VagaResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<VagaResponse>> call,
                                       @NonNull Response<List<VagaResponse>> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        saveVagas(r.body());
                        if (onFresh != null) mainHandler.post(() -> onFresh.onFresh(r.body()));
                    }
                }
                @Override
                public void onFailure(@NonNull Call<List<VagaResponse>> call, @NonNull Throwable t) {}
            });
        });
    }

    @Override
    public void buscarVaga(long id, Callback<VagaResponse> callback) {
        apiService.buscarVaga(id).enqueue(new retrofit2.Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call,
                                   @NonNull Response<VagaResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    callback.onSuccess(r.body());
                } else {
                    callback.onError("Erro ao buscar vaga");
                }
            }
            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }

    @Override
    public void criarVaga(VagaRequest request, Callback<VagaResponse> callback) {
        apiService.criarVaga(request).enqueue(new retrofit2.Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call,
                                   @NonNull Response<VagaResponse> r) {
                if (r.isSuccessful()) {
                    invalidateVagas();
                    callback.onSuccess(r.body());
                } else {
                    callback.onError(buildErroHttp(r));
                }
            }
            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }

    @Override
    public void atualizarVaga(long id, VagaRequest request, Callback<VagaResponse> callback) {
        apiService.atualizarVaga(id, request).enqueue(new retrofit2.Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call,
                                   @NonNull Response<VagaResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    invalidateVagas();
                    callback.onSuccess(r.body());
                } else {
                    callback.onError(buildErroHttp(r));
                }
            }
            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }

    @Override
    public void deletarVaga(long id, SimpleCallback callback) {
        apiService.deletarVaga(id).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> r) {
                if (r.isSuccessful()) {
                    invalidateVagas();
                    callback.onSuccess();
                } else {
                    callback.onError("Erro ao deletar vaga (código " + r.code() + ")");
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }

    @Override
    public void saveVagas(List<VagaResponse> vagas) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.get(context);
            db.vagaDao().deleteAll();
            db.vagaDao().insertAll(VagaEntity.fromResponseList(vagas));
        });
    }

    @Override
    public void invalidateVagas() {
        executor.execute(() -> AppDatabase.get(context).vagaDao().deleteAll());
    }

    private String buildErroHttp(Response<?> r) {
        switch (r.code()) {
            case 400: return "Dados inválidos. Verifique os campos preenchidos.";
            case 401: return "Sessão expirada. Faça login novamente.";
            case 403: return "Você não tem permissão para esta ação.";
            case 409: return "Já existe uma vaga com essas informações.";
            case 500: return "Erro interno no servidor. Tente novamente mais tarde.";
            default:  return "Erro ao processar a vaga (código " + r.code() + ")";
        }
    }
}
