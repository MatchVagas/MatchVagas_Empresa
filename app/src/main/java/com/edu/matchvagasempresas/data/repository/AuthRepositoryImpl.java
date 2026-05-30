package com.edu.matchvagasempresas.data.repository;

import android.content.Context;

import androidx.annotation.NonNull;

import com.edu.matchvagasempresas.data.remote.ApiService;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.edu.matchvagasempresas.data.remote.dto.AuthResponse;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.RegisterEmpresaRequest;
import com.edu.matchvagasempresas.domain.repository.AuthRepository;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepositoryImpl implements AuthRepository {

    private final ApiService apiService;
    private final Context context;

    public AuthRepositoryImpl(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getService(context);
    }

    @Override
    public void login(String email, String senha, AuthCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("senha", senha);

        apiService.login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    RetrofitClient.reset();
                    apiService.minhaEmpresa().enqueue(new Callback<EmpresaResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<EmpresaResponse> c,
                                               @NonNull Response<EmpresaResponse> r) {
                            callback.onSuccess(auth, r.isSuccessful() ? r.body() : null);
                        }
                        @Override
                        public void onFailure(@NonNull Call<EmpresaResponse> c, @NonNull Throwable t) {
                            callback.onSuccess(auth, null);
                        }
                    });
                } else {
                    callback.onError("E-mail ou senha inválidos");
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }

    @Override
    public void registerEmpresa(Object request, RegisterCallback callback) {
        apiService.registerEmpresa((RegisterEmpresaRequest) request)
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AuthResponse> call,
                                           @NonNull Response<AuthResponse> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            callback.onSuccess(r.body());
                        } else {
                            String msg = r.code() == 400
                                    ? "Dados inválidos: verifique CNPJ, e-mail ou campos obrigatórios."
                                    : "Erro no cadastro. Tente novamente.";
                            callback.onError(msg);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                        callback.onError("Erro de conexão: " + t.getMessage());
                    }
                });
    }

    @Override
    public void esqueceuSenha(String email, SimpleCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);

        apiService.esqueceuSenha(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                callback.onSuccess();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }

    @Override
    public void verificarCodigo(String email, String codigo, TokenCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("codigo", codigo);

        apiService.verificarCodigo(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call,
                                   @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().get("token").getAsString());
                } else {
                    callback.onError("Código inválido ou expirado");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }

    @Override
    public void redefinirSenha(String token, String novaSenha, SimpleCallback callback) {
        JsonObject body = new JsonObject();
        body.addProperty("token", token);
        body.addProperty("novaSenha", novaSenha);

        apiService.redefinirSenha(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Token inválido ou expirado. Tente novamente.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError("Erro de conexão: " + t.getMessage());
            }
        });
    }
}
