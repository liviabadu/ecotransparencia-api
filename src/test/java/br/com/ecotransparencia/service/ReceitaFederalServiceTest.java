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
            SituacaoCadastralDto resultado = service.consultarCnpj("12345678000199");

            assertTrue(resultado.isValido());
            assertEquals("ATIVA", resultado.getSituacao());
            assertNotNull(resultado.getDataConsulta());
            assertNotNull(resultado.getMensagem());
        }
    }

    @Nested
    @DisplayName("Consulta de CPF")
    class ConsultaCpfTests {

        @Test
        @DisplayName("Deve retornar CPF valido com situacao REGULAR")
        void shouldReturnValidCpf() {
            SituacaoCadastralDto resultado = service.consultarCpf("12345678901");

            assertTrue(resultado.isValido());
            assertEquals("REGULAR", resultado.getSituacao());
            assertNotNull(resultado.getDataConsulta());
            assertNotNull(resultado.getMensagem());
        }
    }

    @Nested
    @DisplayName("Consulta generica por documento")
    class ConsultaDocumentoTests {

        @Test
        @DisplayName("Deve detectar CPF pelo tamanho (11 digitos)")
        void shouldDetectCpfByLength() {
            SituacaoCadastralDto resultado = service.consultar("12345678901");

            assertTrue(resultado.isValido());
            assertEquals("REGULAR", resultado.getSituacao()); // CPF retorna REGULAR
        }

        @Test
        @DisplayName("Deve detectar CNPJ pelo tamanho (14 digitos)")
        void shouldDetectCnpjByLength() {
            SituacaoCadastralDto resultado = service.consultar("12345678000199");

            assertTrue(resultado.isValido());
            assertEquals("ATIVA", resultado.getSituacao()); // CNPJ retorna ATIVA
        }

        @Test
        @DisplayName("Deve remover caracteres nao numericos")
        void shouldRemoveNonNumericCharacters() {
            SituacaoCadastralDto resultado = service.consultar("123.456.789-01");

            assertTrue(resultado.isValido());
            assertEquals("REGULAR", resultado.getSituacao()); // CPF
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
            SituacaoCadastralDto resultado = service.consultar("12345"); // 5 digitos

            assertFalse(resultado.isValido());
            assertEquals("INVALIDO", resultado.getSituacao());
        }
    }
}
