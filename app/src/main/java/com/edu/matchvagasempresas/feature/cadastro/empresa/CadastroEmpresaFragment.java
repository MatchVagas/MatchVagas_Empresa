package com.edu.matchvagasempresas.feature.cadastro.empresa;

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
import com.edu.matchvagasempresas.model.AuthResponse;
import com.edu.matchvagasempresas.model.EmpresaRequest;
import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.model.LookupItem;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.ApiService;
import com.edu.matchvagasempresas.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroEmpresaFragment extends Fragment {

    private final List<LookupItem> portes = new ArrayList<>();
    private final List<LookupItem> ramos = new ArrayList<>();
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
        carregarLookups();

        view.findViewById(R.id.btn_cadastrar).setOnClickListener(v -> cadastrar(view, v));
        view.findViewById(R.id.tv_fazer_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void carregarLookups() {
        ApiService api = ApiClient.getService(requireContext());

        api.listarPortes().enqueue(new Callback<List<LookupItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<LookupItem>> call,
                                   @NonNull Response<List<LookupItem>> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(requireContext(),
                            "Erro ao carregar portes (código " + r.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                portes.clear();
                portes.addAll(r.body());
                List<String> labels = new ArrayList<>();
                for (LookupItem item : portes) labels.add(item.getLabel());
                actvPorte.setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, labels));
            }
            @Override
            public void onFailure(@NonNull Call<List<LookupItem>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        api.listarRamos().enqueue(new Callback<List<LookupItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<LookupItem>> call,
                                   @NonNull Response<List<LookupItem>> r) {
                if (!isAdded()) return;
                if (!r.isSuccessful() || r.body() == null) {
                    Toast.makeText(requireContext(),
                            "Erro ao carregar ramos (código " + r.code() + ")",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                ramos.clear();
                ramos.addAll(r.body());
                List<String> labels = new ArrayList<>();
                for (LookupItem item : ramos) labels.add(item.getLabel());
                actvRamo.setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, labels));
            }
            @Override
            public void onFailure(@NonNull Call<List<LookupItem>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(),
                        "Falha de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
        if (cnpj.isEmpty() || razaoSocial.isEmpty() || nomeFantasia.isEmpty()) {
            Toast.makeText(requireContext(), "Preencha os dados da empresa", Toast.LENGTH_SHORT).show();
            return;
        }

        Long porteId = getSelectedId(portes, actvPorte.getText().toString());
        Long ramoId = getSelectedId(ramos, actvRamo.getText().toString());

        if (porteId == null || ramoId == null) {
            Toast.makeText(requireContext(), "Selecione porte e ramo de atuação", Toast.LENGTH_SHORT).show();
            return;
        }

        String isoData = parseIsoDate(dataNasc);
        if (isoData == null) isoData = "1990-01-01T00:00:00";

        ((MaterialButton) btn).setEnabled(false);

        final String finalEmail = email;
        final String finalSenha = senha;
        final String finalNomeFantasia = nomeFantasia;
        final Long finalPorteId = porteId;
        final Long finalRamoId = ramoId;
        final String finalDescricao = descricao;
        final String finalSite = site.isEmpty() ? null : site;
        final String finalCnpj = cnpj;
        final String finalRazaoSocial = razaoSocial;

        ApiService api = ApiClient.getService(requireContext());

        JsonObject registerBody = new JsonObject();
        registerBody.addProperty("nome", nome);
        registerBody.addProperty("email", email);
        registerBody.addProperty("senha", senha);
        registerBody.addProperty("dataNascimento", isoData);
        registerBody.addProperty("tipoUsuario", "EMPRESA");
        registerBody.addProperty("ativo", true);

        api.register(registerBody).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call,
                                   @NonNull Response<JsonObject> r) {
                if (!isAdded()) return;
                if (r.isSuccessful()) {
                    fazerLogin(btn, finalEmail, finalSenha,
                            finalCnpj, finalRazaoSocial, finalNomeFantasia,
                            finalDescricao, finalPorteId, finalRamoId, finalSite);
                } else {
                    ((MaterialButton) btn).setEnabled(true);
                    Toast.makeText(requireContext(), "Erro no cadastro: e-mail já em uso?",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                ((MaterialButton) btn).setEnabled(true);
                Toast.makeText(requireContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fazerLogin(View btn, String email, String senha,
                            String cnpj, String razaoSocial, String nomeFantasia,
                            String descricao, Long porteId, Long ramoId, String site) {
        JsonObject loginBody = new JsonObject();
        loginBody.addProperty("email", email);
        loginBody.addProperty("senha", senha);

        ApiClient.getService(requireContext()).login(loginBody).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    new SessionManager(requireContext()).saveAuth(r.body().token, r.body().usuarioId);
                    criarEmpresa(btn, cnpj, razaoSocial, nomeFantasia, descricao, porteId, ramoId, site);
                } else {
                    ((MaterialButton) btn).setEnabled(true);
                    Toast.makeText(requireContext(), "Cadastro criado, faça login", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(btn).navigateUp();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                ((MaterialButton) btn).setEnabled(true);
                Navigation.findNavController(btn).navigateUp();
            }
        });
    }

    private void criarEmpresa(View btn, String cnpj, String razaoSocial, String nomeFantasia,
                              String descricao, Long porteId, Long ramoId, String site) {
        EmpresaRequest req = new EmpresaRequest(cnpj, razaoSocial, nomeFantasia, descricao, porteId, ramoId, site);
        ApiClient.getService(requireContext()).criarEmpresa(req).enqueue(new Callback<EmpresaResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmpresaResponse> call,
                                   @NonNull Response<EmpresaResponse> r) {
                if (!isAdded()) return;
                ((MaterialButton) btn).setEnabled(true);
                if (r.isSuccessful() && r.body() != null) {
                    new SessionManager(requireContext()).saveEmpresa(r.body().id, r.body().nomeFantasia);
                }
                Navigation.findNavController(btn).navigate(R.id.action_cadastroEmpresa_to_dashboard);
            }

            @Override
            public void onFailure(@NonNull Call<EmpresaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                ((MaterialButton) btn).setEnabled(true);
                Navigation.findNavController(btn).navigate(R.id.action_cadastroEmpresa_to_dashboard);
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

    private String parseIsoDate(String dataBr) {
        if (dataBr == null || dataBr.length() < 10) return null;
        String[] parts = dataBr.split("/");
        if (parts.length == 3) return parts[2] + "-" + parts[1] + "-" + parts[0] + "T00:00:00";
        return null;
    }
}
