package com.edu.matchvagasempresas.domain.repository;

import com.edu.matchvagasempresas.data.remote.dto.VagaRequest;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;

import java.util.List;

public interface VagaRepository {

    void loadVagas(OnCached<List<VagaResponse>> onCached, OnFresh<List<VagaResponse>> onFresh);

    void buscarVaga(long id, Callback<VagaResponse> callback);

    void criarVaga(VagaRequest request, Callback<VagaResponse> callback);

    void atualizarVaga(long id, VagaRequest request, Callback<VagaResponse> callback);

    void deletarVaga(long id, SimpleCallback callback);

    void saveVagas(List<VagaResponse> vagas);

    void invalidateVagas();

    interface OnCached<T> { void onCached(T data); }
    interface OnFresh<T>  { void onFresh(T data); }

    interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }
}
