package com.edu.matchvagasempresas.feature.gerenciar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.LookupItem;
import com.edu.matchvagasempresas.model.VagaRequest;
import com.edu.matchvagasempresas.model.VagaResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GerenciarVagaFragment extends Fragment {

    private long vagaId;
    private VagaResponse vagaAtual;
    private final List<LookupItem> statusList = new ArrayList<>();
    private MaterialSwitch switchAtiva;
    private TextView tvTituloVaga;
    private TextView tvStatusBadge;
    private MaterialButton btnSalvarStatus;
    private AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_gerenciar_vaga, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        switchAtiva = view.findViewById(R.id.switch_ativa);
        tvTituloVaga = view.findViewById(R.id.tv_titulo_vaga);
        tvStatusBadge = view.findViewById(R.id.tv_status_vaga_badge);
        btnSalvarStatus = view.findViewById(R.id.btn_salvar_status);

        switchAtiva.setOnCheckedChangeListener((btn, checked) ->
                atualizarBadge(checked));

        TextInputEditText etData = view.findViewById(R.id.et_data_publicacao);
        TextInputLayout tilData = view.findViewById(R.id.til_data_publicacao);
        TextInputEditText etHora = view.findViewById(R.id.et_hora_publicacao);

        etData.setOnClickListener(v -> showDatePicker(etData));
        tilData.setEndIconOnClickListener(v -> showDatePicker(etData));
        etHora.setOnClickListener(v -> showTimePicker(etHora));

        btnSalvarStatus.setOnClickListener(v -> salvarStatus(v));
        view.findViewById(R.id.btn_salvar_programacao).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Funcionalidade em breve", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btn_deletar).setOnClickListener(v -> confirmDelete(v));

        vagaId = getArguments() != null ? getArguments().getLong("vagaId", -1) : -1;
        carregarStatus();
        if (vagaId > 0) carregarVaga();
    }

    private void carregarStatus() {
        ApiClient.getService(requireContext()).listarStatusVaga().enqueue(new Callback<List<LookupItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<LookupItem>> call,
                                   @NonNull Response<List<LookupItem>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    statusList.clear();
                    statusList.addAll(r.body());
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<LookupItem>> call, @NonNull Throwable t) { }
        });
    }

    private void carregarVaga() {
        ApiClient.getService(requireContext()).buscarVaga(vagaId).enqueue(new Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call,
                                   @NonNull Response<VagaResponse> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    vagaAtual = r.body();
                    if (tvTituloVaga != null) tvTituloVaga.setText(vagaAtual.titulo);
                    boolean ativa = vagaAtual.statusDescricao != null
                            && vagaAtual.statusDescricao.equalsIgnoreCase("ATIVA");
                    switchAtiva.setChecked(ativa);
                    atualizarBadge(ativa);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showErroDialog(buildErroConexao(t));
            }
        });
    }

    private void atualizarBadge(boolean ativa) {
        if (tvStatusBadge == null) return;
        if (ativa) {
            tvStatusBadge.setText("● Ativa");
            tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#80FF80"));
        } else {
            tvStatusBadge.setText("● Inativa");
            tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#FFCCCC"));
        }
    }

    private void salvarStatus(View anchor) {
        if (vagaAtual == null || vagaId <= 0) {
            showErroDialog("Vaga não carregada. Tente novamente.");
            return;
        }

        boolean querAtiva = switchAtiva.isChecked();
        Long novoStatusId = encontrarStatusId(querAtiva ? "ATIVA" : "ENCERRADA");
        if (novoStatusId == null) novoStatusId = vagaAtual.statusVagaId;

        btnSalvarStatus.setEnabled(false);
        showLoading("Atualizando status…");
        final Long statusIdFinal = novoStatusId;
        VagaRequest req = construirRequest(statusIdFinal);
        ApiClient.getService(requireContext()).atualizarVaga(vagaId, req).enqueue(new Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call,
                                   @NonNull Response<VagaResponse> r) {
                if (!isAdded()) return;
                hideLoading();
                btnSalvarStatus.setEnabled(true);
                if (r.isSuccessful() && r.body() != null) {
                    vagaAtual = r.body();
                    com.edu.matchvagasempresas.network.DataCache.get().invalidateVagas(requireContext());
                    Toast.makeText(requireContext(),
                            querAtiva ? "Vaga ativada com sucesso" : "Vaga desativada com sucesso",
                            Toast.LENGTH_SHORT).show();
                } else {
                    showErroDialog(buildErroHttp(r));
                    switchAtiva.setChecked(!querAtiva);
                    atualizarBadge(!querAtiva);
                }
            }
            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                hideLoading();
                btnSalvarStatus.setEnabled(true);
                showErroDialog(buildErroConexao(t));
                switchAtiva.setChecked(!querAtiva);
                atualizarBadge(!querAtiva);
            }
        });
    }

    private Long encontrarStatusId(String nome) {
        for (LookupItem item : statusList) {
            if (nome.equalsIgnoreCase(item.descricao) || nome.equalsIgnoreCase(item.nome)) return item.id;
        }
        return null;
    }

    private VagaRequest construirRequest(Long statusId) {
        VagaRequest req = new VagaRequest();
        req.empresaId = vagaAtual.empresaId;
        req.titulo = vagaAtual.titulo;
        req.descricao = vagaAtual.descricao;
        req.requisitos = vagaAtual.requisitos;
        req.tipoVagaId = vagaAtual.tipoVagaId;
        req.modalidadeId = vagaAtual.modalidadeId;
        req.escolaridadeId = vagaAtual.escolaridadeId;
        req.cidadeId = vagaAtual.cidadeId;
        req.salarioMinimo = vagaAtual.salarioMinimo;
        req.salarioMaximo = vagaAtual.salarioMaximo;
        req.cargaHoraria = vagaAtual.cargaHoraria;
        req.numeroVagas = vagaAtual.numeroVagas;
        req.areaAtuacao = vagaAtual.areaAtuacao;
        req.beneficios = vagaAtual.beneficios;
        req.idadeMinima = vagaAtual.idadeMinima;
        req.idadeMaxima = vagaAtual.idadeMaxima;
        req.dataExpiracao = vagaAtual.dataExpiracao;
        req.statusVagaId = statusId;
        return req;
    }

    private void confirmDelete(View anchor) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_deletar_titulo)
                .setMessage(R.string.confirm_deletar_msg)
                .setPositiveButton(R.string.btn_confirmar, (d, w) -> deletarVaga(anchor))
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    private void deletarVaga(View anchor) {
        if (vagaId <= 0) return;
        showLoading("Excluindo vaga…");
        ApiClient.getService(requireContext()).deletarVaga(vagaId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> r) {
                if (!isAdded()) return;
                hideLoading();
                if (r.isSuccessful()) {
                    com.edu.matchvagasempresas.network.DataCache.get().invalidateVagas(requireContext());
                    Toast.makeText(requireContext(), "Vaga excluída", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(anchor).navigateUp();
                } else {
                    showErroDialog(buildErroHttp(r));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                hideLoading();
                showErroDialog(buildErroConexao(t));
            }
        });
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
                .setView(layout)
                .setCancelable(false)
                .create();
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
        loadingDialog = null;
    }

    private void showErroDialog(String mensagem) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Erro")
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }

    private String buildErroHttp(Response<?> r) {
        String corpo = null;
        if (r.errorBody() != null) {
            try { corpo = r.errorBody().string(); } catch (IOException ignored) {}
        }
        if (corpo != null && !corpo.isEmpty()) {
            try {
                org.json.JSONObject json = new org.json.JSONObject(corpo);
                StringBuilder sb = new StringBuilder();
                if (json.has("message")) sb.append(json.getString("message"));
                if (json.has("erros")) {
                    org.json.JSONObject erros = json.getJSONObject("erros");
                    java.util.Iterator<String> keys = erros.keys();
                    if (keys.hasNext()) {
                        if (sb.length() > 0) sb.append("\n\n");
                        while (keys.hasNext()) {
                            sb.append("• ").append(erros.getString(keys.next())).append("\n");
                        }
                    }
                }
                if (sb.length() > 0) return sb.toString().trim();
                for (String campo : new String[]{"error", "detail"}) {
                    if (json.has(campo)) return json.getString(campo);
                }
            } catch (org.json.JSONException ignored) {}
        }
        switch (r.code()) {
            case 400: return "Dados inválidos. Verifique as informações e tente novamente.";
            case 401: return "Sessão expirada. Faça login novamente.";
            case 403: return "Você não tem permissão para realizar esta operação.";
            case 404: return "Vaga não encontrada. Ela pode ter sido removida.";
            case 409: return "Conflito ao salvar. Os dados podem ter sido alterados por outro acesso.";
            case 500: return "Erro interno no servidor. Tente novamente mais tarde.";
            case 503: return "Serviço temporariamente indisponível. Tente novamente em instantes.";
            default:  return "Erro na operação (código " + r.code() + "). Tente novamente.";
        }
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

    private void showDatePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (v, year, month, day) -> et.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (v, hour, minute) -> et.setText(
                        String.format(Locale.getDefault(), "%02d:%02d", hour, minute)),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
                .show();
    }
}
