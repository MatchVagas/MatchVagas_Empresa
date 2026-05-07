package com.edu.matchvagasempresas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;

public class CandidaturasAdapter extends RecyclerView.Adapter<CandidaturasAdapter.ViewHolder> {

    public interface OnCandidaturaClickListener {
        void onCandidaturaClick(int position);
    }

    private static final String[][] DADOS = {
            {"Ana Beatriz Silva", "01/05/2026", "Pendente"},
            {"Carlos Eduardo Santos", "30/04/2026", "Em Análise"},
            {"Fernanda Lima Costa", "28/04/2026", "Aprovado"},
            {"Roberto Oliveira", "27/04/2026", "Reprovado"},
            {"Juliana Martins", "25/04/2026", "Pendente"},
            {"Marcos Pereira", "24/04/2026", "Em Análise"},
            {"Patricia Souza", "22/04/2026", "Pendente"},
            {"Thiago Fernandes", "20/04/2026", "Aprovado"},
    };

    private final Context context;
    private final OnCandidaturaClickListener listener;

    public CandidaturasAdapter(Context context, OnCandidaturaClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_candidatura, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        String[] d = DADOS[position];
        String[] partes = d[0].split(" ");
        String iniciais = partes.length >= 2
                ? "" + partes[0].charAt(0) + partes[1].charAt(0)
                : String.valueOf(d[0].charAt(0));

        h.tvIniciais.setText(iniciais.toUpperCase());
        h.tvNome.setText(d[0]);
        h.tvData.setText("Candidatou-se em " + d[1]);
        h.tvStatus.setText(d[2]);
        h.itemView.setOnClickListener(v -> listener.onCandidaturaClick(position));
    }

    @Override
    public int getItemCount() {
        return DADOS.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIniciais, tvNome, tvData, tvStatus;

        ViewHolder(View view) {
            super(view);
            tvIniciais = view.findViewById(R.id.tv_iniciais);
            tvNome = view.findViewById(R.id.tv_nome_candidato);
            tvData = view.findViewById(R.id.tv_data_candidatura);
            tvStatus = view.findViewById(R.id.tv_status);
        }
    }
}
