package br.com.ecotransparencia.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de unidade para DocumentoUtil.
 */
class DocumentoUtilTest {

    @Nested
    @DisplayName("Validacao de CPF")
    class ValidarCpfTests {

        @Test
        @DisplayName("Deve validar CPF correto")
        void shouldValidateCorrectCpf() {
            // CPFs validos para teste (gerados com algoritmo correto)
            assertTrue(DocumentoUtil.validarCpf("52998224725")); // CPF valido
            assertTrue(DocumentoUtil.validarCpf("11144477735")); // CPF valido
            assertTrue(DocumentoUtil.validarCpf("12345678909")); // CPF valido
        }

        @Test
        @DisplayName("Deve rejeitar CPF com digito verificador incorreto")
        void shouldRejectCpfWithWrongCheckDigit() {
            assertFalse(DocumentoUtil.validarCpf("52998224720")); // ultimo digito errado
            assertFalse(DocumentoUtil.validarCpf("52998224715")); // penultimo digito errado
            assertFalse(DocumentoUtil.validarCpf("12345678900")); // digitos errados
        }

        @ParameterizedTest
        @ValueSource(strings = {"11111111111", "22222222222", "33333333333", "00000000000"})
        @DisplayName("Deve rejeitar CPF com todos os digitos iguais")
        void shouldRejectCpfWithAllSameDigits(String cpf) {
            assertFalse(DocumentoUtil.validarCpf(cpf));
        }

        @Test
        @DisplayName("Deve rejeitar CPF nulo")
        void shouldRejectNullCpf() {
            assertFalse(DocumentoUtil.validarCpf(null));
        }

        @Test
        @DisplayName("Deve rejeitar CPF com tamanho incorreto")
        void shouldRejectCpfWithWrongLength() {
            assertFalse(DocumentoUtil.validarCpf("1234567890"));   // 10 digitos
            assertFalse(DocumentoUtil.validarCpf("123456789012")); // 12 digitos
            assertFalse(DocumentoUtil.validarCpf(""));
        }

        @Test
        @DisplayName("Deve rejeitar CPF com caracteres nao numericos")
        void shouldRejectCpfWithNonNumericChars() {
            assertFalse(DocumentoUtil.validarCpf("529.982.247-25")); // formatado
            assertFalse(DocumentoUtil.validarCpf("5299822472A"));    // com letra
        }
    }

    @Nested
    @DisplayName("Validacao de CNPJ")
    class ValidarCnpjTests {

        @Test
        @DisplayName("Deve validar CNPJ correto")
        void shouldValidateCorrectCnpj() {
            // CNPJs validos para teste
            assertTrue(DocumentoUtil.validarCnpj("11222333000181")); // CNPJ valido
            assertTrue(DocumentoUtil.validarCnpj("11444777000161")); // CNPJ valido
        }

        @Test
        @DisplayName("Deve rejeitar CNPJ com digito verificador incorreto")
        void shouldRejectCnpjWithWrongCheckDigit() {
            assertFalse(DocumentoUtil.validarCnpj("11222333000180")); // ultimo digito errado
            assertFalse(DocumentoUtil.validarCnpj("11222333000191")); // penultimo digito errado
            assertFalse(DocumentoUtil.validarCnpj("12345678000100")); // digitos errados
        }

        @ParameterizedTest
        @ValueSource(strings = {"11111111111111", "22222222222222", "00000000000000"})
        @DisplayName("Deve rejeitar CNPJ com todos os digitos iguais")
        void shouldRejectCnpjWithAllSameDigits(String cnpj) {
            assertFalse(DocumentoUtil.validarCnpj(cnpj));
        }

        @Test
        @DisplayName("Deve rejeitar CNPJ nulo")
        void shouldRejectNullCnpj() {
            assertFalse(DocumentoUtil.validarCnpj(null));
        }

        @Test
        @DisplayName("Deve rejeitar CNPJ com tamanho incorreto")
        void shouldRejectCnpjWithWrongLength() {
            assertFalse(DocumentoUtil.validarCnpj("1122233300018"));   // 13 digitos
            assertFalse(DocumentoUtil.validarCnpj("112223330001811")); // 15 digitos
            assertFalse(DocumentoUtil.validarCnpj(""));
        }

        @Test
        @DisplayName("Deve rejeitar CNPJ com caracteres nao numericos")
        void shouldRejectCnpjWithNonNumericChars() {
            assertFalse(DocumentoUtil.validarCnpj("11.222.333/0001-81")); // formatado
            assertFalse(DocumentoUtil.validarCnpj("1122233300018A"));     // com letra
        }
    }

    @Nested
    @DisplayName("Limpeza de documento")
    class LimparTests {

        @Test
        @DisplayName("Deve remover pontos, tracos e barras")
        void shouldRemoveSpecialChars() {
            assertEquals("52998224725", DocumentoUtil.limpar("529.982.247-25"));
            assertEquals("11222333000181", DocumentoUtil.limpar("11.222.333/0001-81"));
        }

        @Test
        @DisplayName("Deve retornar null para documento nulo")
        void shouldReturnNullForNullDocument() {
            assertNull(DocumentoUtil.limpar(null));
        }

        @Test
        @DisplayName("Deve manter apenas numeros")
        void shouldKeepOnlyNumbers() {
            assertEquals("12345", DocumentoUtil.limpar("1a2b3c4d5e"));
        }
    }

    @Nested
    @DisplayName("Deteccao de tipo")
    class DetectarTipoTests {

        @Test
        @DisplayName("Deve detectar CPF pelo tamanho")
        void shouldDetectCpf() {
            assertEquals("cpf", DocumentoUtil.detectarTipo("52998224725"));
            assertEquals("cpf", DocumentoUtil.detectarTipo("529.982.247-25"));
        }

        @Test
        @DisplayName("Deve detectar CNPJ pelo tamanho")
        void shouldDetectCnpj() {
            assertEquals("cnpj", DocumentoUtil.detectarTipo("11222333000181"));
            assertEquals("cnpj", DocumentoUtil.detectarTipo("11.222.333/0001-81"));
        }

        @Test
        @DisplayName("Deve retornar unknown para tamanho invalido")
        void shouldReturnUnknownForInvalidLength() {
            assertEquals("unknown", DocumentoUtil.detectarTipo("12345"));
            assertEquals("unknown", DocumentoUtil.detectarTipo(null));
        }
    }
}
