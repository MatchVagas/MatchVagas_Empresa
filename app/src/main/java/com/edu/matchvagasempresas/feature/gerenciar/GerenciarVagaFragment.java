package com.edu.matchvagasempresas.feature.gerenciar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

        TextInputEditText etData = view.findViewById(R.id.et_data_publicacao);
        TextInputLayout tilData = view.findViewById(R.id.til_data_publicacao);
        TextInputEditText etHora = view.findViewById(R.id.et_hora_publicacao);

        etData.setOnClickListener(v -> showDatePicker(etData));
        tilData.setEndIconOnClickListener(v -> showDatePicker(etData));
        etHora.setOnClickListener(v -> showTimePicker(etHora));

        view.findViewById(R.id.btn_salvar_programacao).setOnClickListener(v -> salvarStatus(v));
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
                    boolean ativa = "ATIVA".equalsIgnoreCase(vagaAtual.statusDescricao)
                            || "Ativa".equalsIgnoreCase(vagaAtual.statusDescricao);
                    switchAtiva.setChecked(ativa);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro ao carregar vaga", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void salvarStatus(View anchor) {
        if (vagaAtual == null || vagaId <= 0) {
            Toast.makeText(requireContext(), "Vaga não carregada", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean querAtiva = switchAtiva.isChecked();
        Long novoStatusId = encontrarStatusId(querAtiva ? "ATIVA" : "ENCERRADA");
        if (novoStatusId == null) novoStatusId = vagaAtual.statusVagaId;

        VagaRequest req = construirRequest(novoStatusId);
        ApiClient.getService(requireContext()).atualizarVaga(vagaId, req).enqueue(new Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call,
                                   @NonNull Response<VagaResponse> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    vagaAtual = r.body();
                    Toast.makeText(requireContext(),
                            querAtiva ? "Vaga ativada" : "Vaga desativada",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Erro ao atualizar status", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
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
        ApiClient.getService(requireContext()).deletarVaga(vagaId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> r) {
                if (!isAdded()) return;
                if (r.isSuccessful()) {
                    com.edu.matchvagasempresas.network.DataCache.get().invalidateVagas(requireContext());
                    Toast.makeText(requireContext(), "Vaga excluída", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(anchor).navigateUp();
                } else {
                    Toast.makeText(requireContext(), "Erro ao excluir vaga", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
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
