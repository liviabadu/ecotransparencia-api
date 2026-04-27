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
     * Cadastro de Empresas Inidoneas e Suspensas (CEIS - Portal da Transparencia).
     * Peso: 0.10 (provisorio, ajustar com input do produto - Fase B)
     */
    CEIS("CEIS - Empresas Inidoneas/Suspensas", "Portal da Transparencia", 0.10),

    /**
     * Cadastro Nacional de Empresas Punidas (CNEP - Portal da Transparencia).
     * Peso: 0.10 (provisorio, ajustar com input do produto - Fase B)
     */
    CNEP("CNEP - Empresas Punidas", "Portal da Transparencia", 0.10),

    /**
     * Cadastro de Entidades Privadas Sem Fins Lucrativos Impedidas (CEPIM).
     * Peso: 0.05 (provisorio, ajustar com input do produto - Fase B)
     */
    CEPIM("CEPIM - Entidades Impedidas", "Portal da Transparencia", 0.05),

    /**
     * Lista Suja do MTE: empregadores com trabalho analogo ao escravo.
     * Peso: 0.10 (provisorio, ajustar com input do produto - Fase B)
     */
    MTE_TRABALHO_ESCRAVO("MTE - Lista Suja Trabalho Escravo", "MTE", 0.10),

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
