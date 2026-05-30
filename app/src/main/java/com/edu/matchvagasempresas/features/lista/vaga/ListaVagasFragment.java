package com.edu.matchvagasempresas.features.lista.vaga;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.features.lista.vaga.adapter.VagasAdapter;

public class ListaVagasFragment extends Fragment {

    private ListaVagasViewModel viewModel;
    private VagasAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View tvNenhumaVaga;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_lista_vagas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ListaVagasViewModel.class);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.fab_nova_vaga).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_listaVagas_to_cadastroVaga));

        tvNenhumaVaga = view.findViewById(R.id.tv_nenhuma_vaga);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(() -> viewModel.refreshVagas());

        RecyclerView rv = view.findViewById(R.id.rv_vagas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new VagasAdapter(requireContext(), new VagasAdapter.OnVagaActionListener() {
            @Override public void onVagaClick(long vagaId) {}

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

        viewModel.getVagasCached().observe(getViewLifecycleOwner(), vagas -> {
            adapter.submitList(vagas);
            atualizarEstadoVazio(vagas);
        });

        viewModel.getVagasFresh().observe(getViewLifecycleOwner(), vagas -> {
            adapter.submitList(vagas);
            atualizarEstadoVazio(vagas);
        });

        viewModel.getRefreshResult().observe(getViewLifecycleOwner(), resource -> {
            swipeRefresh.setRefreshing(false);
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null) {
                        adapter.submitList(resource.getData());
                        atualizarEstadoVazio(resource.getData());
                    }
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), "Erro ao atualizar vagas", Toast.LENGTH_SHORT).show();
                    break;
                default: break;
            }
        });

        viewModel.carregarVagas();
    }

    private void atualizarEstadoVazio(java.util.List<?> lista) {
        if (tvNenhumaVaga != null)
            tvNenhumaVaga.setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
