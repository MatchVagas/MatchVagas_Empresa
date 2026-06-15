package com.edu.matchvagasempresas.features.gerenciar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.HistoricoStatusResponse;
import com.edu.matchvagasempresas.data.remote.dto.LookupItem;
import com.edu.matchvagasempresas.data.repository.CandidaturaRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.CandidaturaRepository;

import java.util.List;

public class DetalhesCandidaturaViewModel extends AndroidViewModel {

    private final CandidaturaRepository candidaturaRepository;
    private final MutableLiveData<Resource<CandidaturaEmpresaResponse>> candidatura =
            new MutableLiveData<>();
    private final MutableLiveData<Resource<CandidaturaEmpresaResponse>> statusResult =
            new MutableLiveData<>();
    private final MutableLiveData<Resource<List<LookupItem>>> statusOpcoes =
            new MutableLiveData<>();
    private final MutableLiveData<Resource<List<HistoricoStatusResponse>>> historico =
            new MutableLiveData<>();

    public DetalhesCandidaturaViewModel(@NonNull Application application) {
        super(application);
        this.candidaturaRepository = new CandidaturaRepositoryImpl(application);
    }

    public LiveData<Resource<CandidaturaEmpresaResponse>> getCandidatura() { return candidatura; }
    public LiveData<Resource<CandidaturaEmpresaResponse>> getStatusResult() { return statusResult; }
    public LiveData<Resource<List<LookupItem>>> getStatusOpcoes() { return statusOpcoes; }
    public LiveData<Resource<List<HistoricoStatusResponse>>> getHistorico() { return historico; }

    public void carregarHistorico(long candidaturaId) {
        historico.setValue(Resource.loading());
        candidaturaRepository.listarHistoricoStatus(candidaturaId,
                new CandidaturaRepository.Callback<List<HistoricoStatusResponse>>() {
                    @Override
                    public void onSuccess(List<HistoricoStatusResponse> data) {
                        historico.postValue(Resource.success(data));
                    }
                    @Override
                    public void onError(String message) {
                        historico.postValue(Resource.error(message));
                    }
                });
    }

    public void carregarStatusOpcoes() {
        statusOpcoes.setValue(Resource.loading());
        candidaturaRepository.listarStatusCandidatura(
                new CandidaturaRepository.Callback<List<LookupItem>>() {
                    @Override
                    public void onSuccess(List<LookupItem> data) {
                        statusOpcoes.postValue(Resource.success(data));
                    }
                    @Override
                    public void onError(String message) {
                        statusOpcoes.postValue(Resource.error(message));
                    }
                });
    }

    public void carregarCandidatura(long id) {
        candidatura.setValue(Resource.loading());
        candidaturaRepository.detalharCandidatura(id,
                new CandidaturaRepository.Callback<CandidaturaEmpresaResponse>() {
                    @Override
                    public void onSuccess(CandidaturaEmpresaResponse data) {
                        candidatura.postValue(Resource.success(data));
                    }
                    @Override
                    public void onError(String message) {
                        candidatura.postValue(Resource.error(message));
                    }
                });
    }

    public void atualizarStatus(long candidaturaId, long statusId) {
        statusResult.setValue(Resource.loading());
        candidaturaRepository.atualizarStatus(candidaturaId, statusId,
                new CandidaturaRepository.Callback<CandidaturaEmpresaResponse>() {
                    @Override
                    public void onSuccess(CandidaturaEmpresaResponse data) {
                        statusResult.postValue(Resource.success(data));
                        // Trilha de auditoria pode ter crescido: recarrega o histórico.
                        carregarHistorico(candidaturaId);
                    }
                    @Override
                    public void onError(String message) {
                        statusResult.postValue(Resource.error(message));
                    }
                });
    }
}
