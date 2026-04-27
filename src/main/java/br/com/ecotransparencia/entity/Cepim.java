package br.com.ecotransparencia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * CEPIM (Cadastro de Entidades Privadas Sem Fins Lucrativos Impedidas).
 *
 * 5 colunas no CSV; sem PK natural -> usa BIGSERIAL e indexa cnpjEntidade.
 */
@Entity
@Table(
    name = "cepim",
    indexes = {
        @Index(name = "idx_cepim_cnpj_entidade", columnList = "cnpj_entidade")
    }
)
public class Cepim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "cnpj_entidade")
    private String cnpjEntidade;

    @Column(name = "nome_entidade")
    private String nomeEntidade;

    @Column(name = "numero_convenio")
    private String numeroConvenio;

    @Column(name = "orgao_concedente")
    private String orgaoConcedente;

    @Column(name = "motivo_impedimento", columnDefinition = "TEXT")
    private String motivoImpedimento;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCnpjEntidade() {
        return cnpjEntidade;
    }

    public void setCnpjEntidade(String cnpjEntidade) {
        this.cnpjEntidade = cnpjEntidade;
    }

    public String getNomeEntidade() {
        return nomeEntidade;
    }

    public void setNomeEntidade(String nomeEntidade) {
        this.nomeEntidade = nomeEntidade;
    }

    public String getNumeroConvenio() {
        return numeroConvenio;
    }

    public void setNumeroConvenio(String numeroConvenio) {
        this.numeroConvenio = numeroConvenio;
    }

    public String getOrgaoConcedente() {
        return orgaoConcedente;
    }

    public void setOrgaoConcedente(String orgaoConcedente) {
        this.orgaoConcedente = orgaoConcedente;
    }

    public String getMotivoImpedimento() {
        return motivoImpedimento;
    }

    public void setMotivoImpedimento(String motivoImpedimento) {
        this.motivoImpedimento = motivoImpedimento;
    }
}
