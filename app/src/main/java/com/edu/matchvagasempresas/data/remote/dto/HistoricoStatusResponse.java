package com.edu.matchvagasempresas.data.remote.dto;

public class HistoricoStatusResponse {
    public Long id;
    public String statusAnterior;   // nulo quando a candidatura ainda não tinha status
    public String statusNovo;
    public Long usuarioId;          // quem efetuou a mudança (recrutador/empresa)
    public String usuarioNome;
    public String dataHora;         // ISO LocalDateTime, ex: "2026-06-13T17:29:06"
}
