package com.edu.matchvagasempresas.features.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RedefinirSenhaFragment extends Fragment {

    private RedefinirSenhaViewModel viewModel;
    private MaterialButton btnRedefinir;
    private View llLoading;
    private String token;
    private View anchorView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_redefinir_senha, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(RedefinirSenhaViewModel.class);
        anchorView = view;

        if (getArguments() != null) {
            token = getArguments().getString("token", "");
        }

        btnRedefinir = view.findViewById(R.id.btn_redefinir);
        llLoading = view.findViewById(R.id.ll_loading);
        TextInputEditText etNovaSenha = view.findViewById(R.id.et_nova_senha);
        TextInputEditText etConfirmarSenha = view.findViewById(R.id.et_confirmar_senha);

        viewModel.getResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Toast.makeText(requireContext(), "Senha redefinida com sucesso!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(anchorView)
                            .navigate(R.id.action_redefinirSenha_to_login);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });

        btnRedefinir.setOnClickListener(v -> {
            String novaSenha = getText(etNovaSenha);
            String confirmar = getText(etConfirmarSenha);
            if (novaSenha.length() < 6) {
                Toast.makeText(requireContext(), "A senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!novaSenha.equals(confirmar)) {
                Toast.makeText(requireContext(), "As senhas não coincidem", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.redefinirSenha(token, novaSenha);
        });

        view.findViewById(R.id.tv_voltar_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_redefinirSenha_to_login));
    }

    private void setLoading(boolean loading) {
        btnRedefinir.setEnabled(!loading);
        btnRedefinir.setText(loading ? "" : getString(R.string.btn_redefinir_senha));
        llLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
