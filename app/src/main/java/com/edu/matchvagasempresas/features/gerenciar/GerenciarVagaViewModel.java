package com.edu.matchvagasempresas.features.gerenciar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.data.repository.CandidaturaRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.CandidaturaRepository;

import java.util.List;

public class GerenciarVagaViewModel extends AndroidViewModel {

    private final CandidaturaRepository candidaturaRepository;
    private final MutableLiveData<Resource<List<CandidaturaEmpresaResponse>>> candidaturas =
            new MutableLiveData<>();

    public GerenciarVagaViewModel(@NonNull Application application) {
        super(application);
        this.candidaturaRepository = new CandidaturaRepositoryImpl(application);
    }

    public LiveData<Resource<List<CandidaturaEmpresaResponse>>> getCandidaturas() {
        return candidaturas;
    }

    public void carregarCandidaturas(long vagaId) {
        candidaturas.setValue(Resource.loading());
        candidaturaRepository.loadCandidaturas(vagaId,
                cached -> {
                    if (cached != null) candidaturas.postValue(Resource.success(cached));
                },
                fresh -> candidaturas.postValue(Resource.success(fresh))
        );
    }
}
