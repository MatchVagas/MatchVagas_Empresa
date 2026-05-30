package com.edu.matchvagasempresas.features.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.repository.AuthRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.AuthRepository;

public class VerificarCodigoViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<String>> tokenResult = new MutableLiveData<>();

    public VerificarCodigoViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepositoryImpl(application);
    }

    public LiveData<Resource<String>> getTokenResult() { return tokenResult; }

    public void verificarCodigo(String email, String codigo) {
        tokenResult.setValue(Resource.loading());
        authRepository.verificarCodigo(email, codigo, new AuthRepository.TokenCallback() {
            @Override
            public void onSuccess(String token) {
                tokenResult.setValue(Resource.success(token));
            }
            @Override
            public void onError(String message) {
                tokenResult.setValue(Resource.error(message));
            }
        });
    }
}
