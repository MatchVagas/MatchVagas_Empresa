package com.edu.matchvagasempresas.network;

import android.content.Context;
import android.content.SharedPreferences;

import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.model.VagaResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Cache local de dados de tela com padrão stale-while-revalidate:
 *   1. Retorna dado do disco imediatamente (sem esperar rede).
 *   2. Busca versão atualizada da API em background.
 *   3. Chama onFresh() quando o dado novo chega — o fragment decide se redesenha.
 *
 * TTL de 10 minutos: dados expirados são buscados novamente, mas ainda exibidos
 * enquanto o refresh acontece para não deixar a tela vazia.
 */
public class DataCache {

    public interface OnCached<T>  { void onCached(T data); }
    public interface OnFresh<T>   { void onFresh(T data); }

    private static final String PREFS          = "screen_cache";
    private static final String KEY_VAGAS      = "vagas";
    private static final String KEY_EMPRESA    = "empresa";
    private static final String KEY_VAGAS_TS   = "vagas_ts";
    private static final String KEY_EMPRESA_TS = "empresa_ts";
    private static final long   TTL_MS         = 10 * 60 * 1000L; // 10 minutos

    private static DataCache instance;
    private final Gson gson = new Gson();

    private DataCache() {}

    public static DataCache get() {
        if (instance == null) instance = new DataCache();
        return instance;
    }

    // ── Vagas ─────────────────────────────────────────────────────────────────

    /**
     * Carrega vagas do cache (se houver) e dispara refresh da API em background.
     * @param onCached chamado imediatamente com dado do disco (pode ser null)
     * @param onFresh  chamado quando dado novo chegar da API
     */
    public void loadVagas(Context ctx,
                          OnCached<List<VagaResponse>> onCached,
                          OnFresh<List<VagaResponse>> onFresh) {

        List<VagaResponse> cached = readVagas(ctx);
        if (onCached != null) onCached.onCached(cached);

        ApiClient.getService(ctx).minhasVagas().enqueue(new retrofit2.Callback<List<VagaResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<List<VagaResponse>> call,
                                   retrofit2.Response<List<VagaResponse>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    saveVagas(ctx, r.body());
                    if (onFresh != null) onFresh.onFresh(r.body());
                }
            }
            @Override
            public void onFailure(retrofit2.Call<List<VagaResponse>> call, Throwable t) {}
        });
    }

    public void saveVagas(Context ctx, List<VagaResponse> vagas) {
        prefs(ctx).edit()
                .putString(KEY_VAGAS, gson.toJson(vagas))
                .putLong(KEY_VAGAS_TS, System.currentTimeMillis())
                .apply();
    }

    public List<VagaResponse> readVagas(Context ctx) {
        String json = prefs(ctx).getString(KEY_VAGAS, null);
        if (json == null) return null;
        Type type = new TypeToken<List<VagaResponse>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public boolean vagasExpiradas(Context ctx) {
        long ts = prefs(ctx).getLong(KEY_VAGAS_TS, 0);
        return System.currentTimeMillis() - ts > TTL_MS;
    }

    // ── Empresa ───────────────────────────────────────────────────────────────

    public void loadEmpresa(Context ctx,
                            OnCached<EmpresaResponse> onCached,
                            OnFresh<EmpresaResponse> onFresh) {

        EmpresaResponse cached = readEmpresa(ctx);
        if (onCached != null) onCached.onCached(cached);

        ApiClient.getService(ctx).minhaEmpresa().enqueue(new retrofit2.Callback<EmpresaResponse>() {
            @Override
            public void onResponse(retrofit2.Call<EmpresaResponse> call,
                                   retrofit2.Response<EmpresaResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    saveEmpresa(ctx, r.body());
                    if (onFresh != null) onFresh.onFresh(r.body());
                }
            }
            @Override
            public void onFailure(retrofit2.Call<EmpresaResponse> call, Throwable t) {}
        });
    }

    public void saveEmpresa(Context ctx, EmpresaResponse empresa) {
        prefs(ctx).edit()
                .putString(KEY_EMPRESA, gson.toJson(empresa))
                .putLong(KEY_EMPRESA_TS, System.currentTimeMillis())
                .apply();
    }

    public EmpresaResponse readEmpresa(Context ctx) {
        String json = prefs(ctx).getString(KEY_EMPRESA, null);
        if (json == null) return null;
        return gson.fromJson(json, EmpresaResponse.class);
    }

    // ── Invalidação ───────────────────────────────────────────────────────────

    /** Chame após criar/editar/excluir uma vaga para forçar refresh na próxima abertura. */
    public void invalidateVagas(Context ctx) {
        prefs(ctx).edit().remove(KEY_VAGAS_TS).apply();
    }

    /** Chame após editar o perfil da empresa. */
    public void invalidateEmpresa(Context ctx) {
        prefs(ctx).edit().remove(KEY_EMPRESA_TS).apply();
    }

    /** Limpa todo o cache (usado no logout). */
    public void clear(Context ctx) {
        prefs(ctx).edit().clear().apply();
    }

    // ── Interno ───────────────────────────────────────────────────────────────

    private SharedPreferences prefs(Context ctx) {
        return ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
