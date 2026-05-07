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

import com.edu.matchvagasempresas.adapter.CandidaturasAdapter;

public class ListaCandidaturasFragment extends Fragment {

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
        toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        RecyclerView rv = view.findViewById(R.id.rv_candidaturas);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new CandidaturasAdapter(requireContext(), position ->
                Navigation.findNavController(view).navigate(R.id.action_listaCandidaturas_to_detalhes)));
    }
}
