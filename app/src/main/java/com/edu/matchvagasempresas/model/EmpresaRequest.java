package com.edu.matchvagasempresas.model;

public class EmpresaRequest {
    public String cnpj;
    public String razaoSocial;
    public String nomeFantasia;
    public String descricao;
    public Long porteId;
    public Long ramoId;
    public String site;
    public Telefone telefone;

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

    public static class Telefone {
        public String numero;
        public Long tipoTelefoneId;
        public boolean wpp;

        public Telefone(String numero, Long tipoTelefoneId, boolean wpp) {
            this.numero = numero;
            this.tipoTelefoneId = tipoTelefoneId;
            this.wpp = wpp;
        }
    }
}
