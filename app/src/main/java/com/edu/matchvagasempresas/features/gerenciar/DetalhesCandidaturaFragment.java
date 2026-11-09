package com.edu.matchvagasempresas.features.gerenciar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edu.matchvagasempresas.BuildConfig;
import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.core.utils.FluxoStatusCandidatura;
import com.edu.matchvagasempresas.data.local.SessionManager;
import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.ExperienciaResponse;
import com.edu.matchvagasempresas.data.remote.dto.FormacaoResponse;
import com.edu.matchvagasempresas.data.remote.dto.HistoricoStatusResponse;
import com.edu.matchvagasempresas.data.remote.dto.LookupItem;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DetalhesCandidaturaFragment extends Fragment {

    // Mapa label -> id preenchido dinamicamente a partir do lookup da API
    // (GET /api/lookup/sistema/status-candidatura). Não chumbar valores aqui:
    // os status válidos são definidos no backend e podem mudar.
    private final Map<String, Long> statusMap = new LinkedHashMap<>();
    // Opções cruas do lookup; o dropdown é filtrado pelo fluxo conforme o status atual.
    private List<LookupItem> statusOpcoesRaw;

    private DetalhesCandidaturaViewModel viewModel;
    private CandidaturaEmpresaResponse candidaturaAtual;
    private AutoCompleteTextView actvStatus;
    private com.google.android.material.textfield.TextInputLayout tilStatus;
    private MaterialButton btnSalvarStatus;
    private MaterialButton btnVerCurriculo;
    private SwipeRefreshLayout swipeRefresh;
    private long candidaturaId = -1;
    private AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_detalhes_candidatura, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DetalhesCandidaturaViewModel.class);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        actvStatus = view.findViewById(R.id.actv_novo_status);
        tilStatus = view.findViewById(R.id.til_novo_status);

        btnVerCurriculo = view.findViewById(R.id.btn_ver_curriculo);
        btnVerCurriculo.setEnabled(false);
        btnVerCurriculo.setOnClickListener(v -> baixarCurriculo());

        btnSalvarStatus = view.findViewById(R.id.btn_salvar_status);
        btnSalvarStatus.setOnClickListener(this::salvarStatus);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(() -> {
            if (candidaturaId > 0) {
                viewModel.carregarCandidatura(candidaturaId);
                viewModel.carregarHistorico(candidaturaId);
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });

        viewModel.getCandidatura().observe(getViewLifecycleOwner(), resource -> {
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null) {
                        candidaturaAtual = resource.getData();
                        preencherDados(view, candidaturaAtual);
                    }
                    break;
                case ERROR:
                    showErroDialog(resource.getMessage());
                    break;
                default: break;
            }
        });

        viewModel.getStatusResult().observe(getViewLifecycleOwner(), resource -> {
            hideLoading();
            view.findViewById(R.id.btn_salvar_status).setEnabled(true);
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null) {
                        candidaturaAtual = resource.getData();
                        TextView tvStatus = view.findViewById(R.id.tv_status_atual);
                        if (tvStatus != null) tvStatus.setText(candidaturaAtual.status);
                        // Novo status → recalcula transições válidas no dropdown.
                        atualizarDropdownStatus();
                        Toast.makeText(requireContext(), "Status atualizado!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    showErroDialog(resource.getMessage());
                    break;
                default: break;
            }
        });

        viewModel.getStatusOpcoes().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null) preencherStatusOpcoes(resource.getData());
                    break;
                case ERROR:
                    Toast.makeText(requireContext(),
                            "Não foi possível carregar os status: " + resource.getMessage(),
                            Toast.LENGTH_LONG).show();
                    break;
                default: break;
            }
        });

        viewModel.getHistorico().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    preencherHistorico(view, resource.getData());
                    break;
                case ERROR:
                    Toast.makeText(requireContext(),
                            "Não foi possível carregar o histórico: " + resource.getMessage(),
                            Toast.LENGTH_LONG).show();
                    break;
                default: break;
            }
        });

        candidaturaId = getArguments() != null ? getArguments().getLong("candidaturaId", -1) : -1;
        if (candidaturaId > 0) {
            viewModel.carregarCandidatura(candidaturaId);
            viewModel.carregarHistorico(candidaturaId);
        }
        viewModel.carregarStatusOpcoes();
    }

    private void preencherHistorico(View view, List<HistoricoStatusResponse> lista) {
        android.widget.LinearLayout container = view.findViewById(R.id.container_historico);
        TextView tvVazio = view.findViewById(R.id.tv_historico_vazio);
        if (container == null) return;
        container.removeAllViews();

        if (lista == null || lista.isEmpty()) {
            if (tvVazio != null) tvVazio.setVisibility(View.VISIBLE);
            return;
        }
        if (tvVazio != null) tvVazio.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (int i = 0; i < lista.size(); i++) {
            HistoricoStatusResponse h = lista.get(i);
            if (h == null) continue;
            View item = inflater.inflate(R.layout.item_historico_status, container, false);

            String transicao = (h.statusAnterior != null ? h.statusAnterior : "—")
                    + "  →  " + (h.statusNovo != null ? h.statusNovo : "—");
            ((TextView) item.findViewById(R.id.tv_historico_transicao)).setText(transicao);

            StringBuilder meta = new StringBuilder();
            if (h.usuarioNome != null && !h.usuarioNome.isEmpty()) meta.append("por ").append(h.usuarioNome);
            String quando = formatDateTime(h.dataHora);
            if (!quando.isEmpty()) {
                if (meta.length() > 0) meta.append(" • ");
                meta.append(quando);
            }
            ((TextView) item.findViewById(R.id.tv_historico_meta)).setText(meta.toString());

            // Esconde o divisor do último item.
            View divider = item.findViewById(R.id.view_historico_divider);
            if (divider != null) divider.setVisibility(i == lista.size() - 1 ? View.GONE : View.VISIBLE);

            container.addView(item);
        }
    }

    private void preencherStatusOpcoes(List<LookupItem> opcoes) {
        statusOpcoesRaw = opcoes;
        atualizarDropdownStatus();
    }

    /**
     * Rebuild do dropdown a partir das opções do lookup, filtrando pelas transições
     * permitidas pelo fluxo (espelha a regra do backend). Em status terminal a edição
     * é desabilitada. Chamado quando as opções e/ou a candidatura terminam de carregar.
     */
    private void atualizarDropdownStatus() {
        if (actvStatus == null || statusOpcoesRaw == null) return;

        String statusAtual = candidaturaAtual != null ? candidaturaAtual.status : null;

        statusMap.clear();
        List<String> labels = new java.util.ArrayList<>();
        for (LookupItem item : statusOpcoesRaw) {
            if (item == null || item.id == null) continue;
            String label = item.getLabel();
            if (label == null || label.isEmpty()) continue;
            if (!FluxoStatusCandidatura.transicaoPermitida(statusAtual, label)) continue;
            statusMap.put(label, item.id);
            labels.add(label);
        }

        actvStatus.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));
        if (statusAtual != null) actvStatus.setText(statusAtual, false);

        // Status terminal (Aprovado/Reprovado): sem transições possíveis → bloqueia edição.
        FluxoStatusCandidatura atual = FluxoStatusCandidatura.fromNome(statusAtual);
        boolean terminal = atual != null && atual.ehTerminal();
        if (tilStatus != null) {
            tilStatus.setEnabled(!terminal);
            tilStatus.setHelperText(terminal ? getString(R.string.status_final_aviso) : null);
        }
        if (btnSalvarStatus != null) btnSalvarStatus.setEnabled(!terminal);
    }

    private void preencherDados(View view, CandidaturaEmpresaResponse c) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null && c.nomeCandidato != null) toolbar.setTitle(c.nomeCandidato);

        if (c.nomeCandidato != null) {
            String[] partes = c.nomeCandidato.split(" ");
            String iniciais = partes.length >= 2
                    ? "" + partes[0].charAt(0) + partes[1].charAt(0)
                    : String.valueOf(partes[0].charAt(0));
            setText(view, R.id.tv_iniciais, iniciais.toUpperCase());
        }

        setText(view, R.id.tv_nome_candidato,   c.nomeCandidato);
        setText(view, R.id.tv_data_candidatura, "Candidatou-se em " + formatDate(c.dataCandidatura));
        setText(view, R.id.tv_titulo_vaga,       c.tituloVaga);
        setText(view, R.id.tv_status_atual,      c.status);
        setText(view, R.id.tv_email_candidato,   c.email);

        if (c.telefones != null && !c.telefones.isEmpty())
            setText(view, R.id.tv_telefone_candidato, String.join(" • ", c.telefones));
        else
            setText(view, R.id.tv_telefone_candidato, "Não informado");

        setText(view, R.id.tv_endereco, c.endereco);
        setInfoCompartilhada(view, R.id.tv_resumo,          c.objetivoProfissional);
        setInfoCompartilhada(view, R.id.tv_disponibilidade, c.disponibilidade);
        setInfoCompartilhada(view, R.id.tv_pretensao,       formatarPretensao(c.pretensaoSalarial));
        setInfoCompartilhadaLista(view, R.id.tv_experiencia,   formatarExperiencias(c.experiencias));
        setInfoCompartilhadaLista(view, R.id.tv_escolaridade,  formatarFormacoes(c.formacoes));
        setInfoCompartilhadaLista(view, R.id.tv_area_formacao, formatarAreaFormacao(c.formacoes));

        // Agora que o status atual é conhecido, refiltra o dropdown pelo fluxo.
        atualizarDropdownStatus();

        Log.d("Curriculo", "nomeArquivo=" + c.curriculoNomeArquivo + "  caminho=" + c.curriculoCaminho);
        boolean temCurriculo = c.curriculoNomeArquivo != null;
        if (btnVerCurriculo != null) {
            btnVerCurriculo.setEnabled(temCurriculo);
            btnVerCurriculo.setText(temCurriculo ? "Baixar Currículo" : "Currículo não compartilhado");
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

        btnVerCurriculo.setEnabled(false);
        btnVerCurriculo.setText("Obtendo link…");
        showLoading("Obtendo currículo…");

        String token = new SessionManager(requireContext()).getToken();
        String url = BuildConfig.BASE_URL + "api/candidaturas/" + candidaturaAtual.id + "/curriculo/download";

        OkHttpClient client = new OkHttpClient.Builder().followRedirects(false).build();
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + (token != null ? token : ""))
                .build();

        Handler handler = new Handler(Looper.getMainLooper());
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                handler.post(() -> {
                    if (!isAdded()) return;
                    resetarBotaoCurriculo();
                    showErroDialog(buildErroConexao(e));
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call,
                                   @NonNull okhttp3.Response response) throws IOException {
                String location = response.header("Location");
                response.close();
                if (location == null) {
                    handler.post(() -> {
                        if (!isAdded()) return;
                        resetarBotaoCurriculo();
                        showErroDialog("Link do currículo não encontrado. Tente novamente.");
                    });
                    return;
                }
                String urlCorrigida = corrigirUrlSupabase(location);
                baixarEAbrirArquivo(urlCorrigida, candidaturaAtual.curriculoNomeArquivo, handler);
            }
        });
    }

    private String corrigirUrlSupabase(String location) {
        if (location == null) return null;
        if (location.contains("/storage/v1/")) return location;
        return location.replace(".supabase.co/object/", ".supabase.co/storage/v1/object/");
    }

    private void baixarEAbrirArquivo(String url, String nomeArquivo, Handler handler) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            OkHttpClient downloader = new OkHttpClient();
            Request req = new Request.Builder().url(url).build();
            try (okhttp3.Response r = downloader.newCall(req).execute()) {
                if (!r.isSuccessful() || r.body() == null) {
                    handler.post(() -> {
                        if (!isAdded()) return;
                        resetarBotaoCurriculo();
                        showErroDialog("Erro ao baixar o currículo (código " + r.code() + ").");
                    });
                    return;
                }
                File dir = new File(requireContext().getCacheDir(), "curriculos");
                dir.mkdirs();
                File file = new File(dir, nomeArquivo);
                try (InputStream in = r.body().byteStream();
                     FileOutputStream out = new FileOutputStream(file)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
                }
                handler.post(() -> {
                    if (!isAdded()) return;
                    resetarBotaoCurriculo();
                    abrirArquivoLocal(file);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (!isAdded()) return;
                    resetarBotaoCurriculo();
                    showErroDialog("Erro ao salvar o arquivo do currículo. Tente novamente.");
                });
            }
        });
    }

    private void abrirArquivoLocal(File file) {
        Uri uri = FileProvider.getUriForFile(requireContext(),
                requireContext().getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setDataAndType(uri, "*/*");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                showErroDialog("Nenhum aplicativo encontrado para abrir o PDF.");
            }
        }
    }

    private void resetarBotaoCurriculo() {
        hideLoading();
        if (btnVerCurriculo != null) {
            btnVerCurriculo.setEnabled(true);
            btnVerCurriculo.setText("Baixar Currículo");
        }
    }

    private void salvarStatus(View btn) {
        if (candidaturaAtual == null) return;
        String label = actvStatus != null ? actvStatus.getText().toString().trim() : "";
        Long statusId = statusMap.get(label);
        if (statusId == null) {
            showErroDialog("Selecione um status válido antes de salvar.");
            return;
        }
        // Guard de no-op (espelha a regra do backend): status igual ao atual não faz nada.
        if (candidaturaAtual.status != null && candidaturaAtual.status.equalsIgnoreCase(label)) {
            Toast.makeText(requireContext(), R.string.status_inalterado, Toast.LENGTH_SHORT).show();
            return;
        }
        // Guard de transição (espelha o fluxo do backend) — defesa em profundidade.
        if (!FluxoStatusCandidatura.transicaoPermitida(candidaturaAtual.status, label)) {
            showErroDialog(getString(R.string.status_transicao_invalida));
            return;
        }
        btn.setEnabled(false);
        showLoading("Atualizando status…");
        viewModel.atualizarStatus(candidaturaAtual.id, statusId);
    }

    private void showLoading(String mensagem) {
        if (!isAdded() || (loadingDialog != null && loadingDialog.isShowing())) return;
        float dp = requireContext().getResources().getDisplayMetrics().density;
        android.widget.LinearLayout layout = new android.widget.LinearLayout(requireContext());
        layout.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        layout.setPadding((int)(24*dp), (int)(24*dp), (int)(24*dp), (int)(24*dp));
        layout.setGravity(android.view.Gravity.CENTER_VERTICAL);
        com.google.android.material.progressindicator.CircularProgressIndicator progress =
                new com.google.android.material.progressindicator.CircularProgressIndicator(requireContext());
        progress.setIndeterminate(true);
        layout.addView(progress);
        android.widget.TextView tv = new android.widget.TextView(requireContext());
        tv.setText(mensagem);
        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginStart((int)(16*dp));
        tv.setLayoutParams(lp);
        layout.addView(tv);
        loadingDialog = new AlertDialog.Builder(requireContext())
                .setView(layout).setCancelable(false).create();
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
        loadingDialog = null;
    }

    private void showErroDialog(String mensagem) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Erro").setMessage(mensagem).setPositiveButton("OK", null).show();
    }

    private void setText(View root, int id, String value) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(value != null ? value : "Não informado");
    }

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

    private void setInfoCompartilhadaLista(View root, int id, String formatted) {
        setInfoCompartilhada(root, id, formatted);
    }

    private String formatarPretensao(Double valor) {
        if (valor == null) return null;
        return String.format(new java.util.Locale("pt", "BR"), "R$ %.2f", valor);
    }

    private String formatarExperiencias(List<ExperienciaResponse> lista) {
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

    private String formatarFormacoes(List<FormacaoResponse> lista) {
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

    private String formatarAreaFormacao(List<FormacaoResponse> lista) {
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

    private String formatarPeriodo(String inicio, String fim) {
        if (inicio == null) return "";
        return inicio + " — " + (fim != null ? fim : "atual");
    }

    private String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        String[] p = iso.substring(0, 10).split("-");
        return p.length == 3 ? p[2] + "/" + p[1] + "/" + p[0] : iso;
    }

    // Formata ISO LocalDateTime ("2026-06-13T17:29:06") como "13/06/2026 às 17:29".
    private String formatDateTime(String iso) {
        if (iso == null || iso.length() < 10) return "";
        String data = formatDate(iso);
        int t = iso.indexOf('T');
        if (t < 0 || iso.length() < t + 6) return data;
        String hora = iso.substring(t + 1, t + 6); // HH:mm
        return data + " às " + hora;
    }

    private String buildErroConexao(Throwable t) {
        if (t instanceof UnknownHostException)
            return "Sem conexão com a internet. Verifique sua rede e tente novamente.";
        if (t instanceof SocketTimeoutException)
            return "Tempo limite excedido. Verifique sua conexão e tente novamente.";
        if (t instanceof IOException)
            return "Falha na comunicação com o servidor. Tente novamente.";
        return "Erro inesperado: " + t.getMessage();
    }
}
