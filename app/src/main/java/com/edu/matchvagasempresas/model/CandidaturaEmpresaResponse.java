package com.edu.matchvagasempresas.model;

import java.util.List;

public class CandidaturaEmpresaResponse {
    public Long id;
    public Long vagaId;
    public String tituloVaga;
    public String dataCandidatura;
    public String status;
    public Long candidatoId;
    public String nomeCandidato;
    public String objetivoProfissional;
    public String disponibilidade;
    public Double pretensaoSalarial;
    public String curriculoNomeArquivo;
    public String curriculoCaminho;
    public List<ExperienciaResponse> experiencias;
    public List<FormacaoResponse> formacoes;
    public List<String> telefones;
    public String endereco;
}
