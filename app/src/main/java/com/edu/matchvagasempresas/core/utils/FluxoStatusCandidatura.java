package com.edu.matchvagasempresas.core.utils;

import java.text.Normalizer;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Espelha o fluxo de status da candidatura imposto pelo backend
 * (com.matchvagas.backend.entity.FluxoStatusCandidatura).
 *
 * <pre>
 *   EM_ANALISE ──► ENTREVISTA_AGENDADA ──► APROVADO
 *        │                  │
 *        └──────────────────┴────────────► REPROVADO
 * </pre>
 * APROVADO e REPROVADO são terminais. Status que não casam com nenhum estado
 * conhecido ficam fora da validação (transição liberada), preservando a
 * flexibilidade de status customizados.
 */
public enum FluxoStatusCandidatura {

    EM_ANALISE,
    ENTREVISTA_AGENDADA,
    APROVADO,
    REPROVADO;

    private static final Map<FluxoStatusCandidatura, Set<FluxoStatusCandidatura>> TRANSICOES = Map.of(
            EM_ANALISE,          EnumSet.of(ENTREVISTA_AGENDADA, APROVADO, REPROVADO),
            ENTREVISTA_AGENDADA, EnumSet.of(APROVADO, REPROVADO),
            APROVADO,            EnumSet.noneOf(FluxoStatusCandidatura.class),
            REPROVADO,           EnumSet.noneOf(FluxoStatusCandidatura.class)
    );

    public boolean ehTerminal() {
        return TRANSICOES.getOrDefault(this, Set.of()).isEmpty();
    }

    public boolean podeTransicionarPara(FluxoStatusCandidatura destino) {
        return TRANSICOES.getOrDefault(this, Set.of()).contains(destino);
    }

    /** Resolve o estado canônico a partir do nome livre (ex.: "Em Análise" → EM_ANALISE), ou null. */
    public static FluxoStatusCandidatura fromNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return null;
        String chave = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")   // remove acentos
                .trim()
                .toUpperCase()
                .replaceAll("\\s+", "_");
        try {
            return valueOf(chave);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Indica se a empresa pode mudar de {@code statusAtual} para {@code statusDestino}.
     * Reproduz a regra do backend: sem origem, ou origem/destino fora do fluxo conhecido,
     * a transição é liberada. Selecionar o mesmo status é permitido (o no-op é tratado à parte).
     */
    public static boolean transicaoPermitida(String statusAtual, String statusDestino) {
        if (statusAtual == null || statusAtual.trim().isEmpty()) return true;
        if (statusAtual.equalsIgnoreCase(statusDestino == null ? "" : statusDestino.trim())) return true;

        FluxoStatusCandidatura origem = fromNome(statusAtual);
        FluxoStatusCandidatura destino = fromNome(statusDestino);
        if (origem == null || destino == null) return true; // status customizado → sem regra
        return origem.podeTransicionarPara(destino);
    }
}
