package com.edu.matchvagasempresas.feature.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.adapter.VagasAdapter;
import com.edu.matchvagasempresas.model.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.model.VagaResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private VagasAdapter adapter;
    private final List<VagaResponse> vagas = new ArrayList<>();
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

        // Exibe nome do cache enquanto a API carrega
        SessionManager session = new SessionManager(requireContext());
        setText(R.id.tv_nome_empresa, session.getNomeEmpresa());

        // Banner de empresa pendente
        MaterialCardView cardPendente = view.findViewById(R.id.card_pendente);
        if (cardPendente != null && !session.isEmpresaAprovada() && !session.getStatusEmpresa().isEmpty()) {
            cardPendente.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.tv_ver_todas).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_listaVagas));
        view.findViewById(R.id.iv_logout).setOnClickListener(v -> confirmLogout(v));

        setupVagasRecentes(view);
        carregarEmpresa();
        carregarVagas();
        carregarCandidaturas();
    }

    // ── Empresa ──────────────────────────────────────────────────────────────

    private void carregarEmpresa() {
        ApiClient.getService(requireContext()).minhaEmpresa()
                .enqueue(new Callback<EmpresaResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<EmpresaResponse> call,
                                           @NonNull Response<EmpresaResponse> r) {
                        if (!isAdded()) return;
                        if (r.isSuccessful() && r.body() != null) {
                            EmpresaResponse e = r.body();
                            setText(R.id.tv_nome_empresa, e.nomeFantasia);
                            setText(R.id.tv_cnpj,         formatarCnpj(e.cnpj));

                            // Atualiza sessão com dados frescos
                            new SessionManager(requireContext())
                                    .saveEmpresa(e.id, e.nomeFantasia, e.status);

                            // Atualiza banner de pendente
                            MaterialCardView card = rootView.findViewById(R.id.card_pendente);
                            if (card != null) {
                                boolean pendente = "PENDENTE".equalsIgnoreCase(e.status);
                                card.setVisibility(pendente ? View.VISIBLE : View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EmpresaResponse> call, @NonNull Throwable t) {}
                });
    }

    // ── Vagas ─────────────────────────────────────────────────────────────────

    private void setupVagasRecentes(View view) {
        RecyclerView rv = view.findViewById(R.id.rv_vagas_recentes);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new VagasAdapter(requireContext(), vagas, new VagasAdapter.OnVagaActionListener() {
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

    private void carregarVagas() {
        ApiClient.getService(requireContext()).minhasVagas()
                .enqueue(new Callback<List<VagaResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<VagaResponse>> call,
                                           @NonNull Response<List<VagaResponse>> r) {
                        if (!isAdded()) return;
                        if (r.isSuccessful() && r.body() != null) {
                            List<VagaResponse> all = r.body();

                            // Contadores
                            long ativas     = all.stream().filter(v -> isAtiva(v.statusDescricao)).count();
                            long expiradas  = all.stream().filter(v -> isExpirada(v.statusDescricao)).count();

                            setText(R.id.tv_total_vagas,     String.valueOf(ativas));
                            setText(R.id.tv_vagas_expiradas, String.valueOf(expiradas));

                            // Vagas recentes (até 3)
                            vagas.clear();
                            vagas.addAll(all.subList(0, Math.min(3, all.size())));
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<VagaResponse>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Erro ao carregar vagas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Candidaturas ──────────────────────────────────────────────────────────

    private void carregarCandidaturas() {
        ApiClient.getService(requireContext()).candidaturasEmpresa()
                .enqueue(new Callback<List<CandidaturaEmpresaResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CandidaturaEmpresaResponse>> call,
                                           @NonNull Response<List<CandidaturaEmpresaResponse>> r) {
                        if (!isAdded()) return;
                        if (r.isSuccessful() && r.body() != null) {
                            setText(R.id.tv_total_candidaturas, String.valueOf(r.body().size()));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<CandidaturaEmpresaResponse>> call, @NonNull Throwable t) {}
                });
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    private void confirmLogout(View anchor) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sair")
                .setMessage("Deseja encerrar a sessão?")
                .setPositiveButton("Sair", (d, w) -> {
                    new SessionManager(requireContext()).clear();
                    ApiClient.reset();
                    Navigation.findNavController(anchor).navigate(R.id.action_logout);
                })
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
