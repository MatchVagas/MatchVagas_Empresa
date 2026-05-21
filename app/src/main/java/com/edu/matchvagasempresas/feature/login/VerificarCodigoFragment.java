package com.edu.matchvagasempresas.feature.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class VerificarCodigoFragment extends Fragment {

    private MaterialButton btnVerificar;
    private View llLoading;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_verificar_codigo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            email = getArguments().getString("email", "");
        }

        btnVerificar = view.findViewById(R.id.btn_verificar);
        llLoading = view.findViewById(R.id.ll_loading);
        TextInputEditText etCodigo = view.findViewById(R.id.et_codigo);

        TextView tvDesc = view.findViewById(R.id.tv_desc_codigo);
        if (!email.isEmpty()) {
            tvDesc.setText(getString(R.string.desc_verificar_codigo_email, email));
        }

        btnVerificar.setOnClickListener(v -> {
            String codigo = getText(etCodigo);
            if (codigo.length() != 6) {
                Toast.makeText(requireContext(), "Digite o código de 6 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }
            setLoading(true);
            verificarCodigo(v, codigo);
        });

        view.findViewById(R.id.tv_voltar_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_verificarCodigo_to_login));
    }

    private void verificarCodigo(View anchor, String codigo) {
        ApiService api = ApiClient.getService(requireContext());

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("codigo", codigo);

        api.verificarCodigo(body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().get("token").getAsString();
                    Bundle args = new Bundle();
                    args.putString("token", token);
                    Navigation.findNavController(anchor)
                            .navigate(R.id.action_verificarCodigo_to_redefinirSenha, args);
                } else {
                    Toast.makeText(requireContext(), "Código inválido ou expirado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                setLoading(false);
                Toast.makeText(requireContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnVerificar.setEnabled(!loading);
        btnVerificar.setText(loading ? "" : getString(R.string.btn_verificar));
        llLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
