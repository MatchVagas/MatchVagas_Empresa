package com.edu.matchvagasempresas.features.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;
import com.edu.matchvagasempresas.data.repository.CandidaturaRepositoryImpl;
import com.edu.matchvagasempresas.data.repository.EmpresaRepositoryImpl;
import com.edu.matchvagasempresas.data.repository.VagaRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.CandidaturaRepository;
import com.edu.matchvagasempresas.domain.repository.EmpresaRepository;
import com.edu.matchvagasempresas.domain.repository.VagaRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final EmpresaRepository empresaRepository;
    private final VagaRepository vagaRepository;
    private final CandidaturaRepository candidaturaRepository;

    private final MutableLiveData<EmpresaResponse> empresa = new MutableLiveData<>();
    private final MutableLiveData<List<VagaResponse>> vagas = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalCandidaturas = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.empresaRepository = new EmpresaRepositoryImpl(application);
        this.vagaRepository = new VagaRepositoryImpl(application);
        this.candidaturaRepository = new CandidaturaRepositoryImpl(application);
    }

    public LiveData<EmpresaResponse> getEmpresa() { return empresa; }
    public LiveData<List<VagaResponse>> getVagas() { return vagas; }
    public LiveData<Integer> getTotalCandidaturas() { return totalCandidaturas; }

    public void carregarEmpresa() {
        empresaRepository.loadEmpresa(
                cached -> { if (cached != null) empresa.postValue(cached); },
                fresh  -> empresa.postValue(fresh)
        );
    }

    public void carregarVagas() {
        vagaRepository.loadVagas(
                cached -> { if (cached != null) vagas.postValue(cached); },
                fresh  -> vagas.postValue(fresh)
        );
    }

    public void carregarCandidaturas() {
        candidaturaRepository.loadTodasCandidaturas(new CandidaturaRepository.Callback<List<CandidaturaEmpresaResponse>>() {
            @Override
            public void onSuccess(List<CandidaturaEmpresaResponse> data) {
                totalCandidaturas.postValue(data.size());
            }
            @Override
            public void onError(String message) {}
        });
    }
}
