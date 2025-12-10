package br.com.ecotransparencia.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para resposta da CNPJA API Open - Consulta CNPJ.
 * Documentacao: https://cnpja.com/api/open
 *
 * Endpoint: GET https://open.cnpja.com/office/{cnpj}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CnpjaApiResponse {

    @JsonProperty("taxId")
    private String taxId;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("company")
    private CompanyInfo company;

    @JsonProperty("status")
    private StatusInfo status;

    @JsonProperty("address")
    private AddressInfo address;

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public CompanyInfo getCompany() {
        return company;
    }

    public void setCompany(CompanyInfo company) {
        this.company = company;
    }

    public StatusInfo getStatus() {
        return status;
    }

    public void setStatus(StatusInfo status) {
        this.status = status;
    }

    public AddressInfo getAddress() {
        return address;
    }

    public void setAddress(AddressInfo address) {
        this.address = address;
    }

    /**
     * Retorna a razao social da empresa.
     */
    public String getRazaoSocial() {
        return company != null ? company.getName() : null;
    }

    /**
     * Retorna o ID da situacao cadastral.
     */
    public Integer getSituacaoCadastralId() {
        return status != null ? status.getId() : null;
    }

    /**
     * Retorna a descricao da situacao cadastral (ex: "Ativa", "Baixada").
     */
    public String getDescricaoSituacaoCadastral() {
        return status != null ? status.getText() : null;
    }

    /**
     * Verifica se o CNPJ esta com situacao cadastral ATIVA.
     * ID 2 = Ativa na Receita Federal.
     */
    public boolean isAtiva() {
        return status != null && status.getId() != null && status.getId() == 2;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompanyInfo {
        @JsonProperty("name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusInfo {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("text")
        private String text;

        @JsonProperty("date")
        private String date;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddressInfo {
        @JsonProperty("state")
        private String state;

        @JsonProperty("city")
        private String city;

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }
}
