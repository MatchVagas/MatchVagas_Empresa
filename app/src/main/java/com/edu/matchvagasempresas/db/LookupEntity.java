package com.edu.matchvagasempresas.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.edu.matchvagasempresas.model.LookupItem;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "lookups", primaryKeys = {"id", "tipo"})
public class LookupEntity {

    public static final String TIPO_PORTE       = "porte";
    public static final String TIPO_RAMO        = "ramo";
    public static final String TIPO_VAGA        = "tipoVaga";
    public static final String TIPO_MODALIDADE  = "modalidade";
    public static final String TIPO_ESCOLARIDADE = "escolaridade";
    public static final String TIPO_CIDADE      = "cidade";
    public static final String TIPO_STATUS_VAGA = "statusVaga";

    public long id;
    @NonNull
    public String tipo = "";
    public String descricao;
    public String nome;
    public String ufEstado;

    public static LookupEntity fromItem(LookupItem item, String tipo) {
        LookupEntity e = new LookupEntity();
        e.id       = item.id != null ? item.id : 0;
        e.tipo     = tipo;
        e.descricao = item.descricao;
        e.nome     = item.nome;
        e.ufEstado = item.ufEstado;
        return e;
    }

    public LookupItem toItem() {
        LookupItem item = new LookupItem();
        item.id       = id;
        item.descricao = descricao;
        item.nome     = nome;
        item.ufEstado = ufEstado;
        return item;
    }

    public static List<LookupEntity> fromList(List<LookupItem> items, String tipo) {
        List<LookupEntity> result = new ArrayList<>();
        if (items != null) for (LookupItem i : items) result.add(fromItem(i, tipo));
        return result;
    }

    public static List<LookupItem> toList(List<LookupEntity> entities) {
        List<LookupItem> result = new ArrayList<>();
        if (entities != null) for (LookupEntity e : entities) result.add(e.toItem());
        return result;
    }
}
