package com.edu.matchvagasempresas.features.gerenciar;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.data.remote.dto.LookupItem;
import com.edu.matchvagasempresas.data.remote.dto.VagaRequest;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.edu.matchvagasempresas.data.repository.VagaRepositoryImpl;
import com.edu.matchvagasempresas.domain.repository.VagaRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GerenciarVagaFragment extends Fragment {

    private long vagaId;
    private VagaResponse vagaAtual;
    private final List<LookupItem> statusList = new ArrayList<>();
    private VagaRepository vagaRepository;

    private MaterialSwitch switchAtiva;
    private TextView tvTituloVaga;
    private TextView tvStatusBadge;
    private TextView tvNumCandidaturas;
    private MaterialButton btnSalvarStatus;
    private TextView tvTipoVagaInfo, tvModalidadeInfo, tvLocalInfo;
    private TextView tvNumVagasInfo, tvDataPublicacaoInfo, tvDataExpiracaoInfo;
    private TextInputEditText etDataExpiracao, etHoraExpiracao;
    private TextInputLayout tilDataExpiracao;
    private MaterialButton btnSalvarExpiracao;
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

        vagaRepository = new VagaRepositoryImpl(requireContext());

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        switchAtiva = view.findViewById(R.id.switch_ativa);
        tvTituloVaga = view.findViewById(R.id.tv_titulo_vaga);
        tvStatusBadge = view.findViewById(R.id.tv_status_vaga_badge);
        tvNumCandidaturas = view.findViewById(R.id.tv_num_candidaturas);
        btnSalvarStatus = view.findViewById(R.id.btn_salvar_status);
        tvTipoVagaInfo = view.findViewById(R.id.tv_tipo_vaga_info);
        tvModalidadeInfo = view.findViewById(R.id.tv_modalidade_info);
        tvLocalInfo = view.findViewById(R.id.tv_local_info);
        tvNumVagasInfo = view.findViewById(R.id.tv_num_vagas_info);
        tvDataPublicacaoInfo = view.findViewById(R.id.tv_data_publicacao_info);
        tvDataExpiracaoInfo = view.findViewById(R.id.tv_data_expiracao_info);
        etDataExpiracao = view.findViewById(R.id.et_data_expiracao);
        tilDataExpiracao = view.findViewById(R.id.til_data_expiracao);
        etHoraExpiracao = view.findViewById(R.id.et_hora_expiracao);
        btnSalvarExpiracao = view.findViewById(R.id.btn_salvar_expiracao);

        switchAtiva.setOnCheckedChangeListener((btn, checked) -> atualizarBadge(checked));
        etDataExpiracao.setOnClickListener(v -> showDatePicker(etDataExpiracao));
        tilDataExpiracao.setEndIconOnClickListener(v -> showDatePicker(etDataExpiracao));
        etHoraExpiracao.setOnClickListener(v -> showTimePicker(etHoraExpiracao));
        btnSalvarStatus.setOnClickListener(v -> salvarStatus(v));
        btnSalvarExpiracao.setOnClickListener(v -> salvarExpiracao());
        view.findViewById(R.id.btn_deletar).setOnClickListener(v -> confirmDelete(v));

        vagaId = getArguments() != null ? getArguments().getLong("vagaId", -1) : -1;
        carregarStatus();
        if (vagaId > 0) {
            carregarVaga();
            carregarNumeroCandidaturas();
        }
    }

    private void carregarStatus() {
        RetrofitClient.getService(requireContext()).listarStatusVaga()
                .enqueue(new retrofit2.Callback<List<LookupItem>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<List<LookupItem>> call,
                                           @NonNull retrofit2.Response<List<LookupItem>> r) {
                        if (r.isSuccessful() && r.body() != null) {
                            statusList.clear();
                            statusList.addAll(r.body());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull retrofit2.Call<List<LookupItem>> call,
                                          @NonNull Throwable t) {}
                });
    }

    private void carregarVaga() {
        vagaRepository.buscarVaga(vagaId, new VagaRepository.Callback<VagaResponse>() {
            @Override
            public void onSuccess(VagaResponse data) {
                if (!isAdded()) return;
                vagaAtual = data;
                preencherTela();
            }
            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                showErroDialog(message);
            }
        });
    }

    private void preencherTela() {
        tvTituloVaga.setText(vagaAtual.titulo);
        boolean ativa = vagaAtual.statusDescricao != null
                && vagaAtual.statusDescricao.equalsIgnoreCase("ATIVA");
        switchAtiva.setChecked(ativa);
        atualizarBadge(ativa);
        tvTipoVagaInfo.setText(vagaAtual.tipoVagaDescricao != null ? vagaAtual.tipoVagaDescricao : "—");
        tvModalidadeInfo.setText(vagaAtual.modalidadeDescricao != null ? vagaAtual.modalidadeDescricao : "—");
        String local = "—";
        if (vagaAtual.nomeCidade != null) {
            local = vagaAtual.nomeCidade;
            if (vagaAtual.ufEstado != null) local += " - " + vagaAtual.ufEstado;
        }
        tvLocalInfo.setText(local);
        tvNumVagasInfo.setText(vagaAtual.numeroVagas != null ? String.valueOf(vagaAtual.numeroVagas) : "—");
        tvDataPublicacaoInfo.setText(formatarData(vagaAtual.dataPublicacao));
        tvDataExpiracaoInfo.setText(formatarData(vagaAtual.dataExpiracao));
        preencherCamposExpiracao();
    }

    private void preencherCamposExpiracao() {
        if (vagaAtual.dataExpiracao == null || vagaAtual.dataExpiracao.isEmpty()) return;
        try {
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            isoFmt.setLenient(true);
            Date date = isoFmt.parse(vagaAtual.dataExpiracao);
            if (date != null) {
                etDataExpiracao.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date));
                etHoraExpiracao.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date));
            }
        } catch (ParseException ignored) {}
    }

    private String formatarData(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        try {
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            isoFmt.setLenient(true);
            Date date = isoFmt.parse(iso);
            if (date != null) return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
        } catch (ParseException ignored) {}
        return iso;
    }

    private void carregarNumeroCandidaturas() {
        RetrofitClient.getService(requireContext()).candidatosPorVaga(vagaId)
                .enqueue(new retrofit2.Callback<List<com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<List<com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse>> call,
                                           @NonNull retrofit2.Response<List<com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse>> r) {
                        if (!isAdded() || tvNumCandidaturas == null) return;
                        if (r.isSuccessful() && r.body() != null) {
                            int total = r.body().size();
                            tvNumCandidaturas.setText(total + (total == 1 ? " candidatura" : " candidaturas"));
                        }
                    }
                    @Override
                    public void onFailure(@NonNull retrofit2.Call<List<com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse>> call,
                                          @NonNull Throwable t) {}
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
        vagaRepository.atualizarVaga(vagaId, construirRequest(statusIdFinal),
                new VagaRepository.Callback<VagaResponse>() {
                    @Override
                    public void onSuccess(VagaResponse data) {
                        if (!isAdded()) return;
                        hideLoading();
                        btnSalvarStatus.setEnabled(true);
                        vagaAtual = data;
                        Toast.makeText(requireContext(),
                                querAtiva ? "Vaga ativada com sucesso" : "Vaga desativada com sucesso",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(String message) {
                        if (!isAdded()) return;
                        hideLoading();
                        btnSalvarStatus.setEnabled(true);
                        showErroDialog(message);
                        switchAtiva.setChecked(!querAtiva);
                        atualizarBadge(!querAtiva);
                    }
                });
    }

    private void salvarExpiracao() {
        if (vagaAtual == null || vagaId <= 0) {
            showErroDialog("Vaga não carregada. Tente novamente.");
            return;
        }
        tilDataExpiracao.setError(null);
        String dataStr = etDataExpiracao.getText() != null ? etDataExpiracao.getText().toString().trim() : "";
        String horaStr = etHoraExpiracao.getText() != null ? etHoraExpiracao.getText().toString().trim() : "";

        if (dataStr.isEmpty()) {
            tilDataExpiracao.setError("Informe a data de expiração");
            return;
        }

        String iso;
        try {
            SimpleDateFormat displayFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date data = displayFmt.parse(dataStr);
            SimpleDateFormat isoDateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String hora = horaStr.isEmpty() ? "23:59:59" : horaStr + ":00";
            iso = isoDateFmt.format(data) + "T" + hora;
        } catch (ParseException e) {
            tilDataExpiracao.setError("Data inválida");
            return;
        }

        btnSalvarExpiracao.setEnabled(false);
        showLoading("Salvando data de expiração…");
        VagaRequest req = construirRequest(vagaAtual.statusVagaId);
        req.dataExpiracao = iso;
        vagaRepository.atualizarVaga(vagaId, req, new VagaRepository.Callback<VagaResponse>() {
            @Override
            public void onSuccess(VagaResponse data) {
                if (!isAdded()) return;
                hideLoading();
                btnSalvarExpiracao.setEnabled(true);
                vagaAtual = data;
                if (tvDataExpiracaoInfo != null)
                    tvDataExpiracaoInfo.setText(formatarData(vagaAtual.dataExpiracao));
                Toast.makeText(requireContext(), "Data de expiração salva", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                hideLoading();
                btnSalvarExpiracao.setEnabled(true);
                showErroDialog(message);
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
        vagaRepository.deletarVaga(vagaId, new VagaRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                hideLoading();
                Toast.makeText(requireContext(), "Vaga excluída", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(anchor).navigateUp();
            }
            @Override
            public void onError(String message) {
                if (!isAdded()) return;
                hideLoading();
                showErroDialog(message);
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

    private void showDatePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(),
                (v, year, month, day) -> et.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        new android.app.TimePickerDialog(requireContext(),
                (v, hour, minute) -> et.setText(
                        String.format(Locale.getDefault(), "%02d:%02d", hour, minute)),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }
}
