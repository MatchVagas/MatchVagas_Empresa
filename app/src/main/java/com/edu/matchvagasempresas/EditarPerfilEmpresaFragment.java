package com.edu.matchvagasempresas;

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

import com.google.android.material.textfield.TextInputEditText;

public class EditarPerfilEmpresaFragment extends Fragment {

    private static final String[] PORTES = {
            "Microempresa (ME)", "Empresa de Pequeno Porte (EPP)",
            "Médio Porte", "Grande Porte"
    };
    private static final String[] RAMOS = {
            "Tecnologia da Informação", "Comércio", "Indústria",
            "Saúde", "Educação", "Construção Civil", "Financeiro",
            "Logística", "Alimentação", "Consultoria"
    };

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
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        setupDropdowns(view);
        preencherDados(view);

        view.findViewById(R.id.btn_alterar_foto).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Selecione uma imagem", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_salvar).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void setupDropdowns(View view) {
        ((AutoCompleteTextView) view.findViewById(R.id.actv_porte))
                .setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, PORTES));
        ((AutoCompleteTextView) view.findViewById(R.id.actv_ramo))
                .setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, RAMOS));
    }

    private void preencherDados(View view) {
        ((TextInputEditText) view.findViewById(R.id.et_cnpj))
                .setText("00.000.000/0001-00");
        ((TextInputEditText) view.findViewById(R.id.et_razao_social))
                .setText("Empresa XYZ Soluções Tecnológicas Ltda.");
        ((TextInputEditText) view.findViewById(R.id.et_nome_fantasia))
                .setText("Empresa XYZ");
        ((AutoCompleteTextView) view.findViewById(R.id.actv_porte))
                .setText("Médio Porte", false);
        ((AutoCompleteTextView) view.findViewById(R.id.actv_ramo))
                .setText("Tecnologia da Informação", false);
        ((TextInputEditText) view.findViewById(R.id.et_site))
                .setText("https://www.empresaxyz.com.br");
        ((TextInputEditText) view.findViewById(R.id.et_telefone))
                .setText("(11) 3000-0000");
    }
}
