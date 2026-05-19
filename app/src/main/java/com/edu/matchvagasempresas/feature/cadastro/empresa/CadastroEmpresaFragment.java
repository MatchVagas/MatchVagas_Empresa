package com.edu.matchvagasempresas.feature.cadastro.empresa;

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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.AuthResponse;
import com.edu.matchvagasempresas.model.LookupItem;
import com.edu.matchvagasempresas.model.RegisterEmpresaRequest;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.LookupCache;
import com.edu.matchvagasempresas.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CadastroEmpresaFragment extends Fragment {

    private AutoCompleteTextView actvPorte, actvRamo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_cadastro_empresa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        actvPorte = view.findViewById(R.id.actv_porte);
        actvRamo = view.findViewById(R.id.actv_ramo);

        setupDatePicker(view);
        setupCnpjMask(view.findViewById(R.id.et_cnpj));
        carregarLookups();

        view.findViewById(R.id.btn_cadastrar).setOnClickListener(v -> cadastrar(view, v));
        view.findViewById(R.id.tv_fazer_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void carregarLookups() {
        LookupCache.get().preload(requireContext(), () -> {
            if (!isAdded()) return;
            bindDropdown(actvPorte, LookupCache.get().getPortes());
            bindDropdown(actvRamo, LookupCache.get().getRamos());
        });
    }

    private void bindDropdown(AutoCompleteTextView actv, List<LookupItem> items) {
        List<String> labels = new ArrayList<>();
        for (LookupItem item : items) labels.add(item.getLabel());
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));
    }

    private void cadastrar(View view, View btn) {
        String nome = getText(view, R.id.et_nome);
        String email = getText(view, R.id.et_email);
        String senha = getText(view, R.id.et_senha);
        String confirmar = getText(view, R.id.et_confirmar_senha);
        String dataNasc = getText(view, R.id.et_data_nascimento);
        String cnpj = getText(view, R.id.et_cnpj);
        String razaoSocial = getText(view, R.id.et_razao_social);
        String nomeFantasia = getText(view, R.id.et_nome_fantasia);
        String descricao = getText(view, R.id.et_descricao);
        String site = getText(view, R.id.et_site);

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha nome, e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!senha.equals(confirmar)) {
            Toast.makeText(requireContext(), "Senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }
        String cnpjDigits = cnpj.replaceAll("[^0-9]", "");
        if (razaoSocial.isEmpty() || nomeFantasia.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha os dados da empresa", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cnpjDigits.length() != 14) {
            Toast.makeText(requireContext(), "CNPJ deve ter 14 dígitos (XX.XXX.XXX/XXXX-XX)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Long porteId = getSelectedId(LookupCache.get().getPortes(), actvPorte.getText().toString());
        Long ramoId = getSelectedId(LookupCache.get().getRamos(), actvRamo.getText().toString());

        if (porteId == null || ramoId == null) {
            Toast.makeText(requireContext(), "Selecione porte e ramo de atuação", Toast.LENGTH_SHORT).show();
            return;
        }

        String isoData = parseIsoDate(dataNasc);
        if (isoData == null) isoData = "1990-01-01T00:00:00";

        ((MaterialButton) btn).setEnabled(false);

        // Garante que nenhum token de sessão anterior seja enviado no header de registro
        new SessionManager(requireContext()).clear();
        ApiClient.reset();

        RegisterEmpresaRequest req = new RegisterEmpresaRequest(
                nome, email, senha, isoData,
                cnpj, razaoSocial, nomeFantasia,
                descricao.isEmpty() ? null : descricao,
                porteId, ramoId,
                site.isEmpty() ? null : site);

        ApiClient.getService(requireContext()).registerEmpresa(req).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> r) {
                if (!isAdded()) return;
                ((MaterialButton) btn).setEnabled(true);
                if (r.isSuccessful() && r.body() != null) {
                    AuthResponse auth = r.body();
                    SessionManager session = new SessionManager(requireContext());
                    session.saveAuth(auth.token, auth.usuarioId);
                    // empresa status começa como PENDENTE — salva na sessão
                    session.saveEmpresa(null, nomeFantasia, "PENDENTE");
                    ApiClient.reset();
                    Navigation.findNavController(btn).navigate(R.id.action_cadastroEmpresa_to_dashboard);
                } else {
                    String msg = r.code() == 400
                            ? "Dados inválidos: verifique CNPJ, e-mail ou campos obrigatórios."
                            : "Erro no cadastro. Tente novamente.";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                ((MaterialButton) btn).setEnabled(true);
                Toast.makeText(requireContext(),
                        "Erro de conexão ao criar empresa: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupDatePicker(View view) {
        TextInputEditText etData = view.findViewById(R.id.et_data_nascimento);
        if (etData == null) return;
        etData.setOnClickListener(v -> showDatePicker(etData));
    }

    private void showDatePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);
        new DatePickerDialog(requireContext(),
                (v, year, month, day) -> et.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private String getText(View root, int id) {
        TextInputEditText et = root.findViewById(id);
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private Long getSelectedId(List<LookupItem> list, String label) {
        for (LookupItem item : list) {
            if (item.getLabel().equals(label)) return item.id;
        }
        return null;
    }

    private void setupCnpjMask(TextInputEditText et) {
        et.addTextChangedListener(new TextWatcher() {
            private boolean updating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (updating) return;
                updating = true;

                String digits = s.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 14) digits = digits.substring(0, 14);

                // XX.XXX.XXX/XXXX-XX
                StringBuilder masked = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i == 2 || i == 5) masked.append('.');
                    else if (i == 8)      masked.append('/');
                    else if (i == 12)     masked.append('-');
                    masked.append(digits.charAt(i));
                }

                s.replace(0, s.length(), masked.toString());
                updating = false;
            }
        });
    }

    private String parseIsoDate(String dataBr) {
        if (dataBr == null || dataBr.length() < 10) return null;
        String[] parts = dataBr.split("/");
        if (parts.length == 3) return parts[2] + "-" + parts[1] + "-" + parts[0] + "T00:00:00";
        return null;
    }
}
