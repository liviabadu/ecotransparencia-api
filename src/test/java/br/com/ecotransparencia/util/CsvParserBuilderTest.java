package br.com.ecotransparencia.util;

import com.opencsv.CSVReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para CsvParserBuilder.
 *
 * Cobre os charsets usados pelas fontes de dados de Fase B:
 * - UTF-8 (IBAMA)
 * - ISO-8859-1 (CEIS, CNEP, CEPIM)
 * - Windows-1252 / Cp1252 (MTE)
 *
 * E variantes de configuracao de quote:
 * - Quote padrao (")
 * - Sem quote processing (NULL char)
 */
class CsvParserBuilderTest {

    private static final Path FIXTURE_ISO = Paths.get("src/test/resources/fixtures/test-iso-8859-1.csv");
    private static final Path FIXTURE_CP1252 = Paths.get("src/test/resources/fixtures/test-cp1252.csv");
    private static final Path FIXTURE_UTF8 = Paths.get("src/test/resources/fixtures/test-utf8.csv");
    private static final Path FIXTURE_NO_QUOTE = Paths.get("src/test/resources/fixtures/test-no-quote.csv");

    @Test
    @DisplayName("Decodifica corretamente arquivo ISO-8859-1 com acentos")
    void shouldDecodeIso88591() throws Exception {
        try (CSVReader reader = CsvParserBuilder.forCharset(StandardCharsets.ISO_8859_1).open(FIXTURE_ISO)) {
            String[] line = reader.readNext();
            assertNotNull(line);
            assertEquals("São Paulo", line[0]);
            assertEquals("Açúcar", line[1]);

            line = reader.readNext();
            assertNotNull(line);
            assertEquals("Coração", line[0]);
            assertEquals("Tubarão", line[1]);
        }
    }

    @Test
    @DisplayName("Decodifica corretamente arquivo Windows-1252 com smart quote e euro")
    void shouldDecodeCp1252() throws Exception {
        Charset cp1252 = Charset.forName("Cp1252");
        try (CSVReader reader = CsvParserBuilder.forCharsetNoQuote(cp1252).open(FIXTURE_CP1252)) {
            String[] line = reader.readNext();
            assertNotNull(line);
            // 0x92 in Cp1252 = U+2019 RIGHT SINGLE QUOTATION MARK
            assertTrue(line[1].contains("’"), "Should contain smart quote: " + line[1]);

            line = reader.readNext();
            assertNotNull(line);
            // 0x80 in Cp1252 = U+20AC EURO SIGN
            assertTrue(line[0].contains("€"), "Should contain euro sign: " + line[0]);
        }
    }

    @Test
    @DisplayName("Decodifica corretamente arquivo UTF-8 (compatibilidade IBAMA)")
    void shouldDecodeUtf8() throws Exception {
        try (CSVReader reader = CsvParserBuilder.forCharset(StandardCharsets.UTF_8).open(FIXTURE_UTF8)) {
            String[] line = reader.readNext();
            assertNotNull(line);
            assertEquals("São Paulo", line[0]);
            assertEquals("Açúcar", line[1]);
        }
    }

    @Test
    @DisplayName("Variant sem quote-char preserva aspas literais no conteudo (MTE)")
    void shouldPreserveQuotesWhenNoQuoteCharConfigured() throws Exception {
        try (CSVReader reader = CsvParserBuilder.forCharsetNoQuote(StandardCharsets.UTF_8).open(FIXTURE_NO_QUOTE)) {
            String[] line = reader.readNext();
            assertNotNull(line);
            // Espacos no campo nao devem dividir nem ser tratados como quote
            assertEquals("field with spaces", line[0]);
            // Aspas literais devem ser preservadas (nao consumidas pelo parser)
            assertTrue(line[1].contains("\""), "Quotes should be preserved literally: " + line[1]);
        }
    }

    @Test
    @DisplayName("Configuracao padrao usa separador ';' e skip do header")
    void shouldUseSemicolonAndSkipHeader() throws Exception {
        try (CSVReader reader = CsvParserBuilder.forCharset(StandardCharsets.UTF_8).open(FIXTURE_UTF8)) {
            // Skip header em open(); primeira leitura deve ser dado, nao "col_a"
            String[] line = reader.readNext();
            assertNotEquals("col_a", line[0]);
        }
    }
}
