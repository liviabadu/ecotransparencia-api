package br.com.ecotransparencia.util;

/**
 * Utilitario para validacao de documentos (CPF e CNPJ).
 * Implementa validacao dos digitos verificadores conforme algoritmo oficial.
 */
public final class DocumentoUtil {

    private DocumentoUtil() {
        // Classe utilitaria
    }

    /**
     * Valida um CPF verificando os digitos verificadores.
     *
     * @param cpf CPF com 11 digitos (apenas numeros)
     * @return true se o CPF e valido
     */
    public static boolean validarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }

        // Verifica se todos sao digitos
        if (!cpf.matches("\\d{11}")) {
            return false;
        }

        // Rejeita CPFs com todos os digitos iguais (ex: 111.111.111-11)
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        // Calcula primeiro digito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) {
            primeiroDigito = 0;
        }

        // Verifica primeiro digito
        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        // Calcula segundo digito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) {
            segundoDigito = 0;
        }

        // Verifica segundo digito
        return segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    /**
     * Valida um CNPJ verificando os digitos verificadores.
     *
     * @param cnpj CNPJ com 14 digitos (apenas numeros)
     * @return true se o CNPJ e valido
     */
    public static boolean validarCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return false;
        }

        // Verifica se todos sao digitos
        if (!cnpj.matches("\\d{14}")) {
            return false;
        }

        // Rejeita CNPJs com todos os digitos iguais
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        // Pesos para calculo do primeiro digito
        int[] pesosPrimeiro = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * pesosPrimeiro[i];
        }
        int primeiroDigito = soma % 11;
        primeiroDigito = primeiroDigito < 2 ? 0 : 11 - primeiroDigito;

        // Verifica primeiro digito
        if (primeiroDigito != Character.getNumericValue(cnpj.charAt(12))) {
            return false;
        }

        // Pesos para calculo do segundo digito
        int[] pesosSegundo = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * pesosSegundo[i];
        }
        int segundoDigito = soma % 11;
        segundoDigito = segundoDigito < 2 ? 0 : 11 - segundoDigito;

        // Verifica segundo digito
        return segundoDigito == Character.getNumericValue(cnpj.charAt(13));
    }

    /**
     * Remove caracteres nao numericos de um documento.
     *
     * @param documento CPF ou CNPJ com ou sem formatacao
     * @return apenas os digitos
     */
    public static String limpar(String documento) {
        if (documento == null) {
            return null;
        }
        return documento.replaceAll("[^0-9]", "");
    }

    /**
     * Detecta o tipo do documento pelo tamanho.
     *
     * @param documento CPF (11 digitos) ou CNPJ (14 digitos)
     * @return "cpf", "cnpj" ou "unknown"
     */
    public static String detectarTipo(String documento) {
        if (documento == null) {
            return "unknown";
        }
        String limpo = limpar(documento);
        return switch (limpo.length()) {
            case 11 -> "cpf";
            case 14 -> "cnpj";
            default -> "unknown";
        };
    }
}
