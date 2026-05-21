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
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.ApiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RedefinirSenhaFragment extends Fragment {

    private MaterialButton btnRedefinir;
    private View llLoading;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_redefinir_senha, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            token = getArguments().getString("token", "");
        }

        btnRedefinir = view.findViewById(R.id.btn_redefinir);
        llLoading = view.findViewById(R.id.ll_loading);
        TextInputEditText etNovaSenha = view.findViewById(R.id.et_nova_senha);
        TextInputEditText etConfirmarSenha = view.findViewById(R.id.et_confirmar_senha);

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
            setLoading(true);
            redefinirSenha(v, novaSenha);
        });

        view.findViewById(R.id.tv_voltar_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_redefinirSenha_to_login));
    }

    private void redefinirSenha(View anchor, String novaSenha) {
        ApiService api = ApiClient.getService(requireContext());

        JsonObject body = new JsonObject();
        body.addProperty("token", token);
        body.addProperty("novaSenha", novaSenha);

        api.redefinirSenha(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Senha redefinida com sucesso!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(anchor)
                            .navigate(R.id.action_redefinirSenha_to_login);
                } else {
                    Toast.makeText(requireContext(), "Token inválido ou expirado. Tente novamente.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setLoading(false);
                Toast.makeText(requireContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
