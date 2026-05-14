package com.edu.matchvagasempresas.feature.lista.candidatura;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.adapter.CandidaturasAdapter;
import com.edu.matchvagasempresas.model.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaCandidaturasFragment extends Fragment {

    private CandidaturasAdapter adapter;
    private final List<CandidaturaEmpresaResponse> candidaturas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_lista_candidaturas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        RecyclerView rv = view.findViewById(R.id.rv_candidaturas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CandidaturasAdapter(requireContext(), candidaturas, candidaturaId -> {
            Bundle args = new Bundle();
            args.putLong("candidaturaId", candidaturaId);
            Navigation.findNavController(view).navigate(R.id.action_listaCandidaturas_to_detalhes, args);
        });
        rv.setAdapter(adapter);

        long vagaId = getArguments() != null ? getArguments().getLong("vagaId", -1) : -1;
        carregarCandidaturas(vagaId);
    }

    private void carregarCandidaturas(long vagaId) {
        ApiService api = ApiClient.getService(requireContext());
        Call<List<CandidaturaEmpresaResponse>> call = vagaId > 0
                ? api.candidatosPorVaga(vagaId)
                : api.candidaturasEmpresa();

        call.enqueue(new Callback<List<CandidaturaEmpresaResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<CandidaturaEmpresaResponse>> c,
                                   @NonNull Response<List<CandidaturaEmpresaResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    candidaturas.clear();
                    candidaturas.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CandidaturaEmpresaResponse>> c, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro ao carregar candidaturas: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
