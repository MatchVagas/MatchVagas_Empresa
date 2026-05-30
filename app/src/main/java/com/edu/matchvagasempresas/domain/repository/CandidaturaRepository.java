package com.edu.matchvagasempresas.domain.repository;

import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;

import java.util.List;

public interface CandidaturaRepository {

    void loadCandidaturas(long vagaId,
                          OnCached<List<CandidaturaEmpresaResponse>> onCached,
                          OnFresh<List<CandidaturaEmpresaResponse>> onFresh);

    void loadTodasCandidaturas(Callback<List<CandidaturaEmpresaResponse>> callback);

    void detalharCandidatura(long id, Callback<CandidaturaEmpresaResponse> callback);

    void atualizarStatus(long candidaturaId, long statusId,
                         Callback<CandidaturaEmpresaResponse> callback);

    void saveCandidaturas(long vagaId, List<CandidaturaEmpresaResponse> lista);

    void invalidateCandidaturas(long vagaId);

    interface OnCached<T> { void onCached(T data); }
    interface OnFresh<T>  { void onFresh(T data); }

    interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
