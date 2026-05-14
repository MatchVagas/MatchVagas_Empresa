package com.edu.matchvagasempresas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;

public class VagasAdapter extends RecyclerView.Adapter<VagasAdapter.ViewHolder> {

    public interface OnVagaActionListener {
        void onVagaClick(int position);
        void onCandidaturasClick(int position);
        void onEditarClick(int position);
        void onGerenciarClick(int position);
    }

    private static final String[][] DADOS = {
            {"Garoto de Programa", "ComPutaria", "Autonomo", "Presencial", "R$ 25,00 - R$ 500,00", "12", "30/06/2026", "Ativa"},
            {"Designer UX/UI", "Design", "PJ", "Híbrido", "R$ 2.500 - R$ 5.000", "8", "15/07/2026", "Ativa"},
            {"Analista de Dados", "TI", "CLT", "Presencial", "R$ 4.000 - R$ 7.000", "5", "20/07/2026", "Ativa"},
            {"Gerente de Projetos", "Gestão", "CLT", "Híbrido", "R$ 6.000 - R$ 10.000", "3", "10/07/2026", "Inativa"},
            {"Engenheiro de Software", "TI", "CLT", "Remoto", "R$ 8.000 - R$ 14.000", "20", "25/06/2026", "Ativa"},
            {"Suporte Técnico N2", "TI", "CLT", "Presencial", "R$ 1.800 - R$ 2.500", "15", "05/06/2026", "Expirada"},
    };

    private final Context context;
    private final int itemCount;
    private final OnVagaActionListener listener;

    public VagasAdapter(Context context, int itemCount, OnVagaActionListener listener) {
        this.context = context;
        this.itemCount = Math.min(itemCount, DADOS.length);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vaga, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        String[] d = DADOS[position];
        h.tvTitulo.setText(d[0]);
        h.tvArea.setText(d[1]);
        h.tvTipo.setText(d[2]);
        h.tvModalidade.setText(d[3]);
        h.tvSalario.setText(d[4]);
        h.tvCandidaturas.setText(d[5] + " candidaturas");
        h.tvDataExpiracao.setText("Expira: " + d[6]);
        h.tvStatus.setText(d[7]);

        h.itemView.setOnClickListener(v -> listener.onVagaClick(position));
        h.btnCandidaturas.setOnClickListener(v -> listener.onCandidaturasClick(position));
        h.btnEditar.setOnClickListener(v -> listener.onEditarClick(position));
        h.btnGerenciar.setOnClickListener(v -> listener.onGerenciarClick(position));
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvArea, tvTipo, tvModalidade, tvSalario;
        TextView tvCandidaturas, tvDataExpiracao, tvStatus;
        View btnCandidaturas, btnEditar, btnGerenciar;

        ViewHolder(View view) {
            super(view);
            tvTitulo = view.findViewById(R.id.tv_titulo);
            tvArea = view.findViewById(R.id.tv_area);
            tvTipo = view.findViewById(R.id.tv_tipo);
            tvModalidade = view.findViewById(R.id.tv_modalidade);
            tvSalario = view.findViewById(R.id.tv_salario);
            tvCandidaturas = view.findViewById(R.id.tv_candidaturas);
            tvDataExpiracao = view.findViewById(R.id.tv_data_expiracao);
            tvStatus = view.findViewById(R.id.tv_status);
            btnCandidaturas = view.findViewById(R.id.btn_candidaturas);
            btnEditar = view.findViewById(R.id.btn_editar);
            btnGerenciar = view.findViewById(R.id.btn_gerenciar);
        }
    }
}
