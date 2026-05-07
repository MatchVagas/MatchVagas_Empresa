package com.edu.matchvagasempresas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.adapter.VagasAdapter;

public class ListaVagasFragment extends Fragment {

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
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.fab_nova_vaga).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_listaVagas_to_cadastroVaga));

        RecyclerView rv = view.findViewById(R.id.rv_vagas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new VagasAdapter(requireContext(), 6, new VagasAdapter.OnVagaActionListener() {
            @Override
            public void onVagaClick(int position) { }

            @Override
            public void onCandidaturasClick(int position) {
                Navigation.findNavController(view).navigate(R.id.action_listaVagas_to_listaCandidaturas);
            }

            @Override
            public void onEditarClick(int position) {
                Navigation.findNavController(view).navigate(R.id.action_listaVagas_to_editarVaga);
            }

            @Override
            public void onGerenciarClick(int position) {
                Navigation.findNavController(view).navigate(R.id.action_listaVagas_to_gerenciarVaga);
            }
        }));
    }
}
