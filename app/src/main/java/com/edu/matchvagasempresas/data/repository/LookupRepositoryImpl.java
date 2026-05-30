package com.edu.matchvagasempresas.data.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.edu.matchvagasempresas.data.local.database.AppDatabase;
import com.edu.matchvagasempresas.data.local.entity.LookupEntity;
import com.edu.matchvagasempresas.data.remote.ApiService;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.edu.matchvagasempresas.data.remote.dto.LookupItem;
import com.edu.matchvagasempresas.domain.repository.LookupRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LookupRepositoryImpl implements LookupRepository {

    private static LookupRepositoryImpl instance;

    private final Context context;
    private final List<LookupItem> portes       = new ArrayList<>();
    private final List<LookupItem> ramos        = new ArrayList<>();
    private final List<LookupItem> tiposVaga    = new ArrayList<>();
    private final List<LookupItem> modalidades  = new ArrayList<>();
    private final List<LookupItem> escolaridades = new ArrayList<>();
    private final List<LookupItem> cidades      = new ArrayList<>();
    private final List<LookupItem> statusVaga   = new ArrayList<>();

    private boolean loading = false;
    private boolean loaded  = false;
    private final List<OnReady> pending = new ArrayList<>();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private LookupRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    public static LookupRepositoryImpl get(Context context) {
        if (instance == null) instance = new LookupRepositoryImpl(context);
        return instance;
    }

    @Override
    public void preload(OnReady onReady) {
        if (loaded) {
            if (onReady != null) onReady.onReady();
            return;
        }
        if (onReady != null) pending.add(onReady);
        if (loading) return;
        loading = true;

        executor.execute(() -> {
            boolean hasData = loadFromDb();
            main.post(() -> {
                if (hasData) {
                    loaded = true;
                    loading = false;
                    for (OnReady cb : pending) cb.onReady();
                    pending.clear();
                    refreshInBackground();
                } else {
                    fetchAll();
                }
            });
        });
    }

    @Override
    public void clear() {
        loaded = false;
        loading = false;
        portes.clear(); ramos.clear(); tiposVaga.clear();
        modalidades.clear(); escolaridades.clear(); cidades.clear(); statusVaga.clear();
        executor.execute(() -> AppDatabase.get(context).lookupDao().deleteAll());
    }

    private boolean loadFromDb() {
        AppDatabase db = AppDatabase.get(context);
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

    private void saveToDb() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.get(context);
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

    private void fetchAll() {
        ApiService api = RetrofitClient.getService(context);
        AtomicInteger remaining = new AtomicInteger(7);
        Runnable tick = () -> {
            if (remaining.decrementAndGet() == 0) {
                saveToDb();
                loaded = true;
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

    private void refreshInBackground() {
        ApiService api = RetrofitClient.getService(context);
        AtomicInteger remaining = new AtomicInteger(7);
        Runnable tick = () -> { if (remaining.decrementAndGet() == 0) saveToDb(); };
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
            public void onFailure(Call<List<LookupItem>> c, Throwable t) { main.post(tick); }
        });
    }

    @Override public List<LookupItem> getPortes()       { return portes; }
    @Override public List<LookupItem> getRamos()         { return ramos; }
    @Override public List<LookupItem> getTiposVaga()     { return tiposVaga; }
    @Override public List<LookupItem> getModalidades()   { return modalidades; }
    @Override public List<LookupItem> getEscolaridades() { return escolaridades; }
    @Override public List<LookupItem> getCidades()       { return cidades; }
    @Override public List<LookupItem> getStatusVaga()    { return statusVaga; }
}
