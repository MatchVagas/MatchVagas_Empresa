package com.edu.matchvagasempresas.data.remote.dto;

public class LookupItem {
    public Long id;
    public String descricao;
    public String nome;
    public String ufEstado;

    public String getLabel() {
        if (descricao != null && !descricao.isEmpty()) return descricao;
        if (nome != null && ufEstado != null) return nome + " - " + ufEstado;
        return nome != null ? nome : "";
    }
}
