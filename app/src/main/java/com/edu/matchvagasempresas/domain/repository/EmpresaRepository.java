package com.edu.matchvagasempresas.domain.repository;

import com.edu.matchvagasempresas.data.remote.dto.EmpresaRequest;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;

import okhttp3.MultipartBody;

public interface EmpresaRepository {

    void loadEmpresa(OnCached<EmpresaResponse> onCached, OnFresh<EmpresaResponse> onFresh);

    void atualizarEmpresa(Long id, EmpresaRequest request, Callback<EmpresaResponse> callback);

    void uploadLogo(MultipartBody.Part arquivo, Callback<EmpresaResponse> callback);

    void saveEmpresa(EmpresaResponse empresa);

    void invalidateEmpresa();

    interface OnCached<T> { void onCached(T data); }
    interface OnFresh<T>  { void onFresh(T data); }

    interface Callback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
