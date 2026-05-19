package com.edu.matchvagasempresas.model;

public class VagaRequest {
    public Long empresaId;
    public String titulo;
    public String descricao;
    public String requisitos;
    public Long tipoVagaId;
    public Long modalidadeId;
    public Double salarioMinimo;
    public Double salarioMaximo;
    public String beneficios;
    public String cargaHoraria;
    public Integer idadeMinima;
    public Integer idadeMaxima;
    public Long escolaridadeId;
    public String areaAtuacao;
    public String dataExpiracao; // ISO datetime: "2026-06-30T23:59:59"
    public Long statusVagaId;
    public Integer numeroVagas;
    public Long cidadeId;
}
