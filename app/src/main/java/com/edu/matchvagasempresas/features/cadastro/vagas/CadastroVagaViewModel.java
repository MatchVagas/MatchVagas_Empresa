package com.edu.matchvagasempresas.features.cadastro.vagas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.remote.dto.VagaRequest;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;
import com.edu.matchvagasempresas.data.repository.VagaRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.VagaRepository;

public class CadastroVagaViewModel extends AndroidViewModel {

    private final VagaRepository vagaRepository;
    private final MutableLiveData<Resource<VagaResponse>> publicarResult = new MutableLiveData<>();

    public CadastroVagaViewModel(@NonNull Application application) {
        super(application);
        this.vagaRepository = new VagaRepositoryImpl(application);
    }

    public LiveData<Resource<VagaResponse>> getPublicarResult() { return publicarResult; }

    public void publicarVaga(VagaRequest request) {
        publicarResult.setValue(Resource.loading());
        vagaRepository.criarVaga(request, new VagaRepository.Callback<VagaResponse>() {
            @Override
            public void onSuccess(VagaResponse data) {
                publicarResult.postValue(Resource.success(data));
            }
            @Override
            public void onError(String message) {
                publicarResult.postValue(Resource.error(message));
            }
        });
    }
}
