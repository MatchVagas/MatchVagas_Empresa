package com.edu.matchvagasempresas.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "matchvagas_session";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMPRESA_ID = "empresa_id";
    private static final String KEY_NOME_EMPRESA = "nome_empresa";
    private static final String KEY_STATUS_EMPRESA = "status_empresa";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuth(String token, Long userId) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId)
                .apply();
    }

    public void saveEmpresa(Long empresaId, String nomeEmpresa, String status) {
        prefs.edit()
                .putLong(KEY_EMPRESA_ID, empresaId != null ? empresaId : -1L)
                .putString(KEY_NOME_EMPRESA, nomeEmpresa)
                .putString(KEY_STATUS_EMPRESA, status)
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public Long getUserId() {
        long id = prefs.getLong(KEY_USER_ID, -1);
        return id == -1 ? null : id;
    }

    public Long getEmpresaId() {
        long id = prefs.getLong(KEY_EMPRESA_ID, -1);
        return id == -1 ? null : id;
    }

    public String getNomeEmpresa() {
        return prefs.getString(KEY_NOME_EMPRESA, "");
    }

    public String getStatusEmpresa() {
        return prefs.getString(KEY_STATUS_EMPRESA, "");
    }

    public boolean isEmpresaAprovada() {
        return "APROVADA".equals(getStatusEmpresa());
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
