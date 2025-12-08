package br.com.ecotransparencia.domain;

/**
 * Enum que representa as fontes de dados disponiveis para calculo do Score ASG.
 * Novas fontes podem ser adicionadas conforme necessario.
 */
public enum FonteDados {

    /**
     * Embargos ambientais do IBAMA (areas embargadas).
     * Peso: 0.50 (50% do score total)
     */
    EMBARGO("Embargos IBAMA", "IBAMA", 0.50),

    /**
     * Autos de infracao do IBAMA.
     * Peso: 0.35 (35% do score total)
     */
    AUTO_INFRACAO("Autos de Infracao IBAMA", "IBAMA", 0.35),

    /**
     * Reservado para futuras fontes de dados.
     * Peso: 0.15 (15% do score total)
     */
    OUTRAS_FONTES("Outras Fontes", "DIVERSOS", 0.15);

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
