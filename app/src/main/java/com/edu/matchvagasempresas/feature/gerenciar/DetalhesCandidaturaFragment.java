package com.edu.matchvagasempresas.feature.gerenciar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalhesCandidaturaFragment extends Fragment {

    // Status disponíveis: label → id no banco
    private static final Map<String, Long> STATUS_MAP = new LinkedHashMap<>();
    static {
        STATUS_MAP.put("Aguardando",  1L);
        STATUS_MAP.put("Em análise",  2L);
        STATUS_MAP.put("Aprovado",    3L);
        STATUS_MAP.put("Reprovado",   4L);
        STATUS_MAP.put("Contratado",  5L);
    }

    private CandidaturaEmpresaResponse candidaturaAtual;
    private AutoCompleteTextView actvStatus;

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

        actvStatus = view.findViewById(R.id.actv_novo_status);
        List<String> labels = Arrays.asList(STATUS_MAP.keySet().toArray(new String[0]));
        actvStatus.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));

        view.findViewById(R.id.btn_ver_curriculo).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Currículo não disponível nesta versão",
                        Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_salvar_status).setOnClickListener(v -> salvarStatus(v));

        long candidaturaId = getArguments() != null ? getArguments().getLong("candidaturaId", -1) : -1;
        if (candidaturaId > 0) carregarCandidatura(view, candidaturaId);
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
                            candidaturaAtual = response.body();
                            preencherDados(view, candidaturaAtual);
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
        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null && c.nomeCandidato != null) toolbar.setTitle(c.nomeCandidato);

        // Avatar com iniciais
        if (c.nomeCandidato != null) {
            String[] partes = c.nomeCandidato.split(" ");
            String iniciais = partes.length >= 2
                    ? "" + partes[0].charAt(0) + partes[1].charAt(0)
                    : String.valueOf(partes[0].charAt(0));
            setText(view, R.id.tv_iniciais, iniciais.toUpperCase());
        }

        setText(view, R.id.tv_nome_candidato,    c.nomeCandidato);
        setText(view, R.id.tv_data_candidatura,  "Candidatou-se em " + formatDate(c.dataCandidatura));
        setText(view, R.id.tv_titulo_vaga,        c.tituloVaga);
        setText(view, R.id.tv_status_atual,       c.status);

        // Contato
        setText(view, R.id.tv_email_candidato, "");   // não retornado pela API por privacidade
        if (c.telefones != null && !c.telefones.isEmpty())
            setText(view, R.id.tv_telefone_candidato, String.join(" • ", c.telefones));
        else
            setText(view, R.id.tv_telefone_candidato, "Não informado");

        // Perfil profissional
        setText(view, R.id.tv_resumo,       c.objetivoProfissional);
        setText(view, R.id.tv_experiencia,  c.experienciasInfo);
        setText(view, R.id.tv_escolaridade, c.formacoesInfo);
        setText(view, R.id.tv_area_formacao, "");
        setText(view, R.id.tv_habilidades,  "");

        // Pré-seleciona o status atual no dropdown
        if (c.status != null && actvStatus != null) actvStatus.setText(c.status, false);
    }

    private void salvarStatus(View btn) {
        if (candidaturaAtual == null) return;
        String label = actvStatus != null ? actvStatus.getText().toString().trim() : "";
        Long statusId = STATUS_MAP.get(label);
        if (statusId == null) {
            Toast.makeText(requireContext(), "Selecione um status válido", Toast.LENGTH_SHORT).show();
            return;
        }

        ((MaterialButton) btn).setEnabled(false);
        ApiClient.getService(requireContext())
                .atualizarStatusCandidatura(candidaturaAtual.id, statusId)
                .enqueue(new Callback<CandidaturaEmpresaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CandidaturaEmpresaResponse> call,
                                           @NonNull Response<CandidaturaEmpresaResponse> r) {
                        if (!isAdded()) return;
                        ((MaterialButton) btn).setEnabled(true);
                        if (r.isSuccessful() && r.body() != null) {
                            candidaturaAtual = r.body();
                            setText(requireView(), R.id.tv_status_atual, candidaturaAtual.status);
                            Toast.makeText(requireContext(), "Status atualizado!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Erro ao atualizar status", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CandidaturaEmpresaResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        ((MaterialButton) btn).setEnabled(true);
                        Toast.makeText(requireContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setText(View root, int id, String value) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(value != null ? value : "Não informado");
    }

    private String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        String[] p = iso.substring(0, 10).split("-");
        return p.length == 3 ? p[2] + "/" + p[1] + "/" + p[0] : iso;
    }
}
