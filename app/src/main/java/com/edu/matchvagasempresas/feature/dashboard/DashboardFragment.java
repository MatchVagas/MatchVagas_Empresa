package com.edu.matchvagasempresas.feature.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.adapter.VagasAdapter;

public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.tv_ver_todas).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_listaVagas));

        view.findViewById(R.id.iv_logout).setOnClickListener(v -> confirmLogout(v));

        setupVagasRecentes(view);
    }

    private void setupVagasRecentes(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_vagas_recentes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new VagasAdapter(requireContext(), 3, new VagasAdapter.OnVagaActionListener() {
            @Override
            public void onVagaClick(int position) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_listaCandidaturas);
            }
            @Override
            public void onCandidaturasClick(int position) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_listaCandidaturas);
            }
            @Override
            public void onEditarClick(int position) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_listaVagas);
            }
            @Override
            public void onGerenciarClick(int position) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_listaVagas);
            }
        }));
    }

    private void confirmLogout(View anchor) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sair")
                .setMessage("Deseja encerrar a sessão?")
                .setPositiveButton("Sair", (d, w) ->
                        Navigation.findNavController(anchor).navigate(R.id.action_logout))
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }
}
