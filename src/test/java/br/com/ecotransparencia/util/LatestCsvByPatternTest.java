package br.com.ecotransparencia.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para LatestCsvByPattern.
 *
 * O Portal da Transparencia publica CSVs com prefixo de data (YYYYMMDD_NOME.csv)
 * que muda a cada refresh. O helper devolve o arquivo mais recente que casa com
 * o padrao de sufixo informado.
 */
class LatestCsvByPatternTest {

    @Test
    @DisplayName("Retorna o arquivo mais recente quando ha varios com prefixo de data")
    void shouldReturnLatestByDatePrefix(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("20260101_CEIS.csv"));
        Files.createFile(tempDir.resolve("20260201_CEIS.csv"));
        Files.createFile(tempDir.resolve("20260315_CEIS.csv"));

        Optional<Path> latest = LatestCsvByPattern.findLatest(tempDir, "_CEIS.csv$");

        assertTrue(latest.isPresent());
        assertEquals("20260315_CEIS.csv", latest.get().getFileName().toString());
    }

    @Test
    @DisplayName("Retorna empty quando nao ha arquivo casando com o padrao")
    void shouldReturnEmptyWhenNoMatch(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("20260101_CNEP.csv"));
        Files.createFile(tempDir.resolve("readme.txt"));

        Optional<Path> latest = LatestCsvByPattern.findLatest(tempDir, "_CEIS.csv$");

        assertFalse(latest.isPresent());
    }

    @Test
    @DisplayName("Filtra apenas arquivos que casam com o sufixo (ignora outras fontes na mesma pasta)")
    void shouldFilterOnlyMatchingPattern(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("20260424_CEIS.csv"));
        Files.createFile(tempDir.resolve("20260424_CNEP.csv"));
        Files.createFile(tempDir.resolve("20260423_CEPIM.csv"));

        Optional<Path> ceis = LatestCsvByPattern.findLatest(tempDir, "_CEIS.csv$");
        Optional<Path> cnep = LatestCsvByPattern.findLatest(tempDir, "_CNEP.csv$");
        Optional<Path> cepim = LatestCsvByPattern.findLatest(tempDir, "_CEPIM.csv$");

        assertEquals("20260424_CEIS.csv", ceis.get().getFileName().toString());
        assertEquals("20260424_CNEP.csv", cnep.get().getFileName().toString());
        assertEquals("20260423_CEPIM.csv", cepim.get().getFileName().toString());
    }

    @Test
    @DisplayName("Retorna empty quando o diretorio nao existe")
    void shouldReturnEmptyWhenDirectoryDoesNotExist(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("doesnotexist");

        Optional<Path> latest = LatestCsvByPattern.findLatest(missing, "_CEIS.csv$");

        assertFalse(latest.isPresent());
    }

    @Test
    @DisplayName("Retorna empty quando o diretorio existe mas esta vazio")
    void shouldReturnEmptyWhenDirectoryIsEmpty(@TempDir Path tempDir) {
        Optional<Path> latest = LatestCsvByPattern.findLatest(tempDir, "_CEIS.csv$");

        assertFalse(latest.isPresent());
    }
}
