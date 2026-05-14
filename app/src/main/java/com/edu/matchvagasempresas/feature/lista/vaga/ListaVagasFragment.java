package com.edu.matchvagasempresas.feature.lista.vaga;

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
import com.edu.matchvagasempresas.adapter.VagasAdapter;
import com.edu.matchvagasempresas.model.VagaResponse;
import com.edu.matchvagasempresas.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaVagasFragment extends Fragment {

    private VagasAdapter adapter;
    private final List<VagaResponse> vagas = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_lista_vagas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.fab_nova_vaga).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_listaVagas_to_cadastroVaga));

        RecyclerView rv = view.findViewById(R.id.rv_vagas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new VagasAdapter(requireContext(), vagas, new VagasAdapter.OnVagaActionListener() {
            @Override
            public void onVagaClick(long vagaId) { }

            @Override
            public void onCandidaturasClick(long vagaId) {
                Bundle args = new Bundle();
                args.putLong("vagaId", vagaId);
                Navigation.findNavController(view).navigate(R.id.action_listaVagas_to_listaCandidaturas, args);
            }

            @Override
            public void onEditarClick(long vagaId) {
                Bundle args = new Bundle();
                args.putLong("vagaId", vagaId);
                Navigation.findNavController(view).navigate(R.id.action_listaVagas_to_editarVaga, args);
            }

            @Override
            public void onGerenciarClick(long vagaId) {
                Bundle args = new Bundle();
                args.putLong("vagaId", vagaId);
                Navigation.findNavController(view).navigate(R.id.action_listaVagas_to_gerenciarVaga, args);
            }
        });
        rv.setAdapter(adapter);

        carregarVagas();
    }

    private void carregarVagas() {
        ApiClient.getService(requireContext()).minhasVagas().enqueue(new Callback<List<VagaResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<VagaResponse>> call,
                                   @NonNull Response<List<VagaResponse>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    vagas.clear();
                    vagas.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<VagaResponse>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Erro ao carregar vagas: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
