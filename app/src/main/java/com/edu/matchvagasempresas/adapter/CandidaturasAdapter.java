package com.edu.matchvagasempresas.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.model.CandidaturaEmpresaResponse;

import java.util.List;

public class CandidaturasAdapter extends RecyclerView.Adapter<CandidaturasAdapter.ViewHolder> {

    public interface OnCandidaturaClickListener {
        void onCandidaturaClick(long candidaturaId);
    }

    private final Context context;
    private final List<CandidaturaEmpresaResponse> candidaturas;
    private final OnCandidaturaClickListener listener;

    public CandidaturasAdapter(Context context, List<CandidaturaEmpresaResponse> candidaturas,
                               OnCandidaturaClickListener listener) {
        this.context = context;
        this.candidaturas = candidaturas;
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
        CandidaturaEmpresaResponse c = candidaturas.get(position);
        String nome = c.nomeCandidato != null ? c.nomeCandidato : "Candidato";
        String[] partes = nome.split(" ");
        String iniciais = partes.length >= 2
                ? "" + partes[0].charAt(0) + partes[1].charAt(0)
                : String.valueOf(nome.charAt(0));

        h.tvIniciais.setText(iniciais.toUpperCase());
        h.tvNome.setText(nome);
        h.tvData.setText("Candidatou-se em " + formatDate(c.dataCandidatura));
        String status = c.status != null ? c.status : "";
        h.tvStatus.setText(status);
        h.tvStatus.setBackgroundResource(badgeParaStatus(status));

        long id = c.id != null ? c.id : -1;
        h.itemView.setOnClickListener(view -> listener.onCandidaturaClick(id));
    }

    private int badgeParaStatus(String status) {
        if (status == null) return R.drawable.bg_badge_pendente;
        switch (status.toLowerCase()) {
            case "aprovado":   return R.drawable.bg_badge_aprovado;
            case "reprovado":  return R.drawable.bg_badge_reprovado;
            case "em análise": return R.drawable.bg_badge_analise;
            case "contratado": return R.drawable.bg_badge_ativa;
            default:           return R.drawable.bg_badge_pendente;
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) return "";
        String[] parts = isoDate.substring(0, 10).split("-");
        if (parts.length == 3) return parts[2] + "/" + parts[1] + "/" + parts[0];
        return isoDate;
    }

    @Override
    public int getItemCount() {
        return candidaturas != null ? candidaturas.size() : 0;
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
