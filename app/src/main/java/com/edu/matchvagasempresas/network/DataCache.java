package com.edu.matchvagasempresas.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.edu.matchvagasempresas.db.AppDatabase;
import com.edu.matchvagasempresas.db.CandidaturaEntity;
import com.edu.matchvagasempresas.db.EmpresaEntity;
import com.edu.matchvagasempresas.db.VagaEntity;
import com.edu.matchvagasempresas.model.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.model.VagaResponse;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cache local com padrão stale-while-revalidate:
 *   1. Retorna dado do banco imediatamente (sem esperar rede).
 *   2. Busca versão atualizada da API em background.
 *   3. Chama onFresh() quando o dado novo chega — o fragment decide se redesenha.
 *
 * Vagas, Candidaturas e Empresa → Room (SQLite).
 *
 * TTL de 10 minutos: dados expirados são buscados novamente, mas ainda exibidos
 * enquanto o refresh acontece para não deixar a tela vazia.
 */
public class DataCache {

    public interface OnCached<T> { void onCached(T data); }
    public interface OnFresh<T>  { void onFresh(T data); }

    private static final long TTL_MS = 10 * 60 * 1000L;

    private static DataCache instance;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private DataCache() {}

    public static DataCache get() {
        if (instance == null) instance = new DataCache();
        return instance;
    }

    // ── Vagas ─────────────────────────────────────────────────────────────────

    public void loadVagas(Context ctx,
                          OnCached<List<VagaResponse>> onCached,
                          OnFresh<List<VagaResponse>> onFresh) {

        executor.execute(() -> {
            List<VagaEntity> entities = AppDatabase.get(ctx).vagaDao().getAll();
            List<VagaResponse> cached = VagaEntity.toResponseList(entities);
            mainHandler.post(() -> {
                if (onCached != null) onCached.onCached(cached.isEmpty() ? null : cached);
            });

            ApiClient.getService(ctx).minhasVagas().enqueue(new retrofit2.Callback<List<VagaResponse>>() {
                @Override
                public void onResponse(retrofit2.Call<List<VagaResponse>> call,
                                       retrofit2.Response<List<VagaResponse>> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        saveVagas(ctx, r.body());
                        if (onFresh != null) mainHandler.post(() -> onFresh.onFresh(r.body()));
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<List<VagaResponse>> call, Throwable t) {}
            });
        });
    }

    public void saveVagas(Context ctx, List<VagaResponse> vagas) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            db.vagaDao().deleteAll();
            db.vagaDao().insertAll(VagaEntity.fromResponseList(vagas));
        });
    }

    public void invalidateVagas(Context ctx) {
        executor.execute(() -> AppDatabase.get(ctx).vagaDao().deleteAll());
    }

    public boolean vagasExpiradas(Context ctx) {
        Long cachedAt = AppDatabase.get(ctx).vagaDao().getCachedAt();
        if (cachedAt == null) return true;
        return System.currentTimeMillis() - cachedAt > TTL_MS;
    }

    // ── Candidaturas ─────────────────────────────────────────────────────────

    public void loadCandidaturas(Context ctx, long vagaId,
                                 OnCached<List<CandidaturaEmpresaResponse>> onCached,
                                 OnFresh<List<CandidaturaEmpresaResponse>> onFresh) {

        executor.execute(() -> {
            List<CandidaturaEntity> entities = AppDatabase.get(ctx).candidaturaDao().getByVaga(vagaId);
            List<CandidaturaEmpresaResponse> cached = CandidaturaEntity.toResponseList(entities);
            mainHandler.post(() -> {
                if (onCached != null) onCached.onCached(cached.isEmpty() ? null : cached);
            });

            ApiClient.getService(ctx).candidatosPorVaga(vagaId)
                    .enqueue(new retrofit2.Callback<List<CandidaturaEmpresaResponse>>() {
                        @Override
                        public void onResponse(retrofit2.Call<List<CandidaturaEmpresaResponse>> call,
                                               retrofit2.Response<List<CandidaturaEmpresaResponse>> r) {
                            if (r.isSuccessful() && r.body() != null) {
                                saveCandidaturas(ctx, vagaId, r.body());
                                if (onFresh != null) mainHandler.post(() -> onFresh.onFresh(r.body()));
                            }
                        }
                        @Override
                        public void onFailure(retrofit2.Call<List<CandidaturaEmpresaResponse>> call,
                                              Throwable t) {}
                    });
        });
    }

    public void saveCandidaturas(Context ctx, long vagaId,
                                 List<CandidaturaEmpresaResponse> lista) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            db.candidaturaDao().deleteByVaga(vagaId);
            db.candidaturaDao().insertAll(CandidaturaEntity.fromResponseList(lista));
        });
    }

    public void invalidateCandidaturas(Context ctx, long vagaId) {
        executor.execute(() -> AppDatabase.get(ctx).candidaturaDao().deleteByVaga(vagaId));
    }

    // ── Empresa ───────────────────────────────────────────────────────────────

    public void loadEmpresa(Context ctx,
                            OnCached<EmpresaResponse> onCached,
                            OnFresh<EmpresaResponse> onFresh) {

        executor.execute(() -> {
            EmpresaEntity entity = AppDatabase.get(ctx).empresaDao().get();
            EmpresaResponse cached = entity != null ? entity.toResponse() : null;
            mainHandler.post(() -> {
                if (onCached != null) onCached.onCached(cached);
            });

            ApiClient.getService(ctx).minhaEmpresa().enqueue(new retrofit2.Callback<EmpresaResponse>() {
                @Override
                public void onResponse(retrofit2.Call<EmpresaResponse> call,
                                       retrofit2.Response<EmpresaResponse> r) {
                    if (r.isSuccessful() && r.body() != null) {
                        saveEmpresa(ctx, r.body());
                        if (onFresh != null) mainHandler.post(() -> onFresh.onFresh(r.body()));
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<EmpresaResponse> call, Throwable t) {}
            });
        });
    }

    public void saveEmpresa(Context ctx, EmpresaResponse empresa) {
        executor.execute(() -> AppDatabase.get(ctx).empresaDao().insert(EmpresaEntity.fromResponse(empresa)));
    }

    public void invalidateEmpresa(Context ctx) {
        executor.execute(() -> AppDatabase.get(ctx).empresaDao().delete());
    }

    // ── Limpeza total (logout) ─────────────────────────────────────────────────

    public void clear(Context ctx) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            db.vagaDao().deleteAll();
            db.candidaturaDao().deleteAll();
            db.empresaDao().delete();
        });
    }
}
