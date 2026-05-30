package com.edu.matchvagasempresas.features.editar.vaga;

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

public class EditarVagaViewModel extends AndroidViewModel {

    private final VagaRepository vagaRepository;
    private final MutableLiveData<Resource<VagaResponse>> vagaCarregada = new MutableLiveData<>();
    private final MutableLiveData<Resource<VagaResponse>> atualizarResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> deletarResult = new MutableLiveData<>();

    public EditarVagaViewModel(@NonNull Application application) {
        super(application);
        this.vagaRepository = new VagaRepositoryImpl(application);
    }

    public LiveData<Resource<VagaResponse>> getVagaCarregada() { return vagaCarregada; }
    public LiveData<Resource<VagaResponse>> getAtualizarResult() { return atualizarResult; }
    public LiveData<Resource<Void>> getDeletarResult() { return deletarResult; }

    public void carregarVaga(long id) {
        vagaCarregada.setValue(Resource.loading());
        vagaRepository.buscarVaga(id, new VagaRepository.Callback<VagaResponse>() {
            @Override
            public void onSuccess(VagaResponse data) {
                vagaCarregada.postValue(Resource.success(data));
            }
            @Override
            public void onError(String message) {
                vagaCarregada.postValue(Resource.error(message));
            }
        });
    }

    public void atualizarVaga(long id, VagaRequest request) {
        atualizarResult.setValue(Resource.loading());
        vagaRepository.atualizarVaga(id, request, new VagaRepository.Callback<VagaResponse>() {
            @Override
            public void onSuccess(VagaResponse data) {
                atualizarResult.postValue(Resource.success(data));
            }
            @Override
            public void onError(String message) {
                atualizarResult.postValue(Resource.error(message));
            }
        });
    }

    public void deletarVaga(long id) {
        deletarResult.setValue(Resource.loading());
        vagaRepository.deletarVaga(id, new VagaRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                deletarResult.postValue(Resource.success(null));
            }
            @Override
            public void onError(String message) {
                deletarResult.postValue(Resource.error(message));
            }
        });
    }
}
