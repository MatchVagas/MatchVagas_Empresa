package com.edu.matchvagasempresas.features.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.remote.dto.AuthResponse;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;
import com.edu.matchvagasempresas.data.repository.AuthRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.AuthRepository;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<LoginResult>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> reenvioResult = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepositoryImpl(application);
    }

    public LiveData<Resource<LoginResult>> getLoginResult() { return loginResult; }

    public LiveData<Resource<String>> getReenvioResult() { return reenvioResult; }

    /** Reenvia o link de confirmação de e-mail (o login fica bloqueado até a confirmação). */
    public void reenviarVerificacao(String email) {
        reenvioResult.setValue(Resource.loading());
        authRepository.reenviarVerificacao(email, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                reenvioResult.setValue(Resource.success(email));
            }
            @Override
            public void onError(String message) {
                reenvioResult.setValue(Resource.error(message));
            }
        });
    }

    public void login(String email, String senha) {
        loginResult.setValue(Resource.loading());
        authRepository.login(email, senha, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse auth, EmpresaResponse empresa) {
                loginResult.setValue(Resource.success(new LoginResult(auth, empresa)));
            }
            @Override
            public void onError(String message) {
                loginResult.setValue(Resource.error(message));
            }
        });
    }

    public static class LoginResult {
        public final AuthResponse auth;
        public final EmpresaResponse empresa;

        public LoginResult(AuthResponse auth, EmpresaResponse empresa) {
            this.auth = auth;
            this.empresa = empresa;
        }
    }
}
