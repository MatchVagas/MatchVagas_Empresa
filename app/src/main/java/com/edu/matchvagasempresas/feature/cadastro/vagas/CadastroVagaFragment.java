package com.edu.matchvagasempresas.feature.cadastro.vagas;

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
import com.edu.matchvagasempresas.network.ApiService;
import com.edu.matchvagasempresas.util.SessionManager;
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

public class CadastroVagaFragment extends Fragment {

    private final List<LookupItem> tiposVaga = new ArrayList<>();
    private final List<LookupItem> modalidades = new ArrayList<>();
    private final List<LookupItem> escolaridades = new ArrayList<>();
    private final List<LookupItem> cidades = new ArrayList<>();
    private final List<LookupItem> statusVaga = new ArrayList<>();

    private AutoCompleteTextView actvTipo, actvModalidade, actvEscolaridade, actvCidade;

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

        setupDatePicker(view);
        carregarLookups();

        view.findViewById(R.id.btn_publicar).setOnClickListener(v -> publicarVaga(view, v));
    }

    private void carregarLookups() {
        ApiService api = ApiClient.getService(requireContext());

        api.listarTiposVaga().enqueue(callback(tiposVaga, actvTipo));
        api.listarModalidades().enqueue(callback(modalidades, actvModalidade));
        api.listarEscolaridades().enqueue(callback(escolaridades, actvEscolaridade));
        api.listarCidades().enqueue(callback(cidades, actvCidade));
        api.listarStatusVaga().enqueue(new Callback<List<LookupItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<LookupItem>> call,
                                   @NonNull Response<List<LookupItem>> r) {
                if (r.isSuccessful() && r.body() != null) statusVaga.addAll(r.body());
            }
            @Override
            public void onFailure(@NonNull Call<List<LookupItem>> call, @NonNull Throwable t) { }
        });
    }

    private Callback<List<LookupItem>> callback(List<LookupItem> list, AutoCompleteTextView actv) {
        return new Callback<List<LookupItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<LookupItem>> call,
                                   @NonNull Response<List<LookupItem>> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(requireContext(),
                            "Erro ao carregar opções (código " + r.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                list.clear();
                list.addAll(r.body());
                List<String> labels = new ArrayList<>();
                for (LookupItem item : list) labels.add(item.getLabel());
                actv.setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, labels));
            }
            @Override
            public void onFailure(@NonNull Call<List<LookupItem>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Falha de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };
    }

    private void publicarVaga(View view, View btn) {
        TextInputEditText etTitulo = view.findViewById(R.id.et_titulo);
        TextInputEditText etArea = view.findViewById(R.id.et_area_atuacao);
        TextInputEditText etDesc = view.findViewById(R.id.et_descricao);
        TextInputEditText etReq = view.findViewById(R.id.et_requisitos);
        TextInputEditText etSalMin = view.findViewById(R.id.et_salario_min);
        TextInputEditText etSalMax = view.findViewById(R.id.et_salario_max);
        TextInputEditText etBenef = view.findViewById(R.id.et_beneficios);
        TextInputEditText etCarga = view.findViewById(R.id.et_carga_horaria);
        TextInputEditText etNVagas = view.findViewById(R.id.et_num_vagas);
        TextInputEditText etData = view.findViewById(R.id.et_data_expiracao);

        String titulo = getText(etTitulo);
        String area = getText(etArea);
        String desc = getText(etDesc);
        String req = getText(etReq);

        if (titulo.isEmpty() || area.isEmpty() || desc.isEmpty() || req.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        Long tipoId = getSelectedId(tiposVaga, actvTipo.getText().toString());
        Long modalId = getSelectedId(modalidades, actvModalidade.getText().toString());
        Long escolId = getSelectedId(escolaridades, actvEscolaridade.getText().toString());
        Long cidadeId = getSelectedId(cidades, actvCidade.getText().toString());

        if (tipoId == null || modalId == null || escolId == null || cidadeId == null) {
            Toast.makeText(requireContext(), "Selecione tipo, modalidade, escolaridade e cidade",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Long statusId = statusVaga.isEmpty() ? null : statusVaga.get(0).id;
        SessionManager session = new SessionManager(requireContext());
        Long empresaId = session.getEmpresaId();

        VagaRequest request = new VagaRequest();
        request.empresaId = empresaId;
        request.titulo = titulo;
        request.descricao = desc;
        request.requisitos = req;
        request.tipoVagaId = tipoId;
        request.modalidadeId = modalId;
        request.escolaridadeId = escolId;
        request.cidadeId = cidadeId;
        request.statusVagaId = statusId;
        request.areaAtuacao = area;
        request.beneficios = getText(etBenef);
        request.cargaHoraria = getText(etCarga);
        request.numeroVagas = parseInt(getText(etNVagas), 1);
        request.salarioMinimo = parseDouble(getText(etSalMin), 0.0);
        request.salarioMaximo = parseDouble(getText(etSalMax), 0.0);
        request.dataExpiracao = parseIsoDate(getText(etData));

        ((MaterialButton) btn).setEnabled(false);
        ApiClient.getService(requireContext()).criarVaga(request).enqueue(new Callback<VagaResponse>() {
            @Override
            public void onResponse(@NonNull Call<VagaResponse> call, @NonNull Response<VagaResponse> r) {
                if (!isAdded()) return;
                ((MaterialButton) btn).setEnabled(true);
                if (r.isSuccessful()) {
                    Toast.makeText(requireContext(), "Vaga publicada!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(btn).navigateUp();
                } else {
                    Toast.makeText(requireContext(), "Erro ao publicar vaga", Toast.LENGTH_SHORT).show();
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
