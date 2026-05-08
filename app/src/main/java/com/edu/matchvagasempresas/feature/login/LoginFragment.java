package com.edu.matchvagasempresas.feature.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.edu.matchvagasempresas.R;

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

        view.findViewById(R.id.btn_login).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_dashboard));

        view.findViewById(R.id.tv_cadastrar).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_cadastroEmpresa));

        view.findViewById(R.id.tv_esqueceu_senha).setOnClickListener(v -> { /* TODO */ });
    }
}
