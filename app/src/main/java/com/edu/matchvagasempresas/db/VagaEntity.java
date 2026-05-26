package com.edu.matchvagasempresas.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.edu.matchvagasempresas.model.VagaResponse;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "vagas")
public class VagaEntity {

    @PrimaryKey
    public long id;
    public long empresaId;
    public String nomeFantasiaEmpresa;
    public String titulo;
    public String descricao;
    public String requisitos;
    public String beneficios;
    public long tipoVagaId;
    public String tipoVagaDescricao;
    public long modalidadeId;
    public String modalidadeDescricao;
    public double salarioMinimo;
    public double salarioMaximo;
    public String cargaHoraria;
    public int idadeMinima;
    public int idadeMaxima;
    public long escolaridadeId;
    public String escolaridadeNome;
    public String areaAtuacao;
    public String dataPublicacao;
    public String dataExpiracao;
    public long statusVagaId;
    public String statusDescricao;
    public int numeroVagas;
    public long cidadeId;
    public String nomeCidade;
    public String ufEstado;
    public long cachedAt;

    public static VagaEntity fromResponse(VagaResponse r) {
        VagaEntity e = new VagaEntity();
        e.id                  = r.id != null ? r.id : 0;
        e.empresaId           = r.empresaId != null ? r.empresaId : 0;
        e.nomeFantasiaEmpresa = r.nomeFantasiaEmpresa;
        e.titulo              = r.titulo;
        e.descricao           = r.descricao;
        e.requisitos          = r.requisitos;
        e.beneficios          = r.beneficios;
        e.tipoVagaId          = r.tipoVagaId != null ? r.tipoVagaId : 0;
        e.tipoVagaDescricao   = r.tipoVagaDescricao;
        e.modalidadeId        = r.modalidadeId != null ? r.modalidadeId : 0;
        e.modalidadeDescricao = r.modalidadeDescricao;
        e.salarioMinimo       = r.salarioMinimo != null ? r.salarioMinimo : 0;
        e.salarioMaximo       = r.salarioMaximo != null ? r.salarioMaximo : 0;
        e.cargaHoraria        = r.cargaHoraria;
        e.idadeMinima         = r.idadeMinima != null ? r.idadeMinima : 0;
        e.idadeMaxima         = r.idadeMaxima != null ? r.idadeMaxima : 0;
        e.escolaridadeId      = r.escolaridadeId != null ? r.escolaridadeId : 0;
        e.escolaridadeNome    = r.escolaridadeNome;
        e.areaAtuacao         = r.areaAtuacao;
        e.dataPublicacao      = r.dataPublicacao;
        e.dataExpiracao       = r.dataExpiracao;
        e.statusVagaId        = r.statusVagaId != null ? r.statusVagaId : 0;
        e.statusDescricao     = r.statusDescricao;
        e.numeroVagas         = r.numeroVagas != null ? r.numeroVagas : 0;
        e.cidadeId            = r.cidadeId != null ? r.cidadeId : 0;
        e.nomeCidade          = r.nomeCidade;
        e.ufEstado            = r.ufEstado;
        e.cachedAt            = System.currentTimeMillis();
        return e;
    }

    public VagaResponse toResponse() {
        VagaResponse r = new VagaResponse();
        r.id                  = id;
        r.empresaId           = empresaId;
        r.nomeFantasiaEmpresa = nomeFantasiaEmpresa;
        r.titulo              = titulo;
        r.descricao           = descricao;
        r.requisitos          = requisitos;
        r.beneficios          = beneficios;
        r.tipoVagaId          = tipoVagaId;
        r.tipoVagaDescricao   = tipoVagaDescricao;
        r.modalidadeId        = modalidadeId;
        r.modalidadeDescricao = modalidadeDescricao;
        r.salarioMinimo       = salarioMinimo;
        r.salarioMaximo       = salarioMaximo;
        r.cargaHoraria        = cargaHoraria;
        r.idadeMinima         = idadeMinima;
        r.idadeMaxima         = idadeMaxima;
        r.escolaridadeId      = escolaridadeId;
        r.escolaridadeNome    = escolaridadeNome;
        r.areaAtuacao         = areaAtuacao;
        r.dataPublicacao      = dataPublicacao;
        r.dataExpiracao       = dataExpiracao;
        r.statusVagaId        = statusVagaId;
        r.statusDescricao     = statusDescricao;
        r.numeroVagas         = numeroVagas;
        r.cidadeId            = cidadeId;
        r.nomeCidade          = nomeCidade;
        r.ufEstado            = ufEstado;
        return r;
    }

    public static List<VagaEntity> fromResponseList(List<VagaResponse> list) {
        List<VagaEntity> result = new ArrayList<>();
        if (list != null) for (VagaResponse r : list) result.add(fromResponse(r));
        return result;
    }

    public static List<VagaResponse> toResponseList(List<VagaEntity> list) {
        List<VagaResponse> result = new ArrayList<>();
        if (list != null) for (VagaEntity e : list) result.add(e.toResponse());
        return result;
    }
}
