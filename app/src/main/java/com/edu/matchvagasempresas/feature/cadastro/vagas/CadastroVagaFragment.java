package com.edu.matchvagasempresas.feature.cadastro.vagas;

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
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.LookupItem;
import com.edu.matchvagasempresas.model.VagaRequest;
import com.edu.matchvagasempresas.model.VagaResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.LookupCache;
import com.edu.matchvagasempresas.util.SessionManager;
import com.google.android.material.button.MaterialButton;
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

public class CadastroVagaFragment extends Fragment {

    private AutoCompleteTextView actvTipo, actvModalidade, actvEscolaridade, actvCidade;
    private TextInputLayout tilTitulo, tilArea, tilDesc, tilTipo, tilModalidade,
            tilRequisitos, tilEscolaridade, tilCidade;
    private TextInputEditText etTitulo, etArea, etDesc, etReq;
    private AlertDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_cadastro_vaga, container, false);
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
        carregarLookups();

        view.findViewById(R.id.btn_publicar).setOnClickListener(v -> publicarVaga(view, v));
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
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                til.setError(null);
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    private void carregarLookups() {
        LookupCache.get().preload(requireContext(), () -> {
            if (!isAdded()) return;
            bindDropdown(actvTipo, LookupCache.get().getTiposVaga());
            bindDropdown(actvModalidade, LookupCache.get().getModalidades());
            bindDropdown(actvEscolaridade, LookupCache.get().getEscolaridades());
            bindDropdown(actvCidade, LookupCache.get().getCidades());
        });
    }

    private void bindDropdown(AutoCompleteTextView actv, List<LookupItem> items) {
        List<String> labels = new ArrayList<>();
        for (LookupItem item : items) labels.add(item.getLabel());
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));
    }

    private void publicarVaga(View view, View btn) {
        SessionManager session = new SessionManager(requireContext());
        if (!session.isEmpresaAprovada()) {
            showErroDialog("Sua empresa ainda não foi aprovada. Aguarde a aprovação do administrador.");
            return;
        }

        TextInputEditText etSalMin = view.findViewById(R.id.et_salario_min);
        TextInputEditText etSalMax = view.findViewById(R.id.et_salario_max);
        TextInputEditText etBenef  = view.findViewById(R.id.et_beneficios);
        TextInputEditText etCarga  = view.findViewById(R.id.et_carga_horaria);
        TextInputEditText etNVagas = view.findViewById(R.id.et_num_vagas);
        TextInputEditText etIdMin  = view.findViewById(R.id.et_idade_min);
        TextInputEditText etIdMax  = view.findViewById(R.id.et_idade_max);
        TextInputEditText etData   = view.findViewById(R.id.et_data_expiracao);

        if (!validarFormulario()) return;

        String titulo = getText(etTitulo);
        String area   = getText(etArea);
        String desc   = getText(etDesc);
        String req    = getText(etReq);
        Long tipoId   = getSelectedId(LookupCache.get().getTiposVaga(),    actvTipo.getText().toString());
        Long modalId  = getSelectedId(LookupCache.get().getModalidades(),  actvModalidade.getText().toString());
        Long escolId  = getSelectedId(LookupCache.get().getEscolaridades(), actvEscolaridade.getText().toString());
        Long cidadeId = getSelectedId(LookupCache.get().getCidades(),      actvCidade.getText().toString());

        List<LookupItem> statusVaga = LookupCache.get().getStatusVaga();
        Long statusId = statusVaga.isEmpty() ? null : statusVaga.get(0).id;
        Long empresaId = session.getEmpresaId();

        VagaRequest request = new VagaRequest();
        request.empresaId    = empresaId;
        request.titulo       = titulo;
        request.descricao    = desc;
        request.requisitos   = req;
        request.tipoVagaId   = tipoId;
        request.modalidadeId = modalId;
        request.escolaridadeId = escolId;
        request.cidadeId     = cidadeId;
        request.statusVagaId = statusId;
        request.areaAtuacao  = area;
        request.beneficios   = getText(etBenef);
        request.cargaHoraria = getText(etCarga);
        request.numeroVagas  = parseInt(getText(etNVagas), 1);
        request.salarioMinimo = parseDouble(getText(etSalMin), 0.0);
        request.salarioMaximo = parseDouble(getText(etSalMax), 0.0);
        request.idadeMinima  = parseIntOrNull(getText(etIdMin));
        request.idadeMaxima  = parseIntOrNull(getText(etIdMax));
        request.dataExpiracao = parseIsoDate(getText(etData));

        ((MaterialButton) btn).setEnabled(false);
        showLoading("Publicando vaga…");
        ApiClient.getService(requireContext()).criarVaga(request).enqueue(new Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call, @NonNull Response<VagaResponse> r) {
                if (!isAdded()) return;
                hideLoading();
                ((MaterialButton) btn).setEnabled(true);
                if (r.isSuccessful()) {
                    com.edu.matchvagasempresas.network.DataCache.get().invalidateVagas(requireContext());
                    Toast.makeText(requireContext(), "Vaga publicada!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(btn).navigateUp();
                } else {
                    showErroDialog(buildErroHttp(r));
                }
            }

            @Override
            public void onFailure(@NonNull Call<VagaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                hideLoading();
                ((MaterialButton) btn).setEnabled(true);
                showErroDialog(buildErroConexao(t));
            }
        });
    }

    private boolean validarFormulario() {
        tilTitulo.setError(null);
        tilArea.setError(null);
        tilDesc.setError(null);
        tilTipo.setError(null);
        tilModalidade.setError(null);
        tilRequisitos.setError(null);
        tilEscolaridade.setError(null);
        tilCidade.setError(null);

        boolean valido = true;
        View primeiroErro = null;

        if (getText(etTitulo).isEmpty()) {
            tilTitulo.setError(getString(R.string.error_campo_obrigatorio));
            primeiroErro = tilTitulo;
            valido = false;
        }
        if (getText(etArea).isEmpty()) {
            tilArea.setError(getString(R.string.error_campo_obrigatorio));
            if (primeiroErro == null) primeiroErro = tilArea;
            valido = false;
        }
        if (getText(etDesc).isEmpty()) {
            tilDesc.setError(getString(R.string.error_campo_obrigatorio));
            if (primeiroErro == null) primeiroErro = tilDesc;
            valido = false;
        }
        if (getSelectedId(LookupCache.get().getTiposVaga(), actvTipo.getText().toString()) == null) {
            tilTipo.setError(getString(R.string.error_selecione_opcao));
            if (primeiroErro == null) primeiroErro = tilTipo;
            valido = false;
        }
        if (getSelectedId(LookupCache.get().getModalidades(), actvModalidade.getText().toString()) == null) {
            tilModalidade.setError(getString(R.string.error_selecione_opcao));
            if (primeiroErro == null) primeiroErro = tilModalidade;
            valido = false;
        }
        if (getText(etReq).isEmpty()) {
            tilRequisitos.setError(getString(R.string.error_campo_obrigatorio));
            if (primeiroErro == null) primeiroErro = tilRequisitos;
            valido = false;
        }
        if (getSelectedId(LookupCache.get().getEscolaridades(), actvEscolaridade.getText().toString()) == null) {
            tilEscolaridade.setError(getString(R.string.error_selecione_opcao));
            if (primeiroErro == null) primeiroErro = tilEscolaridade;
            valido = false;
        }
        if (getSelectedId(LookupCache.get().getCidades(), actvCidade.getText().toString()) == null) {
            tilCidade.setError(getString(R.string.error_selecione_opcao));
            if (primeiroErro == null) primeiroErro = tilCidade;
            valido = false;
        }

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
            case 400: return "Dados inválidos. Verifique os campos preenchidos e tente novamente.";
            case 401: return "Sessão expirada. Faça login novamente.";
            case 403: return "Você não tem permissão para publicar vagas.";
            case 409: return "Já existe uma vaga com essas informações.";
            case 422: return "Não foi possível processar os dados. Revise as informações e tente novamente.";
            case 500: return "Erro interno no servidor. Tente novamente mais tarde.";
            case 503: return "Serviço temporariamente indisponível. Tente novamente em instantes.";
            default:  return "Erro ao publicar a vaga (código " + r.code() + "). Tente novamente.";
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

    private void setupDatePicker(View view) {
        TextInputEditText etData = view.findViewById(R.id.et_data_expiracao);
        TextInputLayout tilData = view.findViewById(R.id.til_data_expiracao);
        etData.setOnClickListener(v -> showDatePicker(etData));
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

    private String getText(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private Long getSelectedId(List<LookupItem> list, String label) {
        for (LookupItem item : list) {
            if (item.getLabel().equals(label)) return item.id;
        }
        return null;
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

    private String parseIsoDate(String dataBr) {
        if (dataBr == null || dataBr.length() < 10) return null;
        String[] parts = dataBr.split("/");
        if (parts.length == 3) return parts[2] + "-" + parts[1] + "-" + parts[0] + "T23:59:59";
        return null;
    }
}
