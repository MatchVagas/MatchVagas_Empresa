package com.edu.matchvagasempresas.features.lista.candidatura.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CandidaturasAdapter extends RecyclerView.Adapter<CandidaturasAdapter.ViewHolder> {

    public interface OnCandidaturaClickListener {
        void onCandidaturaClick(long candidaturaId);
    }

    private final Context context;
    private final List<CandidaturaEmpresaResponse> items = new ArrayList<>();
    private final OnCandidaturaClickListener listener;

    public CandidaturasAdapter(Context context, OnCandidaturaClickListener listener) {
        this.context = context;
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitList(List<CandidaturaEmpresaResponse> newList) {
        List<CandidaturaEmpresaResponse> oldList = new ArrayList<>(items);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return oldList.size(); }
            @Override public int getNewListSize() { return newList.size(); }

            @Override
            public boolean areItemsTheSame(int o, int n) {
                Long oid = oldList.get(o).id;
                Long nid = newList.get(n).id;
                return oid != null && oid.equals(nid);
            }

            @Override
            public boolean areContentsTheSame(int o, int n) {
                CandidaturaEmpresaResponse oc = oldList.get(o);
                CandidaturaEmpresaResponse nc = newList.get(n);
                return Objects.equals(oc.status, nc.status)
                    && Objects.equals(oc.nomeCandidato, nc.nomeCandidato)
                    && Objects.equals(oc.tituloVaga, nc.tituloVaga)
                    && Objects.equals(oc.dataCandidatura, nc.dataCandidatura);
            }
        });
        items.clear();
        items.addAll(newList);
        result.dispatchUpdatesTo(this);
    }

    @Override
    public long getItemId(int position) {
        CandidaturaEmpresaResponse c = items.get(position);
        return c.id != null ? c.id : position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_candidatura, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CandidaturaEmpresaResponse c = items.get(position);

        String nome = c.nomeCandidato != null ? c.nomeCandidato : "Candidato";
        String[] partes = nome.split(" ");
        String iniciais = partes.length >= 2
                ? "" + partes[0].charAt(0) + partes[1].charAt(0)
                : String.valueOf(nome.charAt(0));

        h.tvIniciais.setText(iniciais.toUpperCase());
        h.tvNome.setText(nome);
        h.tvTituloVaga.setText(c.tituloVaga != null ? c.tituloVaga : "");
        h.tvData.setText(formatDate(c.dataCandidatura));

        String status = c.status != null ? c.status : "";
        h.tvStatus.setText(status);
        h.tvStatus.setBackgroundResource(badgeParaStatus(status));
        h.viewStatusStripe.setBackgroundColor(corParaStatus(status));

        long id = c.id != null ? c.id : -1;
        h.itemView.setOnClickListener(view -> listener.onCandidaturaClick(id));
    }

    private int badgeParaStatus(String status) {
        if (status == null) return R.drawable.bg_badge_pendente;
        switch (status.toLowerCase()) {
            case "aprovado":    return R.drawable.bg_badge_aprovado;
            case "reprovado":   return R.drawable.bg_badge_reprovado;
            case "em análise":  return R.drawable.bg_badge_analise;
            case "contratado":  return R.drawable.bg_badge_ativa;
            default:            return R.drawable.bg_badge_pendente;
        }
    }

    private int corParaStatus(String status) {
        if (status == null) return ContextCompat.getColor(context, R.color.status_pendente);
        switch (status.toLowerCase()) {
            case "aprovado":    return ContextCompat.getColor(context, R.color.status_aprovado);
            case "reprovado":   return ContextCompat.getColor(context, R.color.status_reprovado);
            case "em análise":  return ContextCompat.getColor(context, R.color.status_analise);
            case "contratado":  return ContextCompat.getColor(context, R.color.status_ativa);
            default:            return ContextCompat.getColor(context, R.color.status_pendente);
        }
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) return "";
        String[] parts = isoDate.substring(0, 10).split("-");
        if (parts.length == 3) return parts[2] + "/" + parts[1] + "/" + parts[0];
        return isoDate;
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewStatusStripe;
        TextView tvIniciais, tvNome, tvTituloVaga, tvData, tvStatus;

        ViewHolder(View view) {
            super(view);
            viewStatusStripe = view.findViewById(R.id.view_status_stripe);
            tvIniciais   = view.findViewById(R.id.tv_iniciais);
            tvNome       = view.findViewById(R.id.tv_nome_candidato);
            tvTituloVaga = view.findViewById(R.id.tv_titulo_vaga);
            tvData       = view.findViewById(R.id.tv_data_candidatura);
            tvStatus     = view.findViewById(R.id.tv_status);
        }
    }
}
