package com.edu.matchvagasempresas.features.cadastro.empresa;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.remote.dto.AuthResponse;
import com.edu.matchvagasempresas.data.remote.dto.RegisterEmpresaRequest;
import com.edu.matchvagasempresas.data.repository.AuthRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.AuthRepository;

public class CadastroEmpresaViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<AuthResponse>> cadastroResult = new MutableLiveData<>();

    public CadastroEmpresaViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepositoryImpl(application);
    }

    public LiveData<Resource<AuthResponse>> getCadastroResult() { return cadastroResult; }

    public void cadastrar(RegisterEmpresaRequest request) {
        cadastroResult.setValue(Resource.loading());
        authRepository.registerEmpresa(request, new AuthRepository.RegisterCallback() {
            @Override
            public void onSuccess(AuthResponse auth) {
                cadastroResult.postValue(Resource.success(auth));
            }
            @Override
            public void onError(String message) {
                cadastroResult.postValue(Resource.error(message));
            }
        });
    }
}
