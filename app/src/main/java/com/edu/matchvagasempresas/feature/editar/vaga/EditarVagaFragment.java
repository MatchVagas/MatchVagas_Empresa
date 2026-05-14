package com.edu.matchvagasempresas.feature.editar.vaga;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.LookupItem;
import com.edu.matchvagasempresas.model.VagaRequest;
import com.edu.matchvagasempresas.model.VagaResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.LookupCache;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarVagaFragment extends Fragment {

    private AutoCompleteTextView actvTipo, actvModalidade, actvEscolaridade, actvCidade;
    private VagaResponse vagaAtual;
    private long vagaId;
    private boolean lookupsProntos = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_editar_vaga, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        actvTipo = view.findViewById(R.id.actv_tipo_vaga);
        actvModalidade = view.findViewById(R.id.actv_modalidade);
        actvEscolaridade = view.findViewById(R.id.actv_escolaridade);
        actvCidade = view.findViewById(R.id.actv_cidade);

        setupDatePicker(view);

        vagaId = getArguments() != null ? getArguments().getLong("vagaId", -1) : -1;

        carregarLookups(view);
        if (vagaId > 0) carregarVaga(view, vagaId);

        view.findViewById(R.id.btn_atualizar).setOnClickListener(v -> atualizarVaga(view, v));
    }

    private void carregarLookups(View view) {
        LookupCache.get().preload(requireContext(), () -> {
            if (!isAdded()) return;
            bindDropdown(actvTipo, LookupCache.get().getTiposVaga());
            bindDropdown(actvModalidade, LookupCache.get().getModalidades());
            bindDropdown(actvEscolaridade, LookupCache.get().getEscolaridades());
            bindDropdown(actvCidade, LookupCache.get().getCidades());
            lookupsProntos = true;
            if (vagaAtual != null) preencherDropdowns();
        });
    }

    private void bindDropdown(AutoCompleteTextView actv, List<LookupItem> items) {
        List<String> labels = new ArrayList<>();
        for (LookupItem item : items) labels.add(item.getLabel());
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));
    }

    private void carregarVaga(View view, long id) {
        ApiClient.getService(requireContext()).buscarVaga(id).enqueue(new Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call,
                                   @NonNull Response<VagaResponse> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    vagaAtual = r.body();
                    preencherFormulario(view, vagaAtual);
                    if (lookupsProntos) preencherDropdowns();
                }
            }

            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro ao carregar vaga", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preencherFormulario(View view, VagaResponse v) {
        setEditText(view, R.id.et_titulo, v.titulo);
        setEditText(view, R.id.et_area_atuacao, v.areaAtuacao);
        setEditText(view, R.id.et_salario_min, v.salarioMinimo != null ? String.valueOf(v.salarioMinimo.intValue()) : "");
        setEditText(view, R.id.et_salario_max, v.salarioMaximo != null ? String.valueOf(v.salarioMaximo.intValue()) : "");
        setEditText(view, R.id.et_carga_horaria, v.cargaHoraria);
        setEditText(view, R.id.et_num_vagas, v.numeroVagas != null ? String.valueOf(v.numeroVagas) : "");
        setEditText(view, R.id.et_data_expiracao, formatDateBr(v.dataExpiracao));
    }

    private void preencherDropdowns() {
        if (vagaAtual == null) return;
        if (vagaAtual.tipoVagaDescricao != null) actvTipo.setText(vagaAtual.tipoVagaDescricao, false);
        if (vagaAtual.modalidadeDescricao != null) actvModalidade.setText(vagaAtual.modalidadeDescricao, false);
        if (vagaAtual.escolaridadeNome != null) actvEscolaridade.setText(vagaAtual.escolaridadeNome, false);
        if (vagaAtual.nomeCidade != null && vagaAtual.ufEstado != null)
            actvCidade.setText(vagaAtual.nomeCidade + " - " + vagaAtual.ufEstado, false);
    }

    private void atualizarVaga(View view, View btn) {
        if (vagaAtual == null || vagaId <= 0) return;

        TextInputEditText etTitulo = view.findViewById(R.id.et_titulo);
        TextInputEditText etArea = view.findViewById(R.id.et_area_atuacao);
        TextInputEditText etDesc = view.findViewById(R.id.et_descricao);
        TextInputEditText etReq = view.findViewById(R.id.et_requisitos);
        TextInputEditText etSalMin = view.findViewById(R.id.et_salario_min);
        TextInputEditText etSalMax = view.findViewById(R.id.et_salario_max);
        TextInputEditText etCarga = view.findViewById(R.id.et_carga_horaria);
        TextInputEditText etNVagas = view.findViewById(R.id.et_num_vagas);
        TextInputEditText etData = view.findViewById(R.id.et_data_expiracao);

        VagaRequest req = new VagaRequest();
        req.empresaId = vagaAtual.empresaId;
        req.titulo = getText(etTitulo);
        req.descricao = getText(etDesc) .isEmpty() ? vagaAtual.descricao : getText(etDesc);
        req.requisitos = getText(etReq).isEmpty() ? vagaAtual.requisitos : getText(etReq);
        req.areaAtuacao = getText(etArea);
        req.tipoVagaId = getSelectedId(LookupCache.get().getTiposVaga(), actvTipo.getText().toString(), vagaAtual.tipoVagaId);
        req.modalidadeId = getSelectedId(LookupCache.get().getModalidades(), actvModalidade.getText().toString(), vagaAtual.modalidadeId);
        req.escolaridadeId = getSelectedId(LookupCache.get().getEscolaridades(), actvEscolaridade.getText().toString(), vagaAtual.escolaridadeId);
        req.cidadeId = getSelectedId(LookupCache.get().getCidades(), actvCidade.getText().toString(), vagaAtual.cidadeId);
        req.statusVagaId = vagaAtual.statusVagaId;
        req.salarioMinimo = parseDouble(getText(etSalMin), vagaAtual.salarioMinimo != null ? vagaAtual.salarioMinimo : 0.0);
        req.salarioMaximo = parseDouble(getText(etSalMax), vagaAtual.salarioMaximo != null ? vagaAtual.salarioMaximo : 0.0);
        req.cargaHoraria = getText(etCarga);
        req.numeroVagas = parseInt(getText(etNVagas), vagaAtual.numeroVagas != null ? vagaAtual.numeroVagas : 1);
        req.beneficios = vagaAtual.beneficios;
        String dataInput = getText(etData);
        req.dataExpiracao = dataInput.isEmpty() ? vagaAtual.dataExpiracao : parseIsoDate(dataInput);

        ((MaterialButton) btn).setEnabled(false);
        ApiClient.getService(requireContext()).atualizarVaga(vagaId, req).enqueue(new Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call, @NonNull Response<VagaResponse> r) {
                if (!isAdded()) return;
                ((MaterialButton) btn).setEnabled(true);
                if (r.isSuccessful()) {
                    Toast.makeText(requireContext(), "Vaga atualizada!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(btn).navigateUp();
                } else {
                    Toast.makeText(requireContext(), "Erro ao atualizar vaga", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                ((MaterialButton) btn).setEnabled(true);
                Toast.makeText(requireContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDatePicker(View view) {
        TextInputEditText etData = view.findViewById(R.id.et_data_expiracao);
        TextInputLayout tilData = view.findViewById(R.id.til_data_expiracao);
        if (etData != null) etData.setOnClickListener(v -> showDatePicker(etData));
        if (tilData != null) tilData.setEndIconOnClickListener(v -> showDatePicker(etData));
    }

    private void showDatePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (v, year, month, day) -> et.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setEditText(View root, int id, String value) {
        TextInputEditText et = root.findViewById(id);
        if (et != null && value != null) et.setText(value);
    }

    private String getText(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private Long getSelectedId(List<LookupItem> list, String label, Long fallback) {
        for (LookupItem item : list) {
            if (item.getLabel().equals(label)) return item.id;
        }
        return fallback;
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.replace(",", ".")); } catch (NumberFormatException e) { return def; }
    }

    private String parseIsoDate(String dataBr) {
        if (dataBr == null || dataBr.length() < 10) return null;
        String[] parts = dataBr.split("/");
        if (parts.length == 3) return parts[2] + "-" + parts[1] + "-" + parts[0] + "T23:59:59";
        return null;
    }

    private String formatDateBr(String iso) {
        if (iso == null || iso.length() < 10) return "";
        String[] parts = iso.substring(0, 10).split("-");
        if (parts.length == 3) return parts[2] + "/" + parts[1] + "/" + parts[0];
        return "";
    }
}
