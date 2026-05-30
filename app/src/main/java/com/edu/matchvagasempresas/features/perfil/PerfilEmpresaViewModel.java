package com.edu.matchvagasempresas.features.perfil;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.matchvagasempresas.core.utils.Resource;
import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;
import com.edu.matchvagasempresas.data.repository.EmpresaRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.EmpresaRepository;

import okhttp3.MultipartBody;

public class PerfilEmpresaViewModel extends AndroidViewModel {

    private final EmpresaRepository empresaRepository;
    private final MutableLiveData<EmpresaResponse> empresaCached = new MutableLiveData<>();
    private final MutableLiveData<EmpresaResponse> empresaFresh = new MutableLiveData<>();
    private final MutableLiveData<Resource<EmpresaResponse>> uploadResult = new MutableLiveData<>();

    public PerfilEmpresaViewModel(@NonNull Application application) {
        super(application);
        this.empresaRepository = new EmpresaRepositoryImpl(application);
    }

    public LiveData<EmpresaResponse> getEmpresaCached() { return empresaCached; }
    public LiveData<EmpresaResponse> getEmpresaFresh() { return empresaFresh; }
    public LiveData<Resource<EmpresaResponse>> getUploadResult() { return uploadResult; }

    public void carregarPerfil() {
        empresaRepository.loadEmpresa(
                cached -> { if (cached != null) empresaCached.postValue(cached); },
                fresh  -> empresaFresh.postValue(fresh)
        );
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
