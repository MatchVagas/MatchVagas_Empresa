package com.edu.matchvagasempresas.feature.editar.empresa;

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
import com.edu.matchvagasempresas.model.EmpresaRequest;
import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.model.LookupItem;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.LookupCache;
import com.edu.matchvagasempresas.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarPerfilEmpresaFragment extends Fragment {

    private AutoCompleteTextView actvPorte, actvRamo;
    private EmpresaResponse empresaAtual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_editar_perfil_empresa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        actvPorte = view.findViewById(R.id.actv_porte);
        actvRamo = view.findViewById(R.id.actv_ramo);

        view.findViewById(R.id.btn_alterar_foto).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Funcionalidade em breve", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_salvar).setOnClickListener(v -> salvarPerfil(view, v));

        setupTelefoneMask(view.findViewById(R.id.et_telefone));
        carregarLookups(view);
        carregarPerfil(view);
    }

    private void carregarLookups(View view) {
        LookupCache.get().preload(requireContext(), () -> {
            if (!isAdded()) return;
            bindDropdown(actvPorte, LookupCache.get().getPortes());
            bindDropdown(actvRamo, LookupCache.get().getRamos());
            if (empresaAtual != null && empresaAtual.porte != null)
                actvPorte.setText(empresaAtual.porte, false);
            if (empresaAtual != null && empresaAtual.ramoAtuacao != null)
                actvRamo.setText(empresaAtual.ramoAtuacao, false);
        });
    }

    private void bindDropdown(AutoCompleteTextView actv, List<LookupItem> items) {
        List<String> labels = new ArrayList<>();
        for (LookupItem item : items) labels.add(item.getLabel());
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, labels));
    }

    private void carregarPerfil(View view) {
        ApiClient.getService(requireContext()).minhaEmpresa().enqueue(new Callback<EmpresaResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmpresaResponse> call,
                                   @NonNull Response<EmpresaResponse> r) {
                if (!isAdded()) return;
                if (r.isSuccessful() && r.body() != null) {
                    empresaAtual = r.body();
                    preencherFormulario(view, empresaAtual);
                }
            }

            @Override
            public void onFailure(@NonNull Call<EmpresaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro ao carregar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preencherFormulario(View view, EmpresaResponse e) {
        setEditText(view, R.id.et_cnpj,          e.cnpj);
        setEditText(view, R.id.et_razao_social,  e.razaoSocial);
        setEditText(view, R.id.et_nome_fantasia, e.nomeFantasia);
        setEditText(view, R.id.et_descricao,     e.descricao);
        setEditText(view, R.id.et_site,          e.site);
        if (e.telefone != null && e.telefone.numero != null)
            setEditText(view, R.id.et_telefone, aplicarMascaraTelefone(e.telefone.numero));
        if (e.porte != null)       actvPorte.setText(e.porte, false);
        if (e.ramoAtuacao != null) actvRamo.setText(e.ramoAtuacao, false);
    }

    private void salvarPerfil(View view, View btn) {
        if (empresaAtual == null) {
            Toast.makeText(requireContext(), "Dados não carregados", Toast.LENGTH_SHORT).show();
            return;
        }

        String cnpj        = getText(view, R.id.et_cnpj);
        String razaoSocial  = getText(view, R.id.et_razao_social);
        String nomeFantasia = getText(view, R.id.et_nome_fantasia);
        String descricao    = getText(view, R.id.et_descricao);
        String site         = getText(view, R.id.et_site);
        String telefoneNum  = getText(view, R.id.et_telefone);

        Long porteId = getSelectedId(LookupCache.get().getPortes(), actvPorte.getText().toString());
        Long ramoId  = getSelectedId(LookupCache.get().getRamos(),  actvRamo.getText().toString());

        if (porteId == null && empresaAtual.porte != null)
            porteId = getSelectedId(LookupCache.get().getPortes(), empresaAtual.porte);
        if (ramoId == null && empresaAtual.ramoAtuacao != null)
            ramoId = getSelectedId(LookupCache.get().getRamos(), empresaAtual.ramoAtuacao);

        if (porteId == null || ramoId == null) {
            Toast.makeText(requireContext(), "Selecione porte e ramo de atuação", Toast.LENGTH_SHORT).show();
            return;
        }

        Long empresaId = new SessionManager(requireContext()).getEmpresaId();
        if (empresaId == null) empresaId = empresaAtual.id;

        EmpresaRequest req = new EmpresaRequest(
                cnpj.isEmpty()        ? empresaAtual.cnpj        : cnpj,
                razaoSocial.isEmpty() ? empresaAtual.razaoSocial : razaoSocial,
                nomeFantasia.isEmpty()? empresaAtual.nomeFantasia : nomeFantasia,
                descricao.isEmpty()   ? empresaAtual.descricao   : descricao,
                porteId, ramoId,
                site.isEmpty()        ? empresaAtual.site         : site
        );

        // Telefone: usa o número editado; mantém tipoTelefoneId existente ou padrão Celular (1)
        if (!telefoneNum.isEmpty()) {
            Long tipoId = (empresaAtual.telefone != null && empresaAtual.telefone.tipoTelefoneId != null)
                    ? empresaAtual.telefone.tipoTelefoneId : 1L;
            boolean wpp = empresaAtual.telefone != null && empresaAtual.telefone.wpp;
            req.telefone = new EmpresaRequest.Telefone(telefoneNum, tipoId, wpp);
        }

        final Long finalEmpresaId = empresaId;
        ((MaterialButton) btn).setEnabled(false);
        ApiClient.getService(requireContext())
                .atualizarEmpresa(finalEmpresaId, req)
                .enqueue(new Callback<EmpresaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EmpresaResponse> call,
                                           @NonNull Response<EmpresaResponse> r) {
                        if (!isAdded()) return;
                        ((MaterialButton) btn).setEnabled(true);
                        if (r.isSuccessful() && r.body() != null) {
                            new SessionManager(requireContext())
                                    .saveEmpresa(r.body().id, r.body().nomeFantasia, r.body().status);
                            Toast.makeText(requireContext(), "Perfil atualizado!", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(btn).navigateUp();
                        } else {
                            Toast.makeText(requireContext(), "Erro ao salvar", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EmpresaResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        ((MaterialButton) btn).setEnabled(true);
                        Toast.makeText(requireContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setEditText(View root, int id, String value) {
        TextInputEditText et = root.findViewById(id);
        if (et != null && value != null) et.setText(value);
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

    private void setupTelefoneMask(TextInputEditText et) {
        if (et == null) return;
        et.addTextChangedListener(new TextWatcher() {
            private boolean updating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (updating) return;
                updating = true;
                String masked = aplicarMascaraTelefone(s.toString());
                s.replace(0, s.length(), masked);
                updating = false;
            }
        });
    }

    // Formata para (XX) XXXX-XXXX (fixo) ou (XX) XXXXX-XXXX (celular)
    private String aplicarMascaraTelefone(String input) {
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.length() > 11) digits = digits.substring(0, 11);

        String mask = digits.length() <= 10 ? "(##) ####-####" : "(##) #####-####";
        StringBuilder out = new StringBuilder();
        int d = 0;
        for (int m = 0; m < mask.length() && d < digits.length(); m++) {
            if (mask.charAt(m) == '#') out.append(digits.charAt(d++));
            else                       out.append(mask.charAt(m));
        }
        return out.toString();
    }
}
