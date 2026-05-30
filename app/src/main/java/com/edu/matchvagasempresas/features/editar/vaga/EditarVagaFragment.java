package com.edu.matchvagasempresas.features.editar.vaga;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.edu.matchvagasempresas.data.repository.LookupRepositoryImpl;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditarVagaFragment extends Fragment {

    private EditarVagaViewModel viewModel;
    private AutoCompleteTextView actvTipo, actvModalidade, actvEscolaridade, actvCidade;
    private TextInputLayout tilTitulo, tilArea, tilDesc, tilTipo, tilModalidade,
            tilRequisitos, tilEscolaridade, tilCidade;
    private TextInputEditText etTitulo, etArea, etDesc, etReq;
    private VagaResponse vagaAtual;
    private long vagaId;
    private boolean lookupsProntos = false;
    private AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_editar_vaga, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EditarVagaViewModel.class);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        actvTipo = view.findViewById(R.id.actv_tipo_vaga);
        actvModalidade = view.findViewById(R.id.actv_modalidade);
        actvEscolaridade = view.findViewById(R.id.actv_escolaridade);
        actvCidade = view.findViewById(R.id.actv_cidade);
        tilTitulo = view.findViewById(R.id.til_titulo);
        tilArea = view.findViewById(R.id.til_area_atuacao);
        tilDesc = view.findViewById(R.id.til_descricao);
        tilTipo = view.findViewById(R.id.til_tipo_vaga);
        tilModalidade = view.findViewById(R.id.til_modalidade);
        tilRequisitos = view.findViewById(R.id.til_requisitos);
        tilEscolaridade = view.findViewById(R.id.til_escolaridade);
        tilCidade = view.findViewById(R.id.til_cidade);
        etTitulo = view.findViewById(R.id.et_titulo);
        etArea = view.findViewById(R.id.et_area_atuacao);
        etDesc = view.findViewById(R.id.et_descricao);
        etReq = view.findViewById(R.id.et_requisitos);

        setupErrorClearing();
        setupDatePicker(view);

        vagaId = getArguments() != null ? getArguments().getLong("vagaId", -1) : -1;

        viewModel.getVagaCarregada().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case SUCCESS:
                    vagaAtual = resource.getData();
                    preencherFormulario(view, vagaAtual);
                    if (lookupsProntos) preencherDropdowns();
                    break;
                case ERROR:
                    showErroDialog(resource.getMessage());
                    break;
                default: break;
            }
        });

        viewModel.getAtualizarResult().observe(getViewLifecycleOwner(), resource -> {
            hideLoading();
            view.findViewById(R.id.btn_atualizar).setEnabled(true);
            switch (resource.getStatus()) {
                case SUCCESS:
                    Toast.makeText(requireContext(), "Vaga atualizada!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view.findViewById(R.id.btn_atualizar)).navigateUp();
                    break;
                case ERROR:
                    showErroDialog(resource.getMessage());
                    break;
                default: break;
            }
        });

        carregarLookups(view);
        if (vagaId > 0) viewModel.carregarVaga(vagaId);

        view.findViewById(R.id.btn_atualizar).setOnClickListener(v -> atualizarVaga(view, v));
    }

    private void setupErrorClearing() {
        clearOnType(etTitulo, tilTitulo);
        clearOnType(etArea, tilArea);
        clearOnType(etDesc, tilDesc);
        clearOnType(etReq, tilRequisitos);
        actvTipo.setOnItemClickListener((p, v, i, id) -> tilTipo.setError(null));
        actvModalidade.setOnItemClickListener((p, v, i, id) -> tilModalidade.setError(null));
        actvEscolaridade.setOnItemClickListener((p, v, i, id) -> tilEscolaridade.setError(null));
        actvCidade.setOnItemClickListener((p, v, i, id) -> tilCidade.setError(null));
    }

    private void clearOnType(TextInputEditText et, TextInputLayout til) {
        et.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { til.setError(null); }
            public void afterTextChanged(Editable s) {}
        });
    }

    private void carregarLookups(View view) {
        LookupRepositoryImpl.get(requireContext()).preload(() -> {
            if (!isAdded()) return;
            LookupRepositoryImpl repo = LookupRepositoryImpl.get(requireContext());
            bindDropdown(actvTipo, repo.getTiposVaga());
            bindDropdown(actvModalidade, repo.getModalidades());
            bindDropdown(actvEscolaridade, repo.getEscolaridades());
            bindDropdown(actvCidade, repo.getCidades());
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

    private void preencherFormulario(View view, VagaResponse v) {
        setEditText(view, R.id.et_titulo,        v.titulo);
        setEditText(view, R.id.et_area_atuacao,  v.areaAtuacao);
        setEditText(view, R.id.et_descricao,     v.descricao);
        setEditText(view, R.id.et_requisitos,    v.requisitos);
        setEditText(view, R.id.et_beneficios,    v.beneficios);
        setEditText(view, R.id.et_salario_min,   v.salarioMinimo != null ? formatDecimal(v.salarioMinimo) : "");
        setEditText(view, R.id.et_salario_max,   v.salarioMaximo != null ? formatDecimal(v.salarioMaximo) : "");
        setEditText(view, R.id.et_carga_horaria, v.cargaHoraria);
        setEditText(view, R.id.et_num_vagas,     v.numeroVagas != null ? String.valueOf(v.numeroVagas) : "");
        setEditText(view, R.id.et_idade_min,     v.idadeMinima != null ? String.valueOf(v.idadeMinima) : "");
        setEditText(view, R.id.et_idade_max,     v.idadeMaxima != null ? String.valueOf(v.idadeMaxima) : "");
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
        if (!validarFormulario()) return;

        TextInputEditText etBenef  = view.findViewById(R.id.et_beneficios);
        TextInputEditText etSalMin = view.findViewById(R.id.et_salario_min);
        TextInputEditText etSalMax = view.findViewById(R.id.et_salario_max);
        TextInputEditText etCarga  = view.findViewById(R.id.et_carga_horaria);
        TextInputEditText etNVagas = view.findViewById(R.id.et_num_vagas);
        TextInputEditText etIdMin  = view.findViewById(R.id.et_idade_min);
        TextInputEditText etIdMax  = view.findViewById(R.id.et_idade_max);
        TextInputEditText etData   = view.findViewById(R.id.et_data_expiracao);

        LookupRepositoryImpl repo = LookupRepositoryImpl.get(requireContext());
        VagaRequest req = new VagaRequest();
        req.empresaId    = vagaAtual.empresaId;
        req.titulo       = getText(etTitulo);
        req.descricao    = getText(etDesc);
        req.requisitos   = getText(etReq);
        req.areaAtuacao  = getText(etArea);
        req.beneficios   = getText(etBenef).isEmpty() ? vagaAtual.beneficios : getText(etBenef);
        req.cargaHoraria = getText(etCarga);
        req.tipoVagaId   = getSelectedId(repo.getTiposVaga(),       actvTipo.getText().toString(),        vagaAtual.tipoVagaId);
        req.modalidadeId = getSelectedId(repo.getModalidades(),     actvModalidade.getText().toString(),  vagaAtual.modalidadeId);
        req.escolaridadeId = getSelectedId(repo.getEscolaridades(), actvEscolaridade.getText().toString(), vagaAtual.escolaridadeId);
        req.cidadeId     = getSelectedId(repo.getCidades(),         actvCidade.getText().toString(),      vagaAtual.cidadeId);
        req.statusVagaId = vagaAtual.statusVagaId;
        req.salarioMinimo = parseDouble(getText(etSalMin), vagaAtual.salarioMinimo != null ? vagaAtual.salarioMinimo : 0.0);
        req.salarioMaximo = parseDouble(getText(etSalMax), vagaAtual.salarioMaximo != null ? vagaAtual.salarioMaximo : 0.0);
        req.numeroVagas  = parseInt(getText(etNVagas), vagaAtual.numeroVagas != null ? vagaAtual.numeroVagas : 1);
        req.idadeMinima  = parseIntOrNull(getText(etIdMin));
        req.idadeMaxima  = parseIntOrNull(getText(etIdMax));
        String dataInput = getText(etData);
        req.dataExpiracao = dataInput.isEmpty() ? vagaAtual.dataExpiracao : parseIsoDate(dataInput);

        btn.setEnabled(false);
        showLoading("Salvando alterações…");
        viewModel.atualizarVaga(vagaId, req);
    }

    private boolean validarFormulario() {
        tilTitulo.setError(null); tilArea.setError(null); tilDesc.setError(null);
        tilTipo.setError(null); tilModalidade.setError(null); tilRequisitos.setError(null);
        tilEscolaridade.setError(null); tilCidade.setError(null);

        boolean valido = true;
        View primeiroErro = null;
        LookupRepositoryImpl repo = LookupRepositoryImpl.get(requireContext());

        if (getText(etTitulo).isEmpty()) { tilTitulo.setError(getString(R.string.error_campo_obrigatorio)); primeiroErro = tilTitulo; valido = false; }
        if (getText(etArea).isEmpty()) { tilArea.setError(getString(R.string.error_campo_obrigatorio)); if (primeiroErro == null) primeiroErro = tilArea; valido = false; }
        if (getText(etDesc).isEmpty()) { tilDesc.setError(getString(R.string.error_campo_obrigatorio)); if (primeiroErro == null) primeiroErro = tilDesc; valido = false; }
        if (getSelectedId(repo.getTiposVaga(), actvTipo.getText().toString(), null) == null) { tilTipo.setError(getString(R.string.error_selecione_opcao)); if (primeiroErro == null) primeiroErro = tilTipo; valido = false; }
        if (getSelectedId(repo.getModalidades(), actvModalidade.getText().toString(), null) == null) { tilModalidade.setError(getString(R.string.error_selecione_opcao)); if (primeiroErro == null) primeiroErro = tilModalidade; valido = false; }
        if (getText(etReq).isEmpty()) { tilRequisitos.setError(getString(R.string.error_campo_obrigatorio)); if (primeiroErro == null) primeiroErro = tilRequisitos; valido = false; }
        if (getSelectedId(repo.getEscolaridades(), actvEscolaridade.getText().toString(), null) == null) { tilEscolaridade.setError(getString(R.string.error_selecione_opcao)); if (primeiroErro == null) primeiroErro = tilEscolaridade; valido = false; }
        if (getSelectedId(repo.getCidades(), actvCidade.getText().toString(), null) == null) { tilCidade.setError(getString(R.string.error_selecione_opcao)); if (primeiroErro == null) primeiroErro = tilCidade; valido = false; }

        if (primeiroErro != null) primeiroErro.requestFocus();
        return valido;
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
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMarginStart((int)(16*dp));
        tv.setLayoutParams(lp);
        layout.addView(tv);
        loadingDialog = new AlertDialog.Builder(requireContext()).setView(layout).setCancelable(false).create();
        loadingDialog.show();
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
        loadingDialog = null;
    }

    private void showErroDialog(String mensagem) {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext()).setTitle("Erro").setMessage(mensagem).setPositiveButton("OK", null).show();
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
                (v, year, month, day) -> et.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month+1, year)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setEditText(View root, int id, String value) {
        TextInputEditText et = root.findViewById(id);
        if (et != null && value != null) et.setText(value);
    }

    private String getText(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private Long getSelectedId(List<LookupItem> list, String label, Long fallback) {
        for (LookupItem item : list) { if (item.getLabel().equals(label)) return item.id; }
        return fallback;
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    private double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.replace(",", ".")); } catch (NumberFormatException e) { return def; }
    }

    private String formatDecimal(double value) {
        if (value == Math.floor(value)) return String.valueOf((long) value);
        return String.valueOf(value).replace(".", ",");
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
