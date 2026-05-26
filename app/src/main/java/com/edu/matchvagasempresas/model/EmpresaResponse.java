package com.edu.matchvagasempresas.model;

public class EmpresaResponse {
    public Long id;
    public String cnpj;
    public String razaoSocial;
    public String nomeFantasia;
    public String descricao;
    public String porte;
    public String ramoAtuacao;
    public String site;
    public Integer totalVagasAtivas;
    public Long usuarioGestorId;
    public String nomeGestor;
    public String status;
    public String logoUrl;
    public Telefone telefone;

    public static class Telefone {
        public Long id;
        public String numero;
        public Long tipoTelefoneId;
        public String tipoTelefoneNome;
        public boolean wpp;
    }
}
