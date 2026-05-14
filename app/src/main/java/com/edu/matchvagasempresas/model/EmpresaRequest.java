package com.edu.matchvagasempresas.model;

public class EmpresaRequest {
    public String cnpj;
    public String razaoSocial;
    public String nomeFantasia;
    public String descricao;
    public Long porteId;
    public Long ramoId;
    public String site;

    public EmpresaRequest(String cnpj, String razaoSocial, String nomeFantasia,
                          String descricao, Long porteId, Long ramoId, String site) {
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.descricao = descricao;
        this.porteId = porteId;
        this.ramoId = ramoId;
        this.site = site;
    }
}
