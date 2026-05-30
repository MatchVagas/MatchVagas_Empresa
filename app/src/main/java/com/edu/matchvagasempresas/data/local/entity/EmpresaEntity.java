package com.edu.matchvagasempresas.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.edu.matchvagasempresas.data.remote.dto.EmpresaResponse;

@Entity(tableName = "empresa")
public class EmpresaEntity {

    @PrimaryKey
    public long id;
    public String cnpj;
    public String razaoSocial;
    public String nomeFantasia;
    public String descricao;
    public String porte;
    public String ramoAtuacao;
    public String site;
    public int totalVagasAtivas;
    public long usuarioGestorId;
    public String nomeGestor;
    public String status;
    public String logoUrl;
    public EmpresaResponse.Telefone telefone;

    public static EmpresaEntity fromResponse(EmpresaResponse r) {
        EmpresaEntity e = new EmpresaEntity();
        e.id               = r.id != null ? r.id : 0;
        e.cnpj             = r.cnpj;
        e.razaoSocial      = r.razaoSocial;
        e.nomeFantasia     = r.nomeFantasia;
        e.descricao        = r.descricao;
        e.porte            = r.porte;
        e.ramoAtuacao      = r.ramoAtuacao;
        e.site             = r.site;
        e.totalVagasAtivas = r.totalVagasAtivas != null ? r.totalVagasAtivas : 0;
        e.usuarioGestorId  = r.usuarioGestorId != null ? r.usuarioGestorId : 0;
        e.nomeGestor       = r.nomeGestor;
        e.status           = r.status;
        e.logoUrl          = r.logoUrl;
        e.telefone         = r.telefone;
        return e;
    }

    public EmpresaResponse toResponse() {
        EmpresaResponse r = new EmpresaResponse();
        r.id               = id;
        r.cnpj             = cnpj;
        r.razaoSocial      = razaoSocial;
        r.nomeFantasia     = nomeFantasia;
        r.descricao        = descricao;
        r.porte            = porte;
        r.ramoAtuacao      = ramoAtuacao;
        r.site             = site;
        r.totalVagasAtivas = totalVagasAtivas;
        r.usuarioGestorId  = usuarioGestorId;
        r.nomeGestor       = nomeGestor;
        r.status           = status;
        r.logoUrl          = logoUrl;
        r.telefone         = telefone;
        return r;
    }
}
