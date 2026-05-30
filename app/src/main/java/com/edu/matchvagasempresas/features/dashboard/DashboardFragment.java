package com.edu.matchvagasempresas.features.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.data.local.SessionManager;
import com.edu.matchvagasempresas.data.remote.RetrofitClient;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;
import com.edu.matchvagasempresas.data.repository.LookupRepositoryImpl;
import com.edu.matchvagasempresas.features.lista.vaga.adapter.VagasAdapter;

import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private VagasAdapter adapter;
    private View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        SessionManager session = new SessionManager(requireContext());
        setText(R.id.tv_nome_empresa, session.getNomeEmpresa());

        MaterialCardView cardPendente = view.findViewById(R.id.card_pendente);
        if (cardPendente != null && !session.isEmpresaAprovada() && !session.getStatusEmpresa().isEmpty()) {
            cardPendente.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.tv_ver_todas).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_listaVagas));
        view.findViewById(R.id.iv_logout).setOnClickListener(v -> confirmLogout(v));

        setupVagasRecentes(view);
        setupObservers(view);

        viewModel.carregarEmpresa();
        viewModel.carregarVagas();
        viewModel.carregarCandidaturas();
    }

    private void setupObservers(View view) {
        viewModel.getEmpresa().observe(getViewLifecycleOwner(), e -> {
            if (e == null) return;
            setText(R.id.tv_nome_empresa, e.nomeFantasia);
            setText(R.id.tv_cnpj, formatarCnpj(e.cnpj));
            new SessionManager(requireContext()).saveEmpresa(e.id, e.nomeFantasia, e.status);
            MaterialCardView card = rootView.findViewById(R.id.card_pendente);
            if (card != null)
                card.setVisibility("PENDENTE".equalsIgnoreCase(e.status) ? View.VISIBLE : View.GONE);
        });

        viewModel.getVagas().observe(getViewLifecycleOwner(), all -> {
            if (all == null) return;
            long ativas    = all.stream().filter(v -> isAtiva(v.statusDescricao)).count();
            long expiradas = all.stream().filter(v -> isExpirada(v.statusDescricao)).count();
            setText(R.id.tv_total_vagas,     String.valueOf(ativas));
            setText(R.id.tv_vagas_expiradas, String.valueOf(expiradas));
            adapter.submitList(all.subList(0, Math.min(3, all.size())));
        });

        viewModel.getTotalCandidaturas().observe(getViewLifecycleOwner(), total ->
                setText(R.id.tv_total_candidaturas, String.valueOf(total)));
    }

    private void setupVagasRecentes(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_vagas_recentes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new VagasAdapter(requireContext(), new VagasAdapter.OnVagaActionListener() {
            @Override
            public void onVagaClick(long vagaId) {
                Bundle args = new Bundle();
                args.putLong("vagaId", vagaId);
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_listaCandidaturas, args);
            }
            @Override
            public void onCandidaturasClick(long vagaId) {
                Bundle args = new Bundle();
                args.putLong("vagaId", vagaId);
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_listaCandidaturas, args);
            }
            @Override
            public void onEditarClick(long vagaId) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_listaVagas);
            }
            @Override
            public void onGerenciarClick(long vagaId) {
                Navigation.findNavController(view).navigate(R.id.action_dashboard_to_listaVagas);
            }
        });
        rv.setAdapter(adapter);
    }

    private void confirmLogout(View anchor) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sair")
                .setMessage("Deseja encerrar a sessão?")
                .setPositiveButton("Sair", (d, w) -> {
                    new SessionManager(requireContext()).clear();
                    LookupRepositoryImpl.get(requireContext()).clear();
                    RetrofitClient.reset();
                    Navigation.findNavController(anchor).navigate(R.id.action_logout);
                })
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    private void setText(int id, String value) {
        if (rootView == null) return;
        TextView tv = rootView.findViewById(id);
        if (tv != null) tv.setText(value != null ? value : "");
    }

    private boolean isAtiva(String status) {
        return status != null && (status.equalsIgnoreCase("ATIVA") || status.equalsIgnoreCase("Ativa"));
    }

    private boolean isExpirada(String status) {
        return status != null && (
                status.equalsIgnoreCase("EXPIRADA") ||
                status.equalsIgnoreCase("ENCERRADA") ||
                status.equalsIgnoreCase("INATIVA"));
    }

    private String formatarCnpj(String cnpj) {
        if (cnpj == null) return "";
        String d = cnpj.replaceAll("[^0-9]", "");
        if (d.length() == 14)
            return d.substring(0,2)+"."+d.substring(2,5)+"."+d.substring(5,8)
                    +"/"+d.substring(8,12)+"-"+d.substring(12);
        return cnpj;
    }
}
