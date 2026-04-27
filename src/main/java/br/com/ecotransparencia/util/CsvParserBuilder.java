package br.com.ecotransparencia.util;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Factory utilitario para construir {@link CSVReader} configurado para os
 * formatos CSV usados no projeto.
 *
 * Fontes suportadas:
 * <ul>
 *   <li>IBAMA (UTF-8, separador ';' com aspas)</li>
 *   <li>CEIS / CNEP / CEPIM (ISO-8859-1, separador ';' com aspas)</li>
 *   <li>MTE (Windows-1252, separador ';' sem aspas)</li>
 * </ul>
 *
 * Padroniza:
 * <ul>
 *   <li>Separador {@code ';'}</li>
 *   <li>Skip da linha de cabecalho</li>
 *   <li>Charset parametrizado (decodificacao via {@link InputStreamReader})</li>
 *   <li>Variante "sem quote" para arquivos cujos campos podem conter aspas literais</li>
 * </ul>
 */
public final class CsvParserBuilder {

    private static final char SEPARATOR = ';';
    private static final int SKIP_HEADER_LINES = 1;

    private final Charset charset;
    private final char quoteChar;

    private CsvParserBuilder(Charset charset, char quoteChar) {
        this.charset = charset;
        this.quoteChar = quoteChar;
    }

    /**
     * Builder padrao com aspas {@code "} (CEIS, CNEP, CEPIM, IBAMA).
     */
    public static CsvParserBuilder forCharset(Charset charset) {
        return new CsvParserBuilder(charset, ICSVParser.DEFAULT_QUOTE_CHARACTER);
    }

    /**
     * Builder sem processamento de aspas: campos podem conter {@code "} literal
     * sem que o parser o interprete. Necessario para o CSV do MTE.
     */
    public static CsvParserBuilder forCharsetNoQuote(Charset charset) {
        return new CsvParserBuilder(charset, ICSVParser.NULL_CHARACTER);
    }

    /**
     * Abre o CSV no caminho informado e retorna um {@link CSVReader} ja
     * posicionado apos o cabecalho.
     */
    public CSVReader open(Path path) throws IOException {
        Reader reader = new InputStreamReader(Files.newInputStream(path), charset);
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(SEPARATOR)
                .withQuoteChar(quoteChar)
                .build();
        return new CSVReaderBuilder(reader)
                .withCSVParser(parser)
                .withSkipLines(SKIP_HEADER_LINES)
                .build();
    }
}
