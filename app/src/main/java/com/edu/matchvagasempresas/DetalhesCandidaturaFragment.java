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

public class DetalhesCandidaturaFragment extends Fragment {

    private static final String[] STATUS = {
            "Pendente", "Em Análise", "Aprovado", "Reprovado", "Em Entrevista"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_detalhes_candidatura, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        AutoCompleteTextView actv = view.findViewById(R.id.actv_novo_status);
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, STATUS));
        actv.setText(STATUS[0], false);

        view.findViewById(R.id.btn_salvar_status).setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Status atualizado para: " + actv.getText(),
                    Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });
    }
}
