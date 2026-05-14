package com.edu.matchvagasempresas.model;

public class LookupItem {
    public Long id;
    public String descricao; // tipos, modalidades, portes, ramos, status
    public String nome;      // escolaridades, cidades
    public String ufEstado;  // cidades

    public String getLabel() {
        if (descricao != null && !descricao.isEmpty()) return descricao;
        if (nome != null && ufEstado != null) return nome + " - " + ufEstado;
        return nome != null ? nome : "";
    }
}
