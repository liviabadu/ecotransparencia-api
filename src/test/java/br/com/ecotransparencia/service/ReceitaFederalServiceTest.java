package br.com.ecotransparencia.service;

import br.com.ecotransparencia.dto.SituacaoCadastralDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de unidade para ReceitaFederalService.
 */
class ReceitaFederalServiceTest {

    // CPFs e CNPJs validos para teste (com digitos verificadores corretos)
    private static final String CPF_VALIDO = "52998224725";
    private static final String CPF_VALIDO_FORMATADO = "529.982.247-25";
    private static final String CPF_INVALIDO = "12345678901";

    private static final String CNPJ_VALIDO = "11222333000181";
    private static final String CNPJ_VALIDO_FORMATADO = "11.222.333/0001-81";
    private static final String CNPJ_INVALIDO = "12345678000199";

    private ReceitaFederalService service;

    @BeforeEach
    void setUp() {
        service = new ReceitaFederalServiceStub();
    }

    @Nested
    @DisplayName("Consulta de CNPJ")
    class ConsultaCnpjTests {

        @Test
        @DisplayName("Deve retornar CNPJ valido com situacao ATIVA")
        void shouldReturnValidCnpj() {
            SituacaoCadastralDto resultado = service.consultarCnpj(CNPJ_VALIDO);

            assertTrue(resultado.isValido());
            assertEquals("ATIVA", resultado.getSituacao());
            assertNotNull(resultado.getDataConsulta());
            assertNotNull(resultado.getMensagem());
        }

        @Test
        @DisplayName("Deve retornar invalido para CNPJ com digitos verificadores incorretos")
        void shouldReturnInvalidForWrongCheckDigits() {
            SituacaoCadastralDto resultado = service.consultarCnpj(CNPJ_INVALIDO);

            assertFalse(resultado.isValido());
            assertEquals("INVALIDO", resultado.getSituacao());
            assertTrue(resultado.getMensagem().contains("digitos verificadores"));
        }
    }

    @Nested
    @DisplayName("Consulta de CPF")
    class ConsultaCpfTests {

        @Test
        @DisplayName("Deve retornar CPF valido com situacao REGULAR")
        void shouldReturnValidCpf() {
            SituacaoCadastralDto resultado = service.consultarCpf(CPF_VALIDO);

            assertTrue(resultado.isValido());
            assertEquals("REGULAR", resultado.getSituacao());
            assertNotNull(resultado.getDataConsulta());
            assertNotNull(resultado.getMensagem());
        }

        @Test
        @DisplayName("Deve retornar invalido para CPF com digitos verificadores incorretos")
        void shouldReturnInvalidForWrongCheckDigits() {
            SituacaoCadastralDto resultado = service.consultarCpf(CPF_INVALIDO);

            assertFalse(resultado.isValido());
            assertEquals("INVALIDO", resultado.getSituacao());
            assertTrue(resultado.getMensagem().contains("digitos verificadores"));
        }
    }

    @Nested
    @DisplayName("Consulta generica por documento")
    class ConsultaDocumentoTests {

        @Test
        @DisplayName("Deve detectar CPF pelo tamanho (11 digitos)")
        void shouldDetectCpfByLength() {
            SituacaoCadastralDto resultado = service.consultar(CPF_VALIDO);

            assertTrue(resultado.isValido());
            assertEquals("REGULAR", resultado.getSituacao());
        }

        @Test
        @DisplayName("Deve detectar CNPJ pelo tamanho (14 digitos)")
        void shouldDetectCnpjByLength() {
            SituacaoCadastralDto resultado = service.consultar(CNPJ_VALIDO);

            assertTrue(resultado.isValido());
            assertEquals("ATIVA", resultado.getSituacao());
        }

        @Test
        @DisplayName("Deve remover caracteres nao numericos e validar CPF")
        void shouldRemoveNonNumericCharactersAndValidateCpf() {
            SituacaoCadastralDto resultado = service.consultar(CPF_VALIDO_FORMATADO);

            assertTrue(resultado.isValido());
            assertEquals("REGULAR", resultado.getSituacao());
        }

        @Test
        @DisplayName("Deve remover caracteres nao numericos e validar CNPJ")
        void shouldRemoveNonNumericCharactersAndValidateCnpj() {
            SituacaoCadastralDto resultado = service.consultar(CNPJ_VALIDO_FORMATADO);

            assertTrue(resultado.isValido());
            assertEquals("ATIVA", resultado.getSituacao());
        }

        @Test
        @DisplayName("Deve retornar invalido para documento nulo")
        void shouldReturnInvalidForNullDocument() {
            SituacaoCadastralDto resultado = service.consultar(null);

            assertFalse(resultado.isValido());
            assertEquals("INVALIDO", resultado.getSituacao());
        }

        @Test
        @DisplayName("Deve retornar invalido para documento com tamanho incorreto")
        void shouldReturnInvalidForWrongLength() {
            SituacaoCadastralDto resultado = service.consultar("12345");

            assertFalse(resultado.isValido());
            assertEquals("INVALIDO", resultado.getSituacao());
        }

        @Test
        @DisplayName("Deve retornar invalido para CPF com formato incorreto")
        void shouldReturnInvalidForInvalidCpfFormat() {
            SituacaoCadastralDto resultado = service.consultar(CPF_INVALIDO);

            assertFalse(resultado.isValido());
            assertEquals("INVALIDO", resultado.getSituacao());
        }

        @Test
        @DisplayName("Deve retornar invalido para CNPJ com formato incorreto")
        void shouldReturnInvalidForInvalidCnpjFormat() {
            SituacaoCadastralDto resultado = service.consultar(CNPJ_INVALIDO);

            assertFalse(resultado.isValido());
            assertEquals("INVALIDO", resultado.getSituacao());
        }
    }
}
