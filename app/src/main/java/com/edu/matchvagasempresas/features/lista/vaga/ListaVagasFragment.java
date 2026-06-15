package com.edu.matchvagasempresas.features.lista.vaga;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;
import com.edu.matchvagasempresas.features.lista.vaga.adapter.VagasAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListaVagasFragment extends Fragment {

    private ListaVagasViewModel viewModel;
    private VagasAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private View tvNenhumaVaga;

    private final List<VagaResponse> todasVagas = new ArrayList<>();
    private String filtroAtual = "";

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

        TextInputEditText etPesquisar = view.findViewById(R.id.et_pesquisar);
        etPesquisar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filtroAtual = s != null ? s.toString().trim() : "";
                aplicarFiltro();
            }
        });

        viewModel.getVagasCached().observe(getViewLifecycleOwner(), this::atualizarLista);

        viewModel.getVagasFresh().observe(getViewLifecycleOwner(), this::atualizarLista);

        viewModel.getRefreshResult().observe(getViewLifecycleOwner(), resource -> {
            swipeRefresh.setRefreshing(false);
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null) {
                        atualizarLista(resource.getData());
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

    private void atualizarLista(List<VagaResponse> vagas) {
        todasVagas.clear();
        if (vagas != null) todasVagas.addAll(vagas);
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        List<VagaResponse> filtradas;
        if (filtroAtual.isEmpty()) {
            filtradas = new ArrayList<>(todasVagas);
        } else {
            String termo = filtroAtual.toLowerCase(Locale.getDefault());
            filtradas = new ArrayList<>();
            for (VagaResponse v : todasVagas) {
                if (contem(v.titulo, termo)
                        || contem(v.areaAtuacao, termo)
                        || contem(v.modalidadeDescricao, termo)
                        || contem(v.tipoVagaDescricao, termo)) {
                    filtradas.add(v);
                }
            }
        }
        adapter.submitList(filtradas);
        atualizarEstadoVazio(filtradas);
    }

    private boolean contem(String campo, String termo) {
        return campo != null && campo.toLowerCase(Locale.getDefault()).contains(termo);
    }

    private void atualizarEstadoVazio(java.util.List<?> lista) {
        if (tvNenhumaVaga != null)
            tvNenhumaVaga.setVisibility(lista == null || lista.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
