package com.edu.matchvagasempresas.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.edu.matchvagasempresas.model.LookupItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LookupCache {

    public interface OnReady { void onReady(); }

    private static final String PREFS        = "lookup_cache";
    private static final String KEY_PORTES   = "portes";
    private static final String KEY_RAMOS    = "ramos";
    private static final String KEY_TIPOS    = "tipos_vaga";
    private static final String KEY_MOD      = "modalidades";
    private static final String KEY_ESCOL    = "escolaridades";
    private static final String KEY_CIDADES  = "cidades";
    private static final String KEY_STATUS   = "status_vaga";

    private static LookupCache instance;

    private final List<LookupItem> portes       = new ArrayList<>();
    private final List<LookupItem> ramos         = new ArrayList<>();
    private final List<LookupItem> tiposVaga     = new ArrayList<>();
    private final List<LookupItem> modalidades   = new ArrayList<>();
    private final List<LookupItem> escolaridades = new ArrayList<>();
    private final List<LookupItem> cidades       = new ArrayList<>();
    private final List<LookupItem> statusVaga    = new ArrayList<>();

    private boolean loading = false;
    private boolean loaded  = false;
    private final List<OnReady> pending = new ArrayList<>();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<LookupItem>>(){}.getType();

    private LookupCache() {}

    public static LookupCache get() {
        if (instance == null) instance = new LookupCache();
        return instance;
    }

    public static void clearPrefs(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    /**
     * 1. Se já tem dados em memória → callback imediato.
     * 2. Se tem dados salvos no SharedPreferences → callback imediato + refresh em background.
     * 3. Se não tem nada → busca na API, salva no SharedPreferences, chama callback.
     */
    public void preload(Context ctx, @Nullable OnReady onReady) {
        if (loaded) {
            if (onReady != null) onReady.onReady();
            return;
        }

        if (loadFromPrefs(ctx)) {
            loaded = true;
            if (onReady != null) onReady.onReady();
            refreshInBackground(ctx);
            return;
        }

        if (onReady != null) pending.add(onReady);
        if (loading) return;
        loading = true;
        fetchAll(ctx);
    }

    // ── Persistência ──────────────────────────────────────────────────────────

    private boolean loadFromPrefs(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String portesJson = p.getString(KEY_PORTES, null);
        if (portesJson == null) return false;

        portes.addAll(fromJson(p, KEY_PORTES));
        ramos.addAll(fromJson(p, KEY_RAMOS));
        tiposVaga.addAll(fromJson(p, KEY_TIPOS));
        modalidades.addAll(fromJson(p, KEY_MOD));
        escolaridades.addAll(fromJson(p, KEY_ESCOL));
        cidades.addAll(fromJson(p, KEY_CIDADES));
        statusVaga.addAll(fromJson(p, KEY_STATUS));
        return true;
    }

    private void saveToPrefs(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_PORTES,  gson.toJson(portes))
                .putString(KEY_RAMOS,   gson.toJson(ramos))
                .putString(KEY_TIPOS,   gson.toJson(tiposVaga))
                .putString(KEY_MOD,     gson.toJson(modalidades))
                .putString(KEY_ESCOL,   gson.toJson(escolaridades))
                .putString(KEY_CIDADES, gson.toJson(cidades))
                .putString(KEY_STATUS,  gson.toJson(statusVaga))
                .apply();
    }

    private List<LookupItem> fromJson(SharedPreferences p, String key) {
        String json = p.getString(key, null);
        if (json == null) return new ArrayList<>();
        List<LookupItem> list = gson.fromJson(json, listType);
        return list != null ? list : new ArrayList<>();
    }

    // ── Fetch da API ──────────────────────────────────────────────────────────

    private void fetchAll(Context ctx) {
        ApiService api = ApiClient.getService(ctx);
        AtomicInteger remaining = new AtomicInteger(7);

        Runnable tick = () -> {
            if (remaining.decrementAndGet() == 0) {
                saveToPrefs(ctx);
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

    /** Refresh silencioso em background após carregar do SharedPreferences. */
    private void refreshInBackground(Context ctx) {
        ApiService api = ApiClient.getService(ctx);
        AtomicInteger remaining = new AtomicInteger(7);

        Runnable tick = () -> {
            if (remaining.decrementAndGet() == 0) saveToPrefs(ctx);
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

    public List<LookupItem> getPortes()       { return portes; }
    public List<LookupItem> getRamos()         { return ramos; }
    public List<LookupItem> getTiposVaga()     { return tiposVaga; }
    public List<LookupItem> getModalidades()   { return modalidades; }
    public List<LookupItem> getEscolaridades() { return escolaridades; }
    public List<LookupItem> getCidades()       { return cidades; }
    public List<LookupItem> getStatusVaga()    { return statusVaga; }
}
