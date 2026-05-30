package com.edu.matchvagasempresas.features.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class VerificarCodigoFragment extends Fragment {

    private VerificarCodigoViewModel viewModel;
    private MaterialButton btnVerificar;
    private View llLoading;
    private String email;
    private View anchorView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_verificar_codigo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(VerificarCodigoViewModel.class);
        anchorView = view;

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
        }

        btnVerificar = view.findViewById(R.id.btn_verificar);
        llLoading = view.findViewById(R.id.ll_loading);
        TextInputEditText etCodigo = view.findViewById(R.id.et_codigo);

        TextView tvDesc = view.findViewById(R.id.tv_desc_codigo);
        if (email != null && !email.isEmpty()) {
            tvDesc.setText(getString(R.string.desc_verificar_codigo_email, email));
        }

        viewModel.getTokenResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Bundle args = new Bundle();
                    args.putString("token", resource.getData());
                    Navigation.findNavController(anchorView)
                            .navigate(R.id.action_verificarCodigo_to_redefinirSenha, args);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(requireContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        btnVerificar.setOnClickListener(v -> {
            String codigo = etCodigo.getText() != null ? etCodigo.getText().toString().trim() : "";
            if (codigo.length() != 6) {
                Toast.makeText(requireContext(), "Digite o código de 6 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.verificarCodigo(email, codigo);
        });

        view.findViewById(R.id.tv_voltar_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_verificarCodigo_to_login));
    }

    private void setLoading(boolean loading) {
        btnVerificar.setEnabled(!loading);
        btnVerificar.setText(loading ? "" : getString(R.string.btn_verificar));
        llLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
