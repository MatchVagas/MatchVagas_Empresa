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

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.data.local.SessionManager;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.ExperienciaResponse;
import com.edu.matchvagasempresas.data.remote.dto.FormacaoResponse;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DetalhesCandidaturaFragment extends Fragment {

    private static final Map<String, Long> STATUS_MAP = new LinkedHashMap<>();
    static {
        STATUS_MAP.put("Em análise",       1L);
        STATUS_MAP.put("Aprovado",         2L);
        STATUS_MAP.put("Reprovado",        3L);
        STATUS_MAP.put("Em entrevista",    4L);
        STATUS_MAP.put("Proposta Enviada", 5L);
        STATUS_MAP.put("Contratado",       6L);
        STATUS_MAP.put("Desistiu",         7L);
    }

    private DetalhesCandidaturaViewModel viewModel;
    private CandidaturaEmpresaResponse candidaturaAtual;
    private AutoCompleteTextView actvStatus;
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
        List<String> labels = Arrays.asList(STATUS_MAP.keySet().toArray(new String[0]));
        actvStatus.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));

        btnVerCurriculo = view.findViewById(R.id.btn_ver_curriculo);
        btnVerCurriculo.setEnabled(false);
        btnVerCurriculo.setOnClickListener(v -> baixarCurriculo());

        view.findViewById(R.id.btn_salvar_status).setOnClickListener(v -> salvarStatus(v));

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(() -> {
            if (candidaturaId > 0) viewModel.carregarCandidatura(candidaturaId);
            else swipeRefresh.setRefreshing(false);
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
                        Toast.makeText(requireContext(), "Status atualizado!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ERROR:
                    showErroDialog(resource.getMessage());
                    break;
                default: break;
            }
        });

        candidaturaId = getArguments() != null ? getArguments().getLong("candidaturaId", -1) : -1;
        if (candidaturaId > 0) viewModel.carregarCandidatura(candidaturaId);
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
        setText(view, R.id.tv_email_candidato,   "");

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

        if (c.status != null && actvStatus != null) actvStatus.setText(c.status, false);

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
        String url = RetrofitClient.BASE_URL + "/api/candidaturas/" + candidaturaAtual.id + "/curriculo/download";

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
        Long statusId = STATUS_MAP.get(label);
        if (statusId == null) {
            showErroDialog("Selecione um status válido antes de salvar.");
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
