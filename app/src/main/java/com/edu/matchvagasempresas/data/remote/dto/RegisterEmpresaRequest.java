package com.edu.matchvagasempresas.data.remote.dto;

public class RegisterEmpresaRequest {
    public String nome;
    public String email;
    public String senha;
    public String dataNascimento;
    public String cnpj;
    public String razaoSocial;
    public String nomeFantasia;
    public String descricao;
    public Long porteId;
    public Long ramoId;
    public String site;

    public RegisterEmpresaRequest(String nome, String email, String senha, String dataNascimento,
                                  String cnpj, String razaoSocial, String nomeFantasia,
                                  String descricao, Long porteId, Long ramoId, String site) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.dataNascimento = dataNascimento;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.descricao = descricao;
        this.porteId = porteId;
        this.ramoId = ramoId;
        this.site = site;
    }
}
