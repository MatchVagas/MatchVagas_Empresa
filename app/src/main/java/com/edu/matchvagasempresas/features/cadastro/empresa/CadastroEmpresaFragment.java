package com.edu.matchvagasempresas.features.cadastro.empresa;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.data.local.SessionManager;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.edu.matchvagasempresas.data.remote.dto.LookupItem;
import com.edu.matchvagasempresas.data.remote.dto.RegisterEmpresaRequest;
import com.edu.matchvagasempresas.data.repository.LookupRepositoryImpl;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CadastroEmpresaFragment extends Fragment {

    private CadastroEmpresaViewModel viewModel;
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

        viewModel = new ViewModelProvider(this).get(CadastroEmpresaViewModel.class);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        actvPorte = view.findViewById(R.id.actv_porte);
        actvRamo = view.findViewById(R.id.actv_ramo);

        setupDatePicker(view);
        setupCnpjMask(view.findViewById(R.id.et_cnpj));
        carregarLookups();

        viewModel.getCadastroResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    view.findViewById(R.id.btn_cadastrar).setEnabled(false);
                    break;
                case SUCCESS:
                    if (resource.getData() != null) {
                        SessionManager session = new SessionManager(requireContext());
                        session.saveAuth(resource.getData().token, resource.getData().usuarioId);
                        session.saveEmpresa(null,
                                view.findViewById(R.id.et_nome_fantasia) instanceof TextInputEditText
                                        ? getText(view, R.id.et_nome_fantasia) : "", "PENDENTE");
                        RetrofitClient.reset();
                    }
                    Navigation.findNavController(view).navigate(R.id.action_cadastroEmpresa_to_dashboard);
                    break;
                case ERROR:
                    view.findViewById(R.id.btn_cadastrar).setEnabled(true);
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });

        view.findViewById(R.id.btn_cadastrar).setOnClickListener(v -> cadastrar(view));
        view.findViewById(R.id.tv_fazer_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void carregarLookups() {
        LookupRepositoryImpl.get(requireContext()).preload(() -> {
            if (!isAdded()) return;
            LookupRepositoryImpl repo = LookupRepositoryImpl.get(requireContext());
            bindDropdown(actvPorte, repo.getPortes());
            bindDropdown(actvRamo, repo.getRamos());
        });
    }

    private void bindDropdown(AutoCompleteTextView actv, List<LookupItem> items) {
        List<String> labels = new ArrayList<>();
        for (LookupItem item : items) labels.add(item.getLabel());
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));
    }

    private void cadastrar(View view) {
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
            Toast.makeText(requireContext(), "CNPJ deve ter 14 dígitos (XX.XXX.XXX/XXXX-XX)", Toast.LENGTH_LONG).show();
            return;
        }

        LookupRepositoryImpl repo = LookupRepositoryImpl.get(requireContext());
        Long porteId = getSelectedId(repo.getPortes(), actvPorte.getText().toString());
        Long ramoId = getSelectedId(repo.getRamos(), actvRamo.getText().toString());

        if (porteId == null || ramoId == null) {
            Toast.makeText(requireContext(), "Selecione porte e ramo de atuação", Toast.LENGTH_SHORT).show();
            return;
        }

        String isoData = parseIsoDate(dataNasc);
        if (isoData == null) isoData = "1990-01-01T00:00:00";

        new SessionManager(requireContext()).clear();
        RetrofitClient.reset();

        viewModel.cadastrar(new RegisterEmpresaRequest(
                nome, email, senha, isoData,
                cnpj, razaoSocial, nomeFantasia,
                descricao.isEmpty() ? null : descricao,
                porteId, ramoId,
                site.isEmpty() ? null : site));
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
