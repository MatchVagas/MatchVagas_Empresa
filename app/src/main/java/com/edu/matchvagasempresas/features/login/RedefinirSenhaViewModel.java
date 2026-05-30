package com.edu.matchvagasempresas.features.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.repository.AuthRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.AuthRepository;

public class RedefinirSenhaViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<Void>> result = new MutableLiveData<>();

    public RedefinirSenhaViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepositoryImpl(application);
    }

    public LiveData<Resource<Void>> getResult() { return result; }

    public void redefinirSenha(String token, String novaSenha) {
        result.setValue(Resource.loading());
        authRepository.redefinirSenha(token, novaSenha, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                result.setValue(Resource.success(null));
            }
            @Override
            public void onError(String message) {
                result.setValue(Resource.error(message));
            }
        });
    }
}
