package com.edu.matchvagasempresas.feature.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.AuthResponse;
import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.ApiService;
import com.edu.matchvagasempresas.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnLogin = view.findViewById(R.id.btn_login);
        TextInputEditText etEmail = view.findViewById(R.id.et_email);
        TextInputEditText etSenha = view.findViewById(R.id.et_senha);

        btnLogin.setOnClickListener(v -> {
            String email = getText(etEmail);
            String senha = getText(etSenha);
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
                return;
            }
            btnLogin.setEnabled(false);
            fazerLogin(v, email, senha, btnLogin);
        });

        view.findViewById(R.id.tv_cadastrar).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_cadastroEmpresa));

        view.findViewById(R.id.tv_esqueceu_senha).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Funcionalidade em breve", Toast.LENGTH_SHORT).show());
    }

    private void fazerLogin(View anchor, String email, String senha, MaterialButton btnLogin) {
        ApiService api = ApiClient.getService(requireContext());

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("senha", senha);

        api.login(body).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    new SessionManager(requireContext()).saveAuth(auth.token, auth.usuarioId);
                    carregarEmpresa(anchor, btnLogin);
                } else {
                    btnLogin.setEnabled(true);
                    Toast.makeText(requireContext(), "E-mail ou senha inválidos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                btnLogin.setEnabled(true);
                Toast.makeText(requireContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void carregarEmpresa(View anchor, MaterialButton btnLogin) {
        ApiClient.getService(requireContext()).minhaEmpresa().enqueue(new Callback<EmpresaResponse>() {
            @Override
            public void onResponse(@NonNull Call<EmpresaResponse> call, @NonNull Response<EmpresaResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    EmpresaResponse emp = response.body();
                    new SessionManager(requireContext()).saveEmpresa(emp.id, emp.nomeFantasia);
                }
                Navigation.findNavController(anchor).navigate(R.id.action_login_to_dashboard);
            }

            @Override
            public void onFailure(@NonNull Call<EmpresaResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Navigation.findNavController(anchor).navigate(R.id.action_login_to_dashboard);
            }
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
