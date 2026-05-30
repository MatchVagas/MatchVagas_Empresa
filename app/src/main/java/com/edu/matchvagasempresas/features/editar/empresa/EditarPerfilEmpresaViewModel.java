package com.edu.matchvagasempresas.features.editar.empresa;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaRequest;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;
import com.edu.matchvagasempresas.data.repository.EmpresaRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.EmpresaRepository;

import okhttp3.MultipartBody;

public class EditarPerfilEmpresaViewModel extends AndroidViewModel {

    private final EmpresaRepository empresaRepository;
    private final MutableLiveData<EmpresaResponse> empresaCarregada = new MutableLiveData<>();
    private final MutableLiveData<Resource<EmpresaResponse>> salvarResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<EmpresaResponse>> uploadResult = new MutableLiveData<>();

    public EditarPerfilEmpresaViewModel(@NonNull Application application) {
        super(application);
        this.empresaRepository = new EmpresaRepositoryImpl(application);
    }

    public LiveData<EmpresaResponse> getEmpresaCarregada() { return empresaCarregada; }
    public LiveData<Resource<EmpresaResponse>> getSalvarResult() { return salvarResult; }
    public LiveData<Resource<EmpresaResponse>> getUploadResult() { return uploadResult; }

    public void carregarPerfil() {
        empresaRepository.loadEmpresa(
                cached -> { if (cached != null) empresaCarregada.postValue(cached); },
                fresh  -> empresaCarregada.postValue(fresh)
        );
    }

    public void salvarPerfil(Long empresaId, EmpresaRequest request) {
        salvarResult.setValue(Resource.loading());
        empresaRepository.atualizarEmpresa(empresaId, request,
                new EmpresaRepository.Callback<EmpresaResponse>() {
                    @Override
                    public void onSuccess(EmpresaResponse data) {
                        salvarResult.postValue(Resource.success(data));
                    }
                    @Override
                    public void onError(String message) {
                        salvarResult.postValue(Resource.error(message));
                    }
                });
    }

    public void uploadLogo(MultipartBody.Part part) {
        uploadResult.setValue(Resource.loading());
        empresaRepository.uploadLogo(part, new EmpresaRepository.Callback<EmpresaResponse>() {
            @Override
            public void onSuccess(EmpresaResponse data) {
                uploadResult.postValue(Resource.success(data));
            }
            @Override
            public void onError(String message) {
                uploadResult.postValue(Resource.error(message));
            }
        });
    }
}
