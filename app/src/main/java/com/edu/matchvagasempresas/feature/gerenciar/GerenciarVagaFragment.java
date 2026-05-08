package com.edu.matchvagasempresas.feature.gerenciar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class GerenciarVagaFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_gerenciar_vaga, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        MaterialSwitch sw = view.findViewById(R.id.switch_ativa);
        sw.setOnCheckedChangeListener((btn, checked) ->
                Toast.makeText(requireContext(),
                        checked ? "Vaga ativada" : "Vaga desativada",
                        Toast.LENGTH_SHORT).show());

        TextInputEditText etData = view.findViewById(R.id.et_data_publicacao);
        TextInputLayout tilData = view.findViewById(R.id.til_data_publicacao);
        TextInputEditText etHora = view.findViewById(R.id.et_hora_publicacao);

        etData.setOnClickListener(v -> showDatePicker(etData));
        tilData.setEndIconOnClickListener(v -> showDatePicker(etData));
        etHora.setOnClickListener(v -> showTimePicker(etHora));

        view.findViewById(R.id.btn_salvar_programacao).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Programação salva!", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_deletar).setOnClickListener(v -> confirmDelete(v));
    }

    private void showDatePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (v, year, month, day) -> et.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker(TextInputEditText et) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (v, hour, minute) -> et.setText(
                        String.format(Locale.getDefault(), "%02d:%02d", hour, minute)),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
                .show();
    }

    private void confirmDelete(View anchor) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_deletar_titulo)
                .setMessage(R.string.confirm_deletar_msg)
                .setPositiveButton(R.string.btn_confirmar, (d, w) -> {
                    Toast.makeText(requireContext(), "Vaga excluída", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(anchor).navigateUp();
                })
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }
}
