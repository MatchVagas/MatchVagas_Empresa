package com.edu.matchvagasempresas.domain.repository;

import com.edu.matchvagasempresas.data.remote.dto.AuthResponse;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;

public interface AuthRepository {

    void login(String email, String senha, AuthCallback callback);

    void registerEmpresa(Object request, RegisterCallback callback);

    void esqueceuSenha(String email, SimpleCallback callback);

    void verificarCodigo(String email, String codigo, TokenCallback callback);

    void redefinirSenha(String token, String novaSenha, SimpleCallback callback);

    interface AuthCallback {
        void onSuccess(AuthResponse auth, EmpresaResponse empresa);
        void onError(String message);
    }

    interface RegisterCallback {
        void onSuccess(AuthResponse auth);
        void onError(String message);
    }

    interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    interface TokenCallback {
        void onSuccess(String token);
        void onError(String message);
    }
}
