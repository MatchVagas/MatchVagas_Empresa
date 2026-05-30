package com.edu.matchvagasempresas.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.edu.matchvagasempresas.data.local.database.AppDatabase;
import com.edu.matchvagasempresas.data.local.entity.CandidaturaEntity;
import com.edu.matchvagasempresas.data.remote.ApiService;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.domain.repository.CandidaturaRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class CandidaturaRepositoryImpl implements CandidaturaRepository {

    private final ApiService apiService;
    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public CandidaturaRepositoryImpl(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getService(context);
    }

    @Override
    public void loadCandidaturas(long vagaId,
                                 OnCached<List<CandidaturaEmpresaResponse>> onCached,
                                 OnFresh<List<CandidaturaEmpresaResponse>> onFresh) {
        executor.execute(() -> {
            List<CandidaturaEntity> entities =
                    AppDatabase.get(context).candidaturaDao().getByVaga(vagaId);
            List<CandidaturaEmpresaResponse> cached = CandidaturaEntity.toResponseList(entities);
            mainHandler.post(() -> {
                if (onCached != null) onCached.onCached(cached.isEmpty() ? null : cached);
            });

            apiService.candidatosPorVaga(vagaId)
                    .enqueue(new retrofit2.Callback<List<CandidaturaEmpresaResponse>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<CandidaturaEmpresaResponse>> call,
                                               @NonNull Response<List<CandidaturaEmpresaResponse>> r) {
                            if (r.isSuccessful() && r.body() != null) {
                                saveCandidaturas(vagaId, r.body());
                                if (onFresh != null)
                                    mainHandler.post(() -> onFresh.onFresh(r.body()));
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<List<CandidaturaEmpresaResponse>> call,
                                              @NonNull Throwable t) {}
                    });
        });
    }

    @Override
    public void loadTodasCandidaturas(Callback<List<CandidaturaEmpresaResponse>> callback) {
        apiService.candidaturasEmpresa()
                .enqueue(new retrofit2.Callback<List<CandidaturaEmpresaResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CandidaturaEmpresaResponse>> call,
                                           @NonNull Response<List<CandidaturaEmpresaResponse>> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            callback.onSuccess(r.body());
                        } else {
                            callback.onError("Erro ao carregar candidaturas");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<CandidaturaEmpresaResponse>> call,
                                          @NonNull Throwable t) {
                        callback.onError("Erro de conexão: " + t.getMessage());
                    }
                });
    }

    @Override
    public void detalharCandidatura(long id, Callback<CandidaturaEmpresaResponse> callback) {
        apiService.detalharCandidatura(id)
                .enqueue(new retrofit2.Callback<CandidaturaEmpresaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CandidaturaEmpresaResponse> call,
                                           @NonNull Response<CandidaturaEmpresaResponse> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            callback.onSuccess(r.body());
                        } else {
                            callback.onError("Candidatura não encontrada");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<CandidaturaEmpresaResponse> call,
                                          @NonNull Throwable t) {
                        callback.onError("Erro de conexão: " + t.getMessage());
                    }
                });
    }

    @Override
    public void atualizarStatus(long candidaturaId, long statusId,
                                Callback<CandidaturaEmpresaResponse> callback) {
        apiService.atualizarStatusCandidatura(candidaturaId, statusId)
                .enqueue(new retrofit2.Callback<CandidaturaEmpresaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CandidaturaEmpresaResponse> call,
                                           @NonNull Response<CandidaturaEmpresaResponse> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            callback.onSuccess(r.body());
                        } else {
                            callback.onError("Erro ao atualizar status");
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<CandidaturaEmpresaResponse> call,
                                          @NonNull Throwable t) {
                        callback.onError("Erro de conexão: " + t.getMessage());
                    }
                });
    }

    @Override
    public void saveCandidaturas(long vagaId, List<CandidaturaEmpresaResponse> lista) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.get(context);
            db.candidaturaDao().deleteByVaga(vagaId);
            db.candidaturaDao().insertAll(CandidaturaEntity.fromResponseList(lista));
        });
    }

    @Override
    public void invalidateCandidaturas(long vagaId) {
        executor.execute(() -> AppDatabase.get(context).candidaturaDao().deleteByVaga(vagaId));
    }
}
