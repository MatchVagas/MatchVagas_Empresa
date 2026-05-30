package com.edu.matchvagasempresas.features.lista.vaga;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;
import com.edu.matchvagasempresas.data.repository.VagaRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.VagaRepository;

import java.util.List;

public class ListaVagasViewModel extends AndroidViewModel {

    private final VagaRepository vagaRepository;
    private final MutableLiveData<List<VagaResponse>> vagasCached = new MutableLiveData<>();
    private final MutableLiveData<List<VagaResponse>> vagasFresh = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<VagaResponse>>> refreshResult = new MutableLiveData<>();

    public ListaVagasViewModel(@NonNull Application application) {
        super(application);
        this.vagaRepository = new VagaRepositoryImpl(application);
    }

    public LiveData<List<VagaResponse>> getVagasCached() { return vagasCached; }
    public LiveData<List<VagaResponse>> getVagasFresh() { return vagasFresh; }
    public LiveData<Resource<List<VagaResponse>>> getRefreshResult() { return refreshResult; }

    public void carregarVagas() {
        vagaRepository.loadVagas(
                cached -> { if (cached != null) vagasCached.postValue(cached); },
                fresh  -> vagasFresh.postValue(fresh)
        );
    }

    public void refreshVagas() {
        refreshResult.setValue(Resource.loading());
        vagaRepository.loadVagas(
                cached -> {},
                fresh  -> {
                    vagaRepository.saveVagas(fresh);
                    refreshResult.postValue(Resource.success(fresh));
                }
        );
    }
}
