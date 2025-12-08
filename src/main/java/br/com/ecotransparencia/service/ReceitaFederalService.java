package br.com.ecotransparencia.service;

import br.com.ecotransparencia.dto.SituacaoCadastralDto;

/**
 * Interface para servico de consulta de situacao cadastral na Receita Federal.
 * Permite validar CPF e CNPJ.
 */
public interface ReceitaFederalService {

    /**
     * Consulta a situacao cadastral de um CNPJ na Receita Federal.
     *
     * @param cnpj CNPJ a ser consultado (14 digitos, apenas numeros)
     * @return situacao cadastral do CNPJ
     */
    SituacaoCadastralDto consultarCnpj(String cnpj);

    /**
     * Consulta a situacao cadastral de um CPF na Receita Federal.
     *
     * @param cpf CPF a ser consultado (11 digitos, apenas numeros)
     * @return situacao cadastral do CPF
     */
    SituacaoCadastralDto consultarCpf(String cpf);

    /**
     * Consulta a situacao cadastral de um documento (CPF ou CNPJ).
     * Detecta automaticamente o tipo pelo tamanho.
     *
     * @param documento CPF (11 digitos) ou CNPJ (14 digitos)
     * @return situacao cadastral do documento
     */
    default SituacaoCadastralDto consultar(String documento) {
        if (documento == null) {
            return SituacaoCadastralDto.invalido("INVALIDO", "Documento nao informado");
        }

        String docLimpo = documento.replaceAll("[^0-9]", "");

        if (docLimpo.isEmpty()) {
            return SituacaoCadastralDto.invalido("INVALIDO", "Documento nao informado");
        }

        return switch (docLimpo.length()) {
            case 11 -> consultarCpf(docLimpo);
            case 14 -> consultarCnpj(docLimpo);
            default -> SituacaoCadastralDto.invalido("INVALIDO", "Documento com formato invalido");
        };
    }
}
