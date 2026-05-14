package com.edu.matchvagasempresas.feature.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.adapter.VagasAdapter;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());
        TextView tvNome = view.findViewById(R.id.tv_nome_empresa);
        if (tvNome != null) tvNome.setText(session.getNomeEmpresa());

        view.findViewById(R.id.tv_ver_todas).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_dashboard_to_listaVagas));

        view.findViewById(R.id.iv_logout).setOnClickListener(v -> confirmLogout(v));

        setupVagasRecentes(view);
        carregarVagas(view);
    }

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

    private void carregarVagas(View view) {
        ApiClient.getService(requireContext()).minhasVagas().enqueue(new Callback<List<VagaResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<VagaResponse>> call,
                                   @NonNull Response<List<VagaResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<VagaResponse> all = response.body();
                    vagas.clear();
                    int limit = Math.min(3, all.size());
                    vagas.addAll(all.subList(0, limit));
                    adapter.notifyDataSetChanged();

                    TextView tvTotal = view.findViewById(R.id.tv_total_vagas);
                    if (tvTotal != null) tvTotal.setText(String.valueOf(all.size()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VagaResponse>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro ao carregar vagas", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
}
