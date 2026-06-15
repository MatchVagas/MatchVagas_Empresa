package com.edu.matchvagasempresas.features.lista.candidatura;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
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
import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.LookupItem;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;
import com.edu.matchvagasempresas.features.lista.candidatura.adapter.CandidaturasAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListaCandidaturasFragment extends Fragment {

    private ListaCandidaturasViewModel viewModel;
    private CandidaturasAdapter adapter;
    private final List<CandidaturaEmpresaResponse> todas = new ArrayList<>();
    private final List<VagaResponse> vagas = new ArrayList<>();
    private View tvNenhuma;
    private SwipeRefreshLayout swipeRefresh;
    private long vagaIdFixo = -1;
    private long vagaIdSelecionada = -1;
    private String filtroAtivo = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_lista_candidaturas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ListaCandidaturasViewModel.class);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        tvNenhuma = view.findViewById(R.id.tv_nenhuma_candidatura);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(this::atualizar);

        RecyclerView rv = view.findViewById(R.id.rv_candidaturas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CandidaturasAdapter(requireContext(), candidaturaId -> {
            Bundle args = new Bundle();
            args.putLong("candidaturaId", candidaturaId);
            Navigation.findNavController(view).navigate(R.id.action_listaCandidaturas_to_detalhes, args);
        });
        rv.setAdapter(adapter);

        setupFiltros(view);

        viewModel.getCandidaturasCached().observe(getViewLifecycleOwner(), cached -> {
            if (cached != null) {
                todas.clear();
                todas.addAll(cached);
                aplicarFiltro(filtroAtivo);
                atualizarNomeVaga(view, cached);
            }
        });

        viewModel.getCandidaturasFresh().observe(getViewLifecycleOwner(), fresh -> {
            if (fresh != null) {
                todas.clear();
                todas.addAll(fresh);
                aplicarFiltro(filtroAtivo);
                atualizarNomeVaga(view, fresh);
            }
        });

        viewModel.getRefreshResult().observe(getViewLifecycleOwner(), resource -> {
            swipeRefresh.setRefreshing(false);
            switch (resource.getStatus()) {
                case SUCCESS:
                    if (resource.getData() != null) {
                        todas.clear();
                        todas.addAll(resource.getData());
                        aplicarFiltro(filtroAtivo);
                    }
                    break;
                case ERROR:
                    Toast.makeText(requireContext(), "Erro ao atualizar candidaturas", Toast.LENGTH_SHORT).show();
                    break;
                default: break;
            }
        });

        viewModel.getVagas().observe(getViewLifecycleOwner(), lista -> {
            vagas.clear();
            if (lista != null) vagas.addAll(lista);
            bindSeletorVaga(view);
        });

        viewModel.getStatusOpcoes().observe(getViewLifecycleOwner(),
                statusList -> popularChipsStatus(view, statusList));
        viewModel.carregarStatusOpcoes();

        vagaIdFixo = getArguments() != null ? getArguments().getLong("vagaId", -1) : -1;

        if (vagaIdFixo > 0) {
            vagaIdSelecionada = vagaIdFixo;
            mostrarVagaFixa(view);
            viewModel.carregarCandidaturas(vagaIdFixo);
        } else {
            mostrarSeletorVaga(view);
            viewModel.carregarVagas();
        }
    }

    private void atualizar() {
        if (vagaIdSelecionada > 0) viewModel.refreshCandidaturas(vagaIdSelecionada);
        else swipeRefresh.setRefreshing(false);
    }

    private void mostrarVagaFixa(View view) {
        view.findViewById(R.id.layout_vaga_fixa).setVisibility(View.VISIBLE);
        view.findViewById(R.id.til_vaga_selector).setVisibility(View.GONE);
    }

    private void mostrarSeletorVaga(View view) {
        view.findViewById(R.id.til_vaga_selector).setVisibility(View.VISIBLE);
        view.findViewById(R.id.layout_vaga_fixa).setVisibility(View.GONE);
    }

    private void bindSeletorVaga(View view) {
        AutoCompleteTextView actv = view.findViewById(R.id.actv_vaga_selector);
        if (actv == null) return;
        List<String> titulos = vagas.stream()
                .map(v -> v.titulo != null ? v.titulo : "Vaga #" + v.id)
                .collect(Collectors.toList());
        actv.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, titulos));
        actv.setOnItemClickListener((parent, v, position, id) -> {
            VagaResponse selecionada = vagas.get(position);
            vagaIdSelecionada = selecionada.id;
            todas.clear();
            adapter.submitList(new ArrayList<>());
            atualizarEstadoVazio(new ArrayList<>());
            viewModel.carregarCandidaturas(selecionada.id);
        });
        if (vagas.size() == 1) {
            actv.setText(titulos.get(0), false);
            vagaIdSelecionada = vagas.get(0).id;
            viewModel.carregarCandidaturas(vagas.get(0).id);
        } else if (vagas.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhuma vaga cadastrada", Toast.LENGTH_SHORT).show();
        }
    }

    private void atualizarNomeVaga(View view, List<CandidaturaEmpresaResponse> lista) {
        if (!lista.isEmpty() && lista.get(0).tituloVaga != null) {
            TextView tvNomeVaga = view.findViewById(R.id.tv_nome_vaga);
            if (tvNomeVaga != null) tvNomeVaga.setText(lista.get(0).tituloVaga);
        }
    }

    private void setupFiltros(View view) {
        ChipGroup chips = view.findViewById(R.id.chip_group_status);
        if (chips == null) return;
        // O chip "Todas" (R.id.chip_todas) limpa o filtro; os demais carregam
        // o nome do status no tag, definido ao gerar os chips dinamicamente.
        chips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            View chip = group.findViewById(checkedIds.get(0));
            Object tag = chip != null ? chip.getTag() : null;
            filtroAtivo = tag instanceof String ? (String) tag : null;
            aplicarFiltro(filtroAtivo);
        });
    }

    /** Gera um chip por status retornado pela API, preservando o chip "Todas". */
    private void popularChipsStatus(View view, List<LookupItem> statusList) {
        ChipGroup chips = view.findViewById(R.id.chip_group_status);
        if (chips == null || statusList == null) return;

        // Remove chips dinâmicos de uma execução anterior, mantendo o "Todas".
        for (int i = chips.getChildCount() - 1; i >= 0; i--) {
            View child = chips.getChildAt(i);
            if (child.getId() != R.id.chip_todas) chips.removeViewAt(i);
        }

        for (LookupItem item : statusList) {
            String nome = item.getLabel();
            if (nome == null || nome.isEmpty()) continue;
            Chip chip = (Chip) getLayoutInflater()
                    .inflate(R.layout.item_chip_filtro_status, chips, false);
            chip.setId(View.generateViewId());
            chip.setText(nome);
            chip.setTag(nome);
            chips.addView(chip);
        }
    }

    private void aplicarFiltro(@Nullable String status) {
        List<CandidaturaEmpresaResponse> exibidas;
        if (status == null) {
            exibidas = new ArrayList<>(todas);
        } else {
            exibidas = todas.stream()
                    .filter(c -> status.equalsIgnoreCase(c.status))
                    .collect(Collectors.toList());
        }
        adapter.submitList(exibidas);
        atualizarEstadoVazio(exibidas);
    }

    private void atualizarEstadoVazio(List<?> lista) {
        if (tvNenhuma != null)
            tvNenhuma.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
