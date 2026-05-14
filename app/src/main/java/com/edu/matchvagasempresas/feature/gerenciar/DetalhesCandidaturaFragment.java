package com.edu.matchvagasempresas.feature.gerenciar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalhesCandidaturaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_detalhes_candidatura, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btn_ver_curriculo).setOnClickListener(v ->
                Snackbar.make(v, "Currículo não disponível", Snackbar.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_salvar_status).setOnClickListener(v -> {
            Snackbar.make(v, "Atualização de status não disponível nesta versão",
                    Snackbar.LENGTH_SHORT).show();
        });

        long candidaturaId = getArguments() != null ? getArguments().getLong("candidaturaId", -1) : -1;
        if (candidaturaId > 0) {
            carregarCandidatura(view, candidaturaId);
        }
    }

    private void carregarCandidatura(View view, long candidaturaId) {
        ApiClient.getService(requireContext())
                .detalharCandidatura(candidaturaId)
                .enqueue(new Callback<CandidaturaEmpresaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CandidaturaEmpresaResponse> call,
                                           @NonNull Response<CandidaturaEmpresaResponse> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            preencherDados(view, response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CandidaturaEmpresaResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Erro ao carregar candidatura", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void preencherDados(View view, CandidaturaEmpresaResponse c) {
        setText(view, R.id.tv_escolaridade, c.formacoesInfo);
        setText(view, R.id.tv_area_formacao, "");
        setText(view, R.id.tv_experiencia, c.experienciasInfo);
        setText(view, R.id.tv_habilidades, "");
        setText(view, R.id.tv_resumo, c.objetivoProfissional);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null && c.nomeCandidato != null) {
            toolbar.setTitle(c.nomeCandidato);
        }
    }

    private void setText(View root, int id, String value) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(value != null ? value : "Não informado");
    }
}
