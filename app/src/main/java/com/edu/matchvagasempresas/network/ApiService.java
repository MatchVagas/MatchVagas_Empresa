package com.edu.matchvagasempresas.network;

import com.edu.matchvagasempresas.model.AuthResponse;
import com.edu.matchvagasempresas.model.CandidaturaEmpresaResponse;
import com.edu.matchvagasempresas.model.EmpresaRequest;
import com.edu.matchvagasempresas.model.EmpresaResponse;
import com.edu.matchvagasempresas.model.LookupItem;
import com.edu.matchvagasempresas.model.RegisterEmpresaRequest;
import com.edu.matchvagasempresas.model.VagaRequest;
import com.edu.matchvagasempresas.model.VagaResponse;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ── Autenticação ──────────────────────────────────────────────────────────

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body JsonObject body);

    @POST("api/auth/register")
    Call<JsonObject> register(@Body JsonObject body);

    @POST("api/auth/register-empresa")
    Call<AuthResponse> registerEmpresa(@Body RegisterEmpresaRequest request);

    // ── Empresa ───────────────────────────────────────────────────────────────

    @GET("api/empresas/minha-empresa")
    Call<EmpresaResponse> minhaEmpresa();

    @POST("api/empresas")
    Call<EmpresaResponse> criarEmpresa(@Body EmpresaRequest request);

    @PUT("api/empresas/{id}")
    Call<EmpresaResponse> atualizarEmpresa(@Path("id") Long id, @Body EmpresaRequest request);

    // ── Vagas ─────────────────────────────────────────────────────────────────

    @GET("api/vagas/minhas")
    Call<List<VagaResponse>> minhasVagas();

    @GET("api/vagas/{id}")
    Call<VagaResponse> buscarVaga(@Path("id") Long id);

    @POST("api/vagas")
    Call<VagaResponse> criarVaga(@Body VagaRequest request);

    @PUT("api/vagas/{id}")
    Call<VagaResponse> atualizarVaga(@Path("id") Long id, @Body VagaRequest request);

    @DELETE("api/vagas/{id}")
    Call<Void> deletarVaga(@Path("id") Long id);

    // ── Candidaturas (visão empresa) ──────────────────────────────────────────

    @GET("api/candidaturas/empresa")
    Call<List<CandidaturaEmpresaResponse>> candidaturasEmpresa();

    @GET("api/candidaturas/empresa/vaga/{vagaId}")
    Call<List<CandidaturaEmpresaResponse>> candidatosPorVaga(@Path("vagaId") Long vagaId);

    @GET("api/candidaturas/{id}/empresa")
    Call<CandidaturaEmpresaResponse> detalharCandidatura(@Path("id") Long id);

    @PATCH("api/candidaturas/{id}/empresa/status/{statusId}")
    Call<CandidaturaEmpresaResponse> atualizarStatusCandidatura(
            @Path("id") Long candidaturaId,
            @Path("statusId") Long statusId);

    // ── Lookups (públicos) ────────────────────────────────────────────────────

    @GET("api/lookup/vagas/tipos")
    Call<List<LookupItem>> listarTiposVaga();

    @GET("api/lookup/vagas/modalidades")
    Call<List<LookupItem>> listarModalidades();

    @GET("api/lookup/vagas/escolaridades")
    Call<List<LookupItem>> listarEscolaridades();

    @GET("api/lookup/vagas/portes")
    Call<List<LookupItem>> listarPortes();

    @GET("api/lookup/vagas/ramos")
    Call<List<LookupItem>> listarRamos();

    @GET("api/lookup/vagas/status")
    Call<List<LookupItem>> listarStatusVaga();

    @GET("api/localizacao/cidades")
    Call<List<LookupItem>> listarCidades();
}
