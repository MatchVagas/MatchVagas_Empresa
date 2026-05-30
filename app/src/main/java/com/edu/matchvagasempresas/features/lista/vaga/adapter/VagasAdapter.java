package com.edu.matchvagasempresas.features.lista.vaga.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.matchvagasempresas.R;
import com.edu.matchvagasempresas.data.remote.dto.VagaResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VagasAdapter extends RecyclerView.Adapter<VagasAdapter.ViewHolder> {

    public interface OnVagaActionListener {
        void onVagaClick(long vagaId);
        void onCandidaturasClick(long vagaId);
        void onEditarClick(long vagaId);
        void onGerenciarClick(long vagaId);
    }

    private final Context context;
    private final List<VagaResponse> items = new ArrayList<>();
    private final OnVagaActionListener listener;

    public VagasAdapter(Context context, OnVagaActionListener listener) {
        this.context = context;
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitList(List<VagaResponse> newList) {
        List<VagaResponse> oldList = new ArrayList<>(items);
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
                VagaResponse ov = oldList.get(o);
                VagaResponse nv = newList.get(n);
                return Objects.equals(ov.titulo, nv.titulo)
                    && Objects.equals(ov.statusDescricao, nv.statusDescricao)
                    && Objects.equals(ov.salarioMinimo, nv.salarioMinimo)
                    && Objects.equals(ov.salarioMaximo, nv.salarioMaximo)
                    && Objects.equals(ov.dataExpiracao, nv.dataExpiracao)
                    && Objects.equals(ov.modalidadeDescricao, nv.modalidadeDescricao)
                    && Objects.equals(ov.areaAtuacao, nv.areaAtuacao);
            }
        });
        items.clear();
        items.addAll(newList);
        result.dispatchUpdatesTo(this);
    }

    @Override
    public long getItemId(int position) {
        VagaResponse v = items.get(position);
        return v.id != null ? v.id : position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vaga, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        VagaResponse v = items.get(position);
        h.tvTitulo.setText(v.titulo);
        h.tvArea.setText(v.areaAtuacao != null ? v.areaAtuacao : "");
        h.tvTipo.setText(v.tipoVagaDescricao != null ? v.tipoVagaDescricao : "");
        h.tvModalidade.setText(v.modalidadeDescricao != null ? v.modalidadeDescricao : "");

        String salario = "";
        if (v.salarioMinimo != null && v.salarioMaximo != null) {
            salario = String.format("R$ %.0f - R$ %.0f", v.salarioMinimo, v.salarioMaximo);
        }
        h.tvSalario.setText(salario);
        h.tvDataExpiracao.setText(v.dataExpiracao != null ? "Expira: " + formatDate(v.dataExpiracao) : "");
        h.tvStatus.setText(v.statusDescricao != null ? v.statusDescricao : "");
        h.tvCandidaturas.setText("candidaturas");

        if (v.idadeMinima != null || v.idadeMaxima != null) {
            String idade;
            if (v.idadeMinima != null && v.idadeMaxima != null)
                idade = "Idade: " + v.idadeMinima + " - " + v.idadeMaxima + " anos";
            else if (v.idadeMinima != null)
                idade = "Idade mínima: " + v.idadeMinima + " anos";
            else
                idade = "Idade máxima: " + v.idadeMaxima + " anos";
            h.tvIdade.setText(idade);
            h.tvIdade.setVisibility(View.VISIBLE);
        } else {
            h.tvIdade.setVisibility(View.GONE);
        }

        long id = v.id != null ? v.id : -1;
        h.itemView.setOnClickListener(view -> listener.onVagaClick(id));
        h.btnCandidaturas.setOnClickListener(view -> listener.onCandidaturasClick(id));
        h.btnEditar.setOnClickListener(view -> listener.onEditarClick(id));
        h.btnGerenciar.setOnClickListener(view -> listener.onGerenciarClick(id));
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) return isoDate;
        String[] parts = isoDate.substring(0, 10).split("-");
        if (parts.length == 3) return parts[2] + "/" + parts[1] + "/" + parts[0];
        return isoDate;
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvArea, tvTipo, tvModalidade, tvSalario;
        TextView tvIdade, tvCandidaturas, tvDataExpiracao, tvStatus;
        View btnCandidaturas, btnEditar, btnGerenciar;

        ViewHolder(View view) {
            super(view);
            tvTitulo        = view.findViewById(R.id.tv_titulo);
            tvArea          = view.findViewById(R.id.tv_area);
            tvTipo          = view.findViewById(R.id.tv_tipo);
            tvModalidade    = view.findViewById(R.id.tv_modalidade);
            tvSalario       = view.findViewById(R.id.tv_salario);
            tvIdade         = view.findViewById(R.id.tv_idade);
            tvCandidaturas  = view.findViewById(R.id.tv_candidaturas);
            tvDataExpiracao = view.findViewById(R.id.tv_data_expiracao);
            tvStatus        = view.findViewById(R.id.tv_status);
            btnCandidaturas = view.findViewById(R.id.btn_candidaturas);
            btnEditar       = view.findViewById(R.id.btn_editar);
            btnGerenciar    = view.findViewById(R.id.btn_gerenciar);
        }
    }
}
