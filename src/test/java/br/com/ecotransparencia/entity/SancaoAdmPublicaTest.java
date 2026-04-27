package br.com.ecotransparencia.entity;

import br.com.ecotransparencia.domain.CadastroSancao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de unidade da entity SancaoAdmPublica.
 *
 * Valida que campos de CEIS e CNEP (incluindo o exclusivo {@code valorMulta}
 * de CNEP) sao mapeados corretamente.
 */
class SancaoAdmPublicaTest {

    @Test
    @DisplayName("Cria registro CEIS sem valor de multa")
    void shouldCreateCeisRecordWithoutFineValue() {
        SancaoAdmPublica s = new SancaoAdmPublica();
        s.setCadastro(CadastroSancao.CEIS);
        s.setCpfCnpj("17344993000111");
        s.setNomeSancionado("KM INDUSTRIA");
        s.setDataInicioSancao(LocalDate.of(2024, 4, 26));

        assertEquals(CadastroSancao.CEIS, s.getCadastro());
        assertEquals("17344993000111", s.getCpfCnpj());
        assertNull(s.getValorMulta());
    }

    @Test
    @DisplayName("Cria registro CNEP com valor de multa em pt-BR convertido")
    void shouldCreateCnepRecordWithFineValue() {
        SancaoAdmPublica s = new SancaoAdmPublica();
        s.setCadastro(CadastroSancao.CNEP);
        s.setCpfCnpj("55015050968");
        s.setValorMulta(new BigDecimal("517662.90"));

        assertEquals(CadastroSancao.CNEP, s.getCadastro());
        assertEquals(new BigDecimal("517662.90"), s.getValorMulta());
    }

    @Test
    @DisplayName("Aceita observacoes longas (campo TEXT)")
    void shouldAcceptLongObservacoes() {
        SancaoAdmPublica s = new SancaoAdmPublica();
        String longText = "obs ".repeat(500);
        s.setObservacoes(longText);
        s.setFundamentacaoLegal(longText);

        assertEquals(longText, s.getObservacoes());
        assertEquals(longText, s.getFundamentacaoLegal());
    }
}
