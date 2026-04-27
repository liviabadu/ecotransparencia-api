package br.com.ecotransparencia.util;

import io.quarkus.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Helper para localizar o arquivo CSV mais recente em um diretorio,
 * filtrando por padrao de sufixo (regex) sobre o nome do arquivo.
 *
 * Motivacao: o Portal da Transparencia publica CSVs com prefixo de data
 * (YYYYMMDD_NOME.csv) que muda a cada refresh. As properties dos loaders
 * apontam para o <b>diretorio</b>; este helper resolve o arquivo concreto
 * em runtime, escolhendo o mais recente lexicograficamente (o que coincide
 * com o mais recente cronologicamente para o formato YYYYMMDD).
 */
public final class LatestCsvByPattern {

    private LatestCsvByPattern() {
        // Classe utilitaria
    }

    /**
     * Localiza o arquivo mais recente no {@code directory} cujo nome
     * casa com {@code suffixPattern} (regex).
     *
     * @param directory diretorio a varrer (nao recursivo)
     * @param suffixPattern regex aplicada sobre o nome do arquivo
     *                      (ex.: {@code "_CEIS.csv$"})
     * @return o arquivo mais recente, ou {@link Optional#empty()} se
     *         nenhum casar ou se o diretorio nao existir
     */
    public static Optional<Path> findLatest(Path directory, String suffixPattern) {
        if (!Files.isDirectory(directory)) {
            Log.debugf("Directory not found: %s", directory);
            return Optional.empty();
        }

        Pattern pattern = Pattern.compile(suffixPattern);
        try (Stream<Path> entries = Files.list(directory)) {
            return entries
                    .filter(Files::isRegularFile)
                    .filter(p -> pattern.matcher(p.getFileName().toString()).find())
                    .max(Comparator.comparing(p -> p.getFileName().toString()));
        } catch (IOException e) {
            Log.warnf(e, "Failed to list directory: %s", directory);
            return Optional.empty();
        }
    }
}
