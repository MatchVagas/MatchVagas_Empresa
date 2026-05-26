package com.edu.matchvagasempresas.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.edu.matchvagasempresas.db.AppDatabase;
import com.edu.matchvagasempresas.db.LookupEntity;
import com.edu.matchvagasempresas.model.LookupItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LookupCache {

    public interface OnReady { void onReady(); }

    private static LookupCache instance;

    private final List<LookupItem> portes        = new ArrayList<>();
    private final List<LookupItem> ramos          = new ArrayList<>();
    private final List<LookupItem> tiposVaga      = new ArrayList<>();
    private final List<LookupItem> modalidades    = new ArrayList<>();
    private final List<LookupItem> escolaridades  = new ArrayList<>();
    private final List<LookupItem> cidades        = new ArrayList<>();
    private final List<LookupItem> statusVaga     = new ArrayList<>();

    private boolean loading = false;
    private boolean loaded  = false;
    private final List<OnReady> pending = new ArrayList<>();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LookupCache() {}

    public static LookupCache get() {
        if (instance == null) instance = new LookupCache();
        return instance;
    }

    public static void clear(Context ctx) {
        if (instance != null) {
            instance.loaded  = false;
            instance.loading = false;
            instance.portes.clear();
            instance.ramos.clear();
            instance.tiposVaga.clear();
            instance.modalidades.clear();
            instance.escolaridades.clear();
            instance.cidades.clear();
            instance.statusVaga.clear();
        }
        Executors.newSingleThreadExecutor()
                .execute(() -> AppDatabase.get(ctx).lookupDao().deleteAll());
    }

    /**
     * 1. Se já tem dados em memória → callback imediato.
     * 2. Se tem dados no Room → callback imediato + refresh em background.
     * 3. Se não tem nada → busca na API, salva no Room, chama callback.
     */
    public void preload(Context ctx, @Nullable OnReady onReady) {
        if (loaded) {
            if (onReady != null) onReady.onReady();
            return;
        }

        if (onReady != null) pending.add(onReady);
        if (loading) return;
        loading = true;

        executor.execute(() -> {
            boolean hasData = loadFromDb(ctx);
            main.post(() -> {
                if (hasData) {
                    loaded  = true;
                    loading = false;
                    for (OnReady cb : pending) cb.onReady();
                    pending.clear();
                    refreshInBackground(ctx);
                } else {
                    fetchAll(ctx);
                }
            });
        });
    }

    // ── Persistência Room ─────────────────────────────────────────────────────

    private boolean loadFromDb(Context ctx) {
        AppDatabase db = AppDatabase.get(ctx);
        if (db.lookupDao().count() == 0) return false;

        portes.addAll(       LookupEntity.toList(db.lookupDao().getByTipo(LookupEntity.TIPO_PORTE)));
        ramos.addAll(        LookupEntity.toList(db.lookupDao().getByTipo(LookupEntity.TIPO_RAMO)));
        tiposVaga.addAll(    LookupEntity.toList(db.lookupDao().getByTipo(LookupEntity.TIPO_VAGA)));
        modalidades.addAll(  LookupEntity.toList(db.lookupDao().getByTipo(LookupEntity.TIPO_MODALIDADE)));
        escolaridades.addAll(LookupEntity.toList(db.lookupDao().getByTipo(LookupEntity.TIPO_ESCOLARIDADE)));
        cidades.addAll(      LookupEntity.toList(db.lookupDao().getByTipo(LookupEntity.TIPO_CIDADE)));
        statusVaga.addAll(   LookupEntity.toList(db.lookupDao().getByTipo(LookupEntity.TIPO_STATUS_VAGA)));
        return true;
    }

    private void saveToDb(Context ctx) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.get(ctx);
            db.lookupDao().deleteAll();
            List<LookupEntity> all = new ArrayList<>();
            all.addAll(LookupEntity.fromList(portes,        LookupEntity.TIPO_PORTE));
            all.addAll(LookupEntity.fromList(ramos,         LookupEntity.TIPO_RAMO));
            all.addAll(LookupEntity.fromList(tiposVaga,     LookupEntity.TIPO_VAGA));
            all.addAll(LookupEntity.fromList(modalidades,   LookupEntity.TIPO_MODALIDADE));
            all.addAll(LookupEntity.fromList(escolaridades, LookupEntity.TIPO_ESCOLARIDADE));
            all.addAll(LookupEntity.fromList(cidades,       LookupEntity.TIPO_CIDADE));
            all.addAll(LookupEntity.fromList(statusVaga,    LookupEntity.TIPO_STATUS_VAGA));
            db.lookupDao().insertAll(all);
        });
    }

    // ── Fetch da API ──────────────────────────────────────────────────────────

    private void fetchAll(Context ctx) {
        ApiService api = ApiClient.getService(ctx);
        AtomicInteger remaining = new AtomicInteger(7);

        Runnable tick = () -> {
            if (remaining.decrementAndGet() == 0) {
                saveToDb(ctx);
                loaded  = true;
                loading = false;
                for (OnReady cb : pending) cb.onReady();
                pending.clear();
            }
        };

        fetch(api.listarPortes(),        portes,        tick);
        fetch(api.listarRamos(),         ramos,         tick);
        fetch(api.listarTiposVaga(),     tiposVaga,     tick);
        fetch(api.listarModalidades(),   modalidades,   tick);
        fetch(api.listarEscolaridades(), escolaridades, tick);
        fetch(api.listarCidades(),       cidades,       tick);
        fetch(api.listarStatusVaga(),    statusVaga,    tick);
    }

    private void refreshInBackground(Context ctx) {
        ApiService api = ApiClient.getService(ctx);
        AtomicInteger remaining = new AtomicInteger(7);

        Runnable tick = () -> {
            if (remaining.decrementAndGet() == 0) saveToDb(ctx);
        };

        fetch(api.listarPortes(),        portes,        tick);
        fetch(api.listarRamos(),         ramos,         tick);
        fetch(api.listarTiposVaga(),     tiposVaga,     tick);
        fetch(api.listarModalidades(),   modalidades,   tick);
        fetch(api.listarEscolaridades(), escolaridades, tick);
        fetch(api.listarCidades(),       cidades,       tick);
        fetch(api.listarStatusVaga(),    statusVaga,    tick);
    }

    private void fetch(Call<List<LookupItem>> call, List<LookupItem> target, Runnable tick) {
        call.enqueue(new Callback<List<LookupItem>>() {
            @Override
            public void onResponse(Call<List<LookupItem>> c, Response<List<LookupItem>> r) {
                main.post(() -> {
                    if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                        target.clear();
                        target.addAll(r.body());
                    }
                    tick.run();
                });
            }
            @Override
            public void onFailure(Call<List<LookupItem>> c, Throwable t) {
                main.post(tick);
            }
        });
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public List<LookupItem> getPortes()        { return portes; }
    public List<LookupItem> getRamos()          { return ramos; }
    public List<LookupItem> getTiposVaga()      { return tiposVaga; }
    public List<LookupItem> getModalidades()    { return modalidades; }
    public List<LookupItem> getEscolaridades()  { return escolaridades; }
    public List<LookupItem> getCidades()        { return cidades; }
    public List<LookupItem> getStatusVaga()     { return statusVaga; }
}
