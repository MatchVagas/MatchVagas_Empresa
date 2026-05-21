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

public class EsqueceuSenhaFragment extends Fragment {

    private MaterialButton btnEnviar;
    private View llLoading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_esqueceu_senha, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnEnviar = view.findViewById(R.id.btn_enviar);
        llLoading = view.findViewById(R.id.ll_loading);
        TextInputEditText etEmail = view.findViewById(R.id.et_email);

        btnEnviar.setOnClickListener(v -> {
            String email = getText(etEmail);
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Informe o e-mail", Toast.LENGTH_SHORT).show();
                return;
            }
            setLoading(true);
            solicitarCodigo(v, email);
        });

        view.findViewById(R.id.tv_voltar_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void solicitarCodigo(View anchor, String email) {
        ApiService api = ApiClient.getService(requireContext());

        JsonObject body = new JsonObject();
        body.addProperty("email", email);

        api.esqueceuSenha(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                setLoading(false);
                // Always navigate regardless of whether email exists (backend hides this)
                Bundle args = new Bundle();
                args.putString("email", email);
                Navigation.findNavController(anchor)
                        .navigate(R.id.action_esqueceuSenha_to_verificarCodigo, args);
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
        btnEnviar.setEnabled(!loading);
        btnEnviar.setText(loading ? "" : getString(R.string.btn_enviar_codigo));
        llLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
