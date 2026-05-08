package com.edu.matchvagasempresas.feature.cadastro.empresa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;

public class CadastroEmpresaFragment extends Fragment {

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
        return inflater.inflate(R.layout.activity_cadastro_empresa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        setupDropdowns(view);

        view.findViewById(R.id.btn_cadastrar).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_cadastroEmpresa_to_dashboard));

        view.findViewById(R.id.tv_fazer_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void setupDropdowns(View view) {
        ((AutoCompleteTextView) view.findViewById(R.id.actv_porte))
                .setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, PORTES));

        ((AutoCompleteTextView) view.findViewById(R.id.actv_ramo))
                .setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, RAMOS));
    }
}
