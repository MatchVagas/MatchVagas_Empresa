package com.edu.matchvagasempresas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

public class DetalhesCandidaturaFragment extends Fragment {

    private static final String[] STATUS = {
            "Pendente", "Em Análise", "Aprovado", "Reprovado", "Em Entrevista"
    };

    // Dados mock do perfil compartilhado pelo candidato
    private static final String ESCOLARIDADE    = "Ensino Superior Completo";
    private static final String AREA_FORMACAO   = "Ciência da Computação";
    private static final String EXPERIENCIA     = "3 anos em desenvolvimento mobile (Android / iOS)";
    private static final String HABILIDADES     = "Java  ·  Kotlin  ·  Android SDK  ·  SQL  ·  Git  ·  REST APIs";
    private static final String RESUMO          =
            "Desenvolvedora mobile apaixonada por criar experiências intuitivas e de alto desempenho. " +
            "Atua com ciclo completo de desenvolvimento Android, desde a arquitetura até a publicação " +
            "na Play Store. Experiência com equipes ágeis e entregas contínuas.";

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

        preencherPerfilCompartilhado(view);

        AutoCompleteTextView actv = view.findViewById(R.id.actv_novo_status);
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, STATUS));
        actv.setText(STATUS[0], false);

        view.findViewById(R.id.btn_salvar_status).setOnClickListener(v -> {
            Snackbar.make(v, "Status atualizado para: " + actv.getText(), Snackbar.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });

        view.findViewById(R.id.btn_ver_curriculo).setOnClickListener(v ->
                Snackbar.make(v, "Abrindo currículo em PDF…", Snackbar.LENGTH_SHORT).show());
    }

    private void preencherPerfilCompartilhado(View view) {
        ((TextView) view.findViewById(R.id.tv_escolaridade)).setText(ESCOLARIDADE);
        ((TextView) view.findViewById(R.id.tv_area_formacao)).setText(AREA_FORMACAO);
        ((TextView) view.findViewById(R.id.tv_experiencia)).setText(EXPERIENCIA);
        ((TextView) view.findViewById(R.id.tv_habilidades)).setText(HABILIDADES);
        ((TextView) view.findViewById(R.id.tv_resumo)).setText(RESUMO);
    }
}
