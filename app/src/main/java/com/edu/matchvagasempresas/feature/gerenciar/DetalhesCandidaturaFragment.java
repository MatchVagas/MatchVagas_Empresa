package com.edu.matchvagasempresas.feature.gerenciar;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.edu.matchvagasempresas.model.ExperienciaResponse;
import com.edu.matchvagasempresas.model.FormacaoResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.util.SessionManager;
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
        STATUS_MAP.put("Em análise",  1L);
        STATUS_MAP.put("Aprovado",  2L);
        STATUS_MAP.put("Reprovado",    3L);
        STATUS_MAP.put("Em entrevista",   4L);
        STATUS_MAP.put("Proposta Enviada",  5L);
        STATUS_MAP.put("Contratado",6L);
        STATUS_MAP.put("Desistiu",7L);
    }

    private CandidaturaEmpresaResponse candidaturaAtual;
    private AutoCompleteTextView actvStatus;
    private MaterialButton btnVerCurriculo;

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

        btnVerCurriculo = view.findViewById(R.id.btn_ver_curriculo);
        btnVerCurriculo.setEnabled(false);
        btnVerCurriculo.setOnClickListener(v -> baixarCurriculo());

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

        // Contato — endereço
        setText(view, R.id.tv_endereco, c.endereco);

        // Perfil profissional — exibe apenas o que o candidato autorizou compartilhar
        setInfoCompartilhada(view, R.id.tv_resumo,         c.objetivoProfissional);
        setInfoCompartilhada(view, R.id.tv_disponibilidade, c.disponibilidade);
        setInfoCompartilhada(view, R.id.tv_pretensao,       formatarPretensao(c.pretensaoSalarial));
        setInfoCompartilhadaLista(view, R.id.tv_experiencia,   formatarExperiencias(c.experiencias));
        setInfoCompartilhadaLista(view, R.id.tv_escolaridade,  formatarFormacoes(c.formacoes));
        setInfoCompartilhadaLista(view, R.id.tv_area_formacao, formatarAreaFormacao(c.formacoes));

        // Pré-seleciona o status atual no dropdown
        if (c.status != null && actvStatus != null) actvStatus.setText(c.status, false);

        // Botão de download do currículo
        boolean temCurriculo = c.curriculoNomeArquivo != null;
        if (btnVerCurriculo != null) {
            btnVerCurriculo.setEnabled(temCurriculo);
            if (temCurriculo) {
                btnVerCurriculo.setText("Baixar Currículo");
            } else {
                btnVerCurriculo.setText("Currículo não compartilhado");
            }
        }
        TextView tvCurriculoStatus = view.findViewById(R.id.tv_curriculo_status);
        if (tvCurriculoStatus != null) {
            tvCurriculoStatus.setVisibility(View.VISIBLE);
            if (temCurriculo) {
                tvCurriculoStatus.setText("Arquivo: " + c.curriculoNomeArquivo);
                tvCurriculoStatus.setTextColor(requireContext().getColor(R.color.primary));
            } else {
                tvCurriculoStatus.setText("O candidato não autorizou o compartilhamento do currículo");
                tvCurriculoStatus.setTextColor(requireContext().getColor(R.color.text_secondary));
            }
        }
    }

    private void baixarCurriculo() {
        if (candidaturaAtual == null || candidaturaAtual.curriculoNomeArquivo == null) return;

        String token = new SessionManager(requireContext()).getToken();
        String url = ApiClient.BASE_URL + "api/candidaturas/" + candidaturaAtual.id + "/curriculo/download";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        if (token != null) request.addRequestHeader("Authorization", "Bearer " + token);
        request.setTitle(candidaturaAtual.nomeCandidato != null
                ? "Currículo de " + candidaturaAtual.nomeCandidato
                : "Currículo");
        request.setDescription("Baixando currículo...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                candidaturaAtual.curriculoNomeArquivo);

        DownloadManager dm = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.enqueue(request);
            Toast.makeText(requireContext(), "Download iniciado — verifique as notificações",
                    Toast.LENGTH_SHORT).show();
        }
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

    // Diferencia campo sem dados de campo não compartilhado (null = bloqueado pelo candidato)
    private void setInfoCompartilhada(View root, int id, String value) {
        TextView tv = root.findViewById(id);
        if (tv == null) return;
        if (value != null) {
            tv.setText(value);
            tv.setTextColor(requireContext().getColor(R.color.text_primary));
            tv.setAlpha(1f);
        } else {
            tv.setText("Não compartilhado pelo candidato");
            tv.setTextColor(requireContext().getColor(R.color.text_secondary));
            tv.setAlpha(0.7f);
        }
    }

    // Igual ao anterior mas recebe String formatada vinda de lista (null = lista não compartilhada)
    private void setInfoCompartilhadaLista(View root, int id, String formatted) {
        setInfoCompartilhada(root, id, formatted);
    }

    private String formatarAreaFormacao(java.util.List<FormacaoResponse> lista) {
        if (lista == null) return null;
        if (lista.isEmpty()) return "Não informado";
        StringBuilder sb = new StringBuilder();
        for (FormacaoResponse f : lista) {
            if (f.curso == null) continue;
            if (sb.length() > 0) sb.append(" • ");
            sb.append(f.curso);
        }
        return sb.length() > 0 ? sb.toString() : "Não informado";
    }

    private String formatarPretensao(Double valor) {
        if (valor == null) return null;
        return String.format(new java.util.Locale("pt", "BR"), "R$ %.2f", valor);
    }

    private String formatarExperiencias(java.util.List<ExperienciaResponse> lista) {
        if (lista == null) return null;
        if (lista.isEmpty()) return "Nenhuma experiência cadastrada";
        StringBuilder sb = new StringBuilder();
        for (ExperienciaResponse e : lista) {
            if (sb.length() > 0) sb.append("\n\n");
            sb.append(e.cargo != null ? e.cargo : "").append(" — ").append(e.empresa != null ? e.empresa : "");
            String periodo = formatarPeriodo(e.dataInicio, e.dataFim);
            if (!periodo.isEmpty()) sb.append("\n").append(periodo);
            if (e.descricao != null && !e.descricao.isBlank()) sb.append("\n").append(e.descricao);
        }
        return sb.toString();
    }

    private String formatarFormacoes(java.util.List<FormacaoResponse> lista) {
        if (lista == null) return null;
        if (lista.isEmpty()) return "Nenhuma formação cadastrada";
        StringBuilder sb = new StringBuilder();
        for (FormacaoResponse f : lista) {
            if (sb.length() > 0) sb.append("\n\n");
            sb.append(f.curso != null ? f.curso : "");
            if (f.nivel != null) sb.append(" (").append(f.nivel).append(")");
            if (f.instituicao != null) sb.append("\n").append(f.instituicao);
            String periodo = formatarPeriodo(f.dataInicio, f.dataFim);
            if (!periodo.isEmpty()) sb.append("\n").append(periodo);
        }
        return sb.toString();
    }

    private String formatarPeriodo(String inicio, String fim) {
        if (inicio == null) return "";
        return inicio + " — " + (fim != null ? fim : "atual");
    }

    private String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        String[] p = iso.substring(0, 10).split("-");
        return p.length == 3 ? p[2] + "/" + p[1] + "/" + p[0] : iso;
    }
}
