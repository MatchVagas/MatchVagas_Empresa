package com.edu.matchvagasempresas.feature.lista.candidatura;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.adapter.CandidaturasAdapter;
import com.edu.matchvagasempresas.model.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.model.VagaResponse;
import com.edu.matchvagasempresas.network.ApiClient;
import com.edu.matchvagasempresas.network.DataCache;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListaCandidaturasFragment extends Fragment {

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

        vagaIdFixo = getArguments() != null ? getArguments().getLong("vagaId", -1) : -1;

        if (vagaIdFixo > 0) {
            vagaIdSelecionada = vagaIdFixo;
            mostrarVagaFixa(view);
            carregarCandidaturas(view, vagaIdFixo);
        } else {
            mostrarSeletorVaga(view);
            carregarVagas(view);
        }
    }

    private void atualizar() {
        if (vagaIdSelecionada > 0) {
            refreshCandidaturas(vagaIdSelecionada);
        } else {
            swipeRefresh.setRefreshing(false);
        }
    }

    // ── Modo vaga fixa ───────────────────────────────────────────────────────

    private void mostrarVagaFixa(View view) {
        view.findViewById(R.id.layout_vaga_fixa).setVisibility(View.VISIBLE);
        view.findViewById(R.id.til_vaga_selector).setVisibility(View.GONE);
    }

    /** Carga inicial: cache primeiro, depois dado fresco. */
    private void carregarCandidaturas(View view, long vagaId) {
        DataCache.get().loadCandidaturas(requireContext(), vagaId,
                cached -> {
                    if (cached != null && isAdded()) {
                        todas.clear();
                        todas.addAll(cached);
                        aplicarFiltro(filtroAtivo);
                        atualizarNomeVaga(view, cached);
                    }
                },
                fresh -> {
                    if (!isAdded()) return;
                    todas.clear();
                    todas.addAll(fresh);
                    aplicarFiltro(filtroAtivo);
                    atualizarNomeVaga(view, fresh);
                }
        );
    }

    /** Refresh explícito: vai direto à API, sem cache, para poder parar o spinner em qualquer caso. */
    private void refreshCandidaturas(long vagaId) {
        ApiClient.getService(requireContext()).candidatosPorVaga(vagaId)
                .enqueue(new Callback<List<CandidaturaEmpresaResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CandidaturaEmpresaResponse>> c,
                                           @NonNull Response<List<CandidaturaEmpresaResponse>> r) {
                        if (!isAdded()) return;
                        swipeRefresh.setRefreshing(false);
                        if (r.isSuccessful() && r.body() != null) {
                            DataCache.get().saveCandidaturas(requireContext(), vagaId, r.body());
                            todas.clear();
                            todas.addAll(r.body());
                            aplicarFiltro(filtroAtivo);
                        } else {
                            Toast.makeText(requireContext(), "Erro ao atualizar candidaturas", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<CandidaturaEmpresaResponse>> c, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Erro de conexão", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void atualizarNomeVaga(View view, List<CandidaturaEmpresaResponse> lista) {
        if (!lista.isEmpty() && lista.get(0).tituloVaga != null) {
            TextView tvNomeVaga = view.findViewById(R.id.tv_nome_vaga);
            if (tvNomeVaga != null) tvNomeVaga.setText(lista.get(0).tituloVaga);
        }
    }

    // ── Modo seletor ─────────────────────────────────────────────────────────

    private void mostrarSeletorVaga(View view) {
        view.findViewById(R.id.til_vaga_selector).setVisibility(View.VISIBLE);
        view.findViewById(R.id.layout_vaga_fixa).setVisibility(View.GONE);
    }

    private void carregarVagas(View view) {
        ApiClient.getService(requireContext()).minhasVagas()
                .enqueue(new Callback<List<VagaResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<VagaResponse>> c,
                                           @NonNull Response<List<VagaResponse>> r) {
                        if (!isAdded()) return;
                        if (r.isSuccessful() && r.body() != null) {
                            vagas.clear();
                            vagas.addAll(r.body());
                            bindSeletorVaga(view);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<VagaResponse>> c, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Erro ao carregar vagas", Toast.LENGTH_SHORT).show();
                    }
                });
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
            carregarCandidaturas(view, selecionada.id);
        });

        if (vagas.size() == 1) {
            actv.setText(titulos.get(0), false);
            vagaIdSelecionada = vagas.get(0).id;
            carregarCandidaturas(view, vagas.get(0).id);
        } else if (vagas.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhuma vaga cadastrada", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Filtros por status ───────────────────────────────────────────────────

    private void setupFiltros(View view) {
        ChipGroup chips = view.findViewById(R.id.chip_group_status);
        if (chips == null) return;
        chips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_pendente)        filtroAtivo = "Aguardando";
            else if (id == R.id.chip_em_analise) filtroAtivo = "Em análise";
            else if (id == R.id.chip_aprovado)   filtroAtivo = "Aprovado";
            else if (id == R.id.chip_reprovado)  filtroAtivo = "Reprovado";
            else                                 filtroAtivo = null;
            aplicarFiltro(filtroAtivo);
        });
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
