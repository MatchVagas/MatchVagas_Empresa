package com.edu.matchvagasempresas.feature.perfil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.network.DataCache;

public class PerfilEmpresaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_perfil_empresa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btn_editar_perfil).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_perfil_to_editarPerfil));

        carregarPerfil(view);
    }

    private void carregarPerfil(View view) {
        DataCache.get().loadEmpresa(requireContext(),
                cached -> { if (cached != null && isAdded()) preencherDados(view, cached); },
                fresh  -> { if (isAdded()) preencherDados(view, fresh); }
        );
    }

    private void preencherDados(View view, EmpresaResponse e) {
        setText(view, R.id.tv_nome_fantasia, e.nomeFantasia);
        setText(view, R.id.tv_ramo_atuacao,  e.ramoAtuacao);
        setText(view, R.id.tv_cnpj,          e.cnpj);
        setText(view, R.id.tv_razao_social,  e.razaoSocial);
        setText(view, R.id.tv_porte,         e.porte);
        setText(view, R.id.tv_descricao,     e.descricao);
        setText(view, R.id.tv_site,          e.site);
        String telefone = (e.telefone != null && e.telefone.numero != null)
                ? formatarTelefone(e.telefone.numero) : "";
        setText(view, R.id.tv_telefone, telefone);
    }

    private String formatarTelefone(String numero) {
        String digits = numero.replaceAll("[^0-9]", "");
        if (digits.length() == 11)
            return String.format("(%s) %s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 7),
                    digits.substring(7));
        if (digits.length() == 10)
            return String.format("(%s) %s-%s",
                    digits.substring(0, 2),
                    digits.substring(2, 6),
                    digits.substring(6));
        return numero; // retorna como veio se formato desconhecido
    }

    private void setText(View root, int id, String value) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(value != null ? value : "");
    }
}
