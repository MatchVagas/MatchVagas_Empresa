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

public class EsqueceuSenhaFragment extends Fragment {

    private EsqueceuSenhaViewModel viewModel;
    private MaterialButton btnEnviar;
    private View llLoading;
    private View anchorView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_esqueceu_senha, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EsqueceuSenhaViewModel.class);
        anchorView = view;

        btnEnviar = view.findViewById(R.id.btn_enviar);
        llLoading = view.findViewById(R.id.ll_loading);
        TextInputEditText etEmail = view.findViewById(R.id.et_email);

        viewModel.getResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Bundle args = new Bundle();
                    args.putString("email", etEmail.getText() != null
                            ? etEmail.getText().toString().trim() : "");
                    Navigation.findNavController(anchorView)
                            .navigate(R.id.action_esqueceuSenha_to_verificarCodigo, args);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        });

        btnEnviar.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Informe o e-mail", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.solicitarCodigo(email);
        });

        view.findViewById(R.id.tv_voltar_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void setLoading(boolean loading) {
        btnEnviar.setEnabled(!loading);
        btnEnviar.setText(loading ? "" : getString(R.string.btn_enviar_codigo));
        llLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
