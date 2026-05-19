package com.edu.matchvagasempresas.feature.gerenciar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.model.ExperienciaResponse;
import com.edu.matchvagasempresas.model.FormacaoResponse;
import android.util.Log;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.util.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.core.content.FileProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private CandidaturaEmpresaResponse candidaturaAtual;
    private AutoCompleteTextView actvStatus;
    private MaterialButton btnVerCurriculo;
    private SwipeRefreshLayout swipeRefresh;
    private long candidaturaIdAtual = -1;

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

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(() -> {
            if (candidaturaIdAtual > 0) carregarCandidatura(view, candidaturaIdAtual);
            else swipeRefresh.setRefreshing(false);
        });

        candidaturaIdAtual = getArguments() != null ? getArguments().getLong("candidaturaId", -1) : -1;
        if (candidaturaIdAtual > 0) carregarCandidatura(view, candidaturaIdAtual);
    }

    private void carregarCandidatura(View view, long candidaturaId) {
        ApiClient.getService(requireContext())
                .detalharCandidatura(candidaturaId)
                .enqueue(new Callback<CandidaturaEmpresaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CandidaturaEmpresaResponse> call,
                                           @NonNull Response<CandidaturaEmpresaResponse> response) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            candidaturaAtual = response.body();
                            preencherDados(view, candidaturaAtual);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CandidaturaEmpresaResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Erro ao carregar candidatura", Toast.LENGTH_SHORT).show();
                    }
                });
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

    // ── Download de currículo ────────────────────────────────────────────────

    /**
     * Chama o backend com followRedirects=false para obter a URL assinada do Supabase
     * no header Location, e abre essa URL diretamente no browser.
     *
     * Não encaminhamos o Bearer para o Supabase — a URL assinada já tem autenticação
     * via ?token= e o Supabase rejeita qualquer Authorization header externo.
     */
    private void baixarCurriculo() {
        if (candidaturaAtual == null || candidaturaAtual.curriculoNomeArquivo == null) return;

        btnVerCurriculo.setEnabled(false);
        btnVerCurriculo.setText("Obtendo link…");

        String token = new SessionManager(requireContext()).getToken();
        String url = ApiClient.BASE_URL + "/api/candidaturas/" + candidaturaAtual.id + "/curriculo/download";

        // Cliente sem redirect para capturar o Location do 302
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .build();

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
                    Toast.makeText(requireContext(), "Erro de conexão ao buscar currículo",
                            Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(requireContext(), "Link do currículo não encontrado",
                                Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Baixa o arquivo usando a URL assinada corrigida (sem auth header)
                String urlCorrigida = corrigirUrlSupabase(location);
                baixarEAbrirArquivo(urlCorrigida,
                        candidaturaAtual.curriculoNomeArquivo, handler);
            }
        });
    }

    /**
     * O backend gera a URL assinada sem o prefixo /storage/v1/ obrigatório:
     *   gerada:  .supabase.co/object/sign/...
     *   correta: .supabase.co/storage/v1/object/sign/...
     * Corrigimos no cliente antes de abrir no browser.
     */
    private String corrigirUrlSupabase(String location) {
        if (location == null) return null;
        if (location.contains("/storage/v1/")) return location;
        return location.replace(".supabase.co/object/", ".supabase.co/storage/v1/object/");
    }

    /** Baixa a URL assinada em background e abre o arquivo no viewer de PDF. */
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
                        Toast.makeText(requireContext(),
                                "Erro ao baixar o currículo (" + r.code() + ")",
                                Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), "Erro ao salvar o arquivo",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void abrirArquivoLocal(File file) {
        Uri uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Tenta abrindo com qualquer app que suporte o arquivo
            intent.setDataAndType(uri, "*/*");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(requireContext(), "Nenhum app para abrir o PDF",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void resetarBotaoCurriculo() {
        if (btnVerCurriculo != null) {
            btnVerCurriculo.setEnabled(true);
            btnVerCurriculo.setText("Baixar Currículo");
        }
    }

    // ── Atualização de status ────────────────────────────────────────────────

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

    // ── Helpers ──────────────────────────────────────────────────────────────

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
}
