package com.edu.matchvagasempresas.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.edu.matchvagasempresas.data.remote.dto.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.data.remote.dto.ExperienciaResponse;
import com.edu.matchvagasempresas.data.remote.dto.FormacaoResponse;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "candidaturas")
public class CandidaturaEntity {

    @PrimaryKey
    public long id;
    public long vagaId;
    public String tituloVaga;
    public String dataCandidatura;
    public String status;
    public long candidatoId;
    public String nomeCandidato;
    public String objetivoProfissional;
    public String disponibilidade;
    public double pretensaoSalarial;
    public String curriculoNomeArquivo;
    public String curriculoCaminho;
    public List<ExperienciaResponse> experiencias;
    public List<FormacaoResponse> formacoes;
    public List<String> telefones;
    public String endereco;
    public long cachedAt;

    public static CandidaturaEntity fromResponse(CandidaturaEmpresaResponse r) {
        CandidaturaEntity e = new CandidaturaEntity();
        e.id                   = r.id != null ? r.id : 0;
        e.vagaId               = r.vagaId != null ? r.vagaId : 0;
        e.tituloVaga           = r.tituloVaga;
        e.dataCandidatura      = r.dataCandidatura;
        e.status               = r.status;
        e.candidatoId          = r.candidatoId != null ? r.candidatoId : 0;
        e.nomeCandidato        = r.nomeCandidato;
        e.objetivoProfissional = r.objetivoProfissional;
        e.disponibilidade      = r.disponibilidade;
        e.pretensaoSalarial    = r.pretensaoSalarial != null ? r.pretensaoSalarial : 0;
        e.curriculoNomeArquivo = r.curriculoNomeArquivo;
        e.curriculoCaminho     = r.curriculoCaminho;
        e.experiencias         = r.experiencias;
        e.formacoes            = r.formacoes;
        e.telefones            = r.telefones;
        e.endereco             = r.endereco;
        e.cachedAt             = System.currentTimeMillis();
        return e;
    }

    public CandidaturaEmpresaResponse toResponse() {
        CandidaturaEmpresaResponse r = new CandidaturaEmpresaResponse();
        r.id                   = id;
        r.vagaId               = vagaId;
        r.tituloVaga           = tituloVaga;
        r.dataCandidatura      = dataCandidatura;
        r.status               = status;
        r.candidatoId          = candidatoId;
        r.nomeCandidato        = nomeCandidato;
        r.objetivoProfissional = objetivoProfissional;
        r.disponibilidade      = disponibilidade;
        r.pretensaoSalarial    = pretensaoSalarial;
        r.curriculoNomeArquivo = curriculoNomeArquivo;
        r.curriculoCaminho     = curriculoCaminho;
        r.experiencias         = experiencias != null ? experiencias : new ArrayList<>();
        r.formacoes            = formacoes != null ? formacoes : new ArrayList<>();
        r.telefones            = telefones != null ? telefones : new ArrayList<>();
        r.endereco             = endereco;
        return r;
    }

    public static List<CandidaturaEntity> fromResponseList(List<CandidaturaEmpresaResponse> list) {
        List<CandidaturaEntity> result = new ArrayList<>();
        if (list != null) for (CandidaturaEmpresaResponse r : list) result.add(fromResponse(r));
        return result;
    }

    public static List<CandidaturaEmpresaResponse> toResponseList(List<CandidaturaEntity> list) {
        List<CandidaturaEmpresaResponse> result = new ArrayList<>();
        if (list != null) for (CandidaturaEntity e : list) result.add(e.toResponse());
        return result;
    }
}
