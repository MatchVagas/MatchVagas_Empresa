package com.edu.matchvagasempresas.db;

import androidx.room.TypeConverter;

import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.model.ExperienciaResponse;
import com.edu.matchvagasempresas.model.FormacaoResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {

    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromExperiencias(List<ExperienciaResponse> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<ExperienciaResponse> toExperiencias(String json) {
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<ExperienciaResponse>>() {}.getType();
        List<ExperienciaResponse> result = gson.fromJson(json, type);
        return result != null ? result : new ArrayList<>();
    }

    @TypeConverter
    public static String fromFormacoes(List<FormacaoResponse> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<FormacaoResponse> toFormacoes(String json) {
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<FormacaoResponse>>() {}.getType();
        List<FormacaoResponse> result = gson.fromJson(json, type);
        return result != null ? result : new ArrayList<>();
    }

    @TypeConverter
    public static String fromStrings(List<String> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStrings(String json) {
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<String>>() {}.getType();
        List<String> result = gson.fromJson(json, type);
        return result != null ? result : new ArrayList<>();
    }

    @TypeConverter
    public static String fromTelefone(EmpresaResponse.Telefone telefone) {
        return telefone == null ? null : gson.toJson(telefone);
    }

    @TypeConverter
    public static EmpresaResponse.Telefone toTelefone(String json) {
        if (json == null) return null;
        return gson.fromJson(json, EmpresaResponse.Telefone.class);
    }
}
