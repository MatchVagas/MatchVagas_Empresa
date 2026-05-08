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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class CadastroVagaFragment extends Fragment {

    private static final String[] TIPOS_VAGA = {"CLT", "PJ", "Estágio", "Temporário", "Autônomo", "Freelancer"};
    private static final String[] MODALIDADES = {"Presencial", "Remoto", "Híbrido"};
    private static final String[] ESCOLARIDADES = {
            "Fundamental", "Médio", "Técnico", "Superior Incompleto",
            "Superior Completo", "Pós-Graduação", "Mestrado", "Doutorado"
    };
    private static final String[] CIDADES = {
            "São Paulo - SP", "Rio de Janeiro - RJ", "Belo Horizonte - MG",
            "Curitiba - PR", "Porto Alegre - RS", "Salvador - BA",
            "Fortaleza - CE", "Recife - PE", "Manaus - AM", "Brasília - DF"
    };

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
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        setupDropdowns(view);
        setupDatePicker(view);

        view.findViewById(R.id.btn_publicar).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Vaga publicada com sucesso!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(v).navigateUp();
        });
    }

    private void setupDropdowns(View view) {
        ((AutoCompleteTextView) view.findViewById(R.id.actv_tipo_vaga))
                .setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, TIPOS_VAGA));
        ((AutoCompleteTextView) view.findViewById(R.id.actv_modalidade))
                .setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, MODALIDADES));
        ((AutoCompleteTextView) view.findViewById(R.id.actv_escolaridade))
                .setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, ESCOLARIDADES));
        ((AutoCompleteTextView) view.findViewById(R.id.actv_cidade))
                .setAdapter(new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_dropdown_item_1line, CIDADES));
    }

    private void setupDatePicker(View view) {
        TextInputEditText etData = view.findViewById(R.id.et_data_expiracao);
        TextInputLayout tilData = view.findViewById(R.id.til_data_expiracao);
        etData.setOnClickListener(v -> showDatePicker(etData));
        tilData.setEndIconOnClickListener(v -> showDatePicker(etData));
    }

    private void showDatePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (v, year, month, day) -> et.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }
}
