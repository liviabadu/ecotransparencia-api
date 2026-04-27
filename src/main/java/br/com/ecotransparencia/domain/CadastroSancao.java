package br.com.ecotransparencia.domain;

/**
 * Discriminador para registros da entidade {@link br.com.ecotransparencia.entity.SancaoAdmPublica}.
 *
 * CEIS e CNEP compartilham 95% do esquema de colunas; ficam na mesma tabela
 * com este enum como discriminador. CNEP adiciona o campo {@code valorMulta}.
 */
public enum CadastroSancao {
    CEIS,
    CNEP
}
