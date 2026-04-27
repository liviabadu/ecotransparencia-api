package br.com.ecotransparencia.domain;

/**
 * Enum que representa as fontes de dados disponiveis para o calculo do Score ASG.
 *
 * <p><b>Distribuicao ESG (calibrada 2026-04-27):</b> os pesos somam 1.00 e
 * obedecem uma distribuicao 60% Environmental / 20% Social / 20% Governance.
 *
 * <pre>
 *   E (60%):  EMBARGO 0.25 + AUTO_INFRACAO 0.18 + ICMBIO_EMBARGO 0.10 + ICMBIO_AUTO 0.07
 *   S (20%):  MTE_TRABALHO_ESCRAVO 0.20
 *   G (20%):  CEIS 0.07 + CNEP 0.08 + CEPIM 0.05
 * </pre>
 *
 * <p>Justificativas:
 * <ul>
 *   <li>EMBARGO IBAMA tem cobertura nacional ampla e enquadramento direto -> maior peso individual.</li>
 *   <li>ICMBio cobre apenas Unidades de Conservacao federais (escopo restrito); pesos menores que IBAMA.</li>
 *   <li>MTE recebe peso unico alto (0.20) porque trabalho escravo e' a violacao mais grave do bloco social.</li>
 *   <li>CNEP > CEIS porque CNEP exige condenacao por ato lesivo (Lei 12.846); CEIS pode ser sancao administrativa rasa.</li>
 *   <li>CEPIM tem o menor peso (0.05) porque incide apenas sobre entidades sem fins lucrativos com convenios.</li>
 * </ul>
 */
public enum FonteDados {

    /**
     * Embargos ambientais do IBAMA (areas embargadas).
     * Peso: 0.25 (E - cobertura nacional ampla, enquadramento direto).
     */
    EMBARGO("Embargos IBAMA", "IBAMA", 0.25),

    /**
     * Autos de infracao do IBAMA.
     * Peso: 0.18 (E).
     */
    AUTO_INFRACAO("Autos de Infracao IBAMA", "IBAMA", 0.18),

    /**
     * Embargos do ICMBio (em Unidades de Conservacao federais).
     * Peso: 0.10 (E - escopo restrito a UCs).
     */
    ICMBIO_EMBARGO("Embargos ICMBio", "ICMBio", 0.10),

    /**
     * Autos de infracao do ICMBio.
     * Peso: 0.07 (E - escopo restrito a UCs).
     */
    ICMBIO_AUTO("Autos de Infracao ICMBio", "ICMBio", 0.07),

    /**
     * Lista Suja do MTE: empregadores com trabalho analogo ao escravo.
     * Peso: 0.20 (S - violacao mais grave do bloco social).
     */
    MTE_TRABALHO_ESCRAVO("MTE - Lista Suja Trabalho Escravo", "MTE", 0.20),

    /**
     * Cadastro Nacional de Empresas Punidas (CNEP - Portal da Transparencia).
     * Peso: 0.08 (G - condenacao por ato lesivo Lei 12.846).
     */
    CNEP("CNEP - Empresas Punidas", "Portal da Transparencia", 0.08),

    /**
     * Cadastro de Empresas Inidoneas e Suspensas (CEIS - Portal da Transparencia).
     * Peso: 0.07 (G).
     */
    CEIS("CEIS - Empresas Inidoneas/Suspensas", "Portal da Transparencia", 0.07),

    /**
     * Cadastro de Entidades Privadas Sem Fins Lucrativos Impedidas (CEPIM).
     * Peso: 0.05 (G - incide apenas sobre entidades com convenios).
     */
    CEPIM("CEPIM - Entidades Impedidas", "Portal da Transparencia", 0.05),

    /**
     * Reservado para futuras fontes de dados; peso 0.0 (nao entra no breakdown).
     */
    OUTRAS_FONTES("Outras Fontes", "DIVERSOS", 0.0);

    private final String descricao;
    private final String orgao;
    private final double peso;

    FonteDados(String descricao, String orgao, double peso) {
        this.descricao = descricao;
        this.orgao = orgao;
        this.peso = peso;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getOrgao() {
        return orgao;
    }

    public double getPeso() {
        return peso;
    }
}
