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
import com.edu.matchvagasempresas.data.local.SessionManager;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    private LoginViewModel viewModel;
    private MaterialButton btnLogin;
    private View llLoading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        btnLogin = view.findViewById(R.id.btn_login);
        llLoading = view.findViewById(R.id.ll_loading);
        TextInputEditText etEmail = view.findViewById(R.id.et_email);
        TextInputEditText etSenha = view.findViewById(R.id.et_senha);

        viewModel.getLoginResult().observe(getViewLifecycleOwner(), resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    if (resource.getData() != null) {
                        SessionManager session = new SessionManager(requireContext());
                        session.saveAuth(resource.getData().auth.token,
                                resource.getData().auth.usuarioId);
                        if (resource.getData().empresa != null) {
                            session.saveEmpresa(resource.getData().empresa.id,
                                    resource.getData().empresa.nomeFantasia,
                                    resource.getData().empresa.status);
                        }
                    }
                    Navigation.findNavController(view).navigate(R.id.action_login_to_dashboard);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(requireContext(),
                            resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        btnLogin.setOnClickListener(v -> {
            String email = getText(etEmail);
            String senha = getText(etSenha);
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, senha);
        });

        view.findViewById(R.id.tv_cadastrar).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_cadastroEmpresa));

        view.findViewById(R.id.tv_esqueceu_senha).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_esqueceuSenha));
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "" : getString(R.string.btn_entrar));
        llLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
