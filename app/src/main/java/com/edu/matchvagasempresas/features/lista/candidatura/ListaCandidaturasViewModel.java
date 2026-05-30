package com.edu.matchvagasempresas.features.lista.candidatura;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;
import com.edu.matchvagasempresas.data.repository.CandidaturaRepositoryImpl;
import com.edu.matchvagasempresas.data.repository.VagaRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.CandidaturaRepository;
import com.edu.matchvagasempresas.domain.repository.VagaRepository;

import java.util.List;

public class ListaCandidaturasViewModel extends AndroidViewModel {

    private final CandidaturaRepository candidaturaRepository;
    private final VagaRepository vagaRepository;

    private final MutableLiveData<List<CandidaturaEmpresaResponse>> candidaturasCached = new MutableLiveData<>();
    private final MutableLiveData<List<CandidaturaEmpresaResponse>> candidaturasFresh = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<CandidaturaEmpresaResponse>>> refreshResult = new MutableLiveData<>();
    private final MutableLiveData<List<VagaResponse>> vagas = new MutableLiveData<>();

    public ListaCandidaturasViewModel(@NonNull Application application) {
        super(application);
        this.candidaturaRepository = new CandidaturaRepositoryImpl(application);
        this.vagaRepository = new VagaRepositoryImpl(application);
    }

    public LiveData<List<CandidaturaEmpresaResponse>> getCandidaturasCached() { return candidaturasCached; }
    public LiveData<List<CandidaturaEmpresaResponse>> getCandidaturasFresh() { return candidaturasFresh; }
    public LiveData<Resource<List<CandidaturaEmpresaResponse>>> getRefreshResult() { return refreshResult; }
    public LiveData<List<VagaResponse>> getVagas() { return vagas; }

    public void carregarCandidaturas(long vagaId) {
        candidaturaRepository.loadCandidaturas(vagaId,
                cached -> { if (cached != null) candidaturasCached.postValue(cached); },
                fresh  -> candidaturasFresh.postValue(fresh)
        );
    }

    public void refreshCandidaturas(long vagaId) {
        refreshResult.setValue(Resource.loading());
        candidaturaRepository.loadCandidaturas(vagaId,
                cached -> {},
                fresh  -> {
                    candidaturaRepository.saveCandidaturas(vagaId, fresh);
                    refreshResult.postValue(Resource.success(fresh));
                }
        );
    }

    public void carregarVagas() {
        vagaRepository.loadVagas(
                cached -> { if (cached != null) vagas.postValue(cached); },
                fresh  -> vagas.postValue(fresh)
        );
    }
}
