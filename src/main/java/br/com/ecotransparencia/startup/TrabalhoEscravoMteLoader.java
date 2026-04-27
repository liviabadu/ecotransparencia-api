package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.entity.TrabalhoEscravoMte;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import br.com.ecotransparencia.repository.TrabalhoEscravoMteRepository;
import br.com.ecotransparencia.util.CsvParserBuilder;
import br.com.ecotransparencia.util.DocumentoUtil;
import com.opencsv.CSVReader;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Carrega a Lista Suja do MTE a partir de {@code docs/mte/tr_escravo.csv}.
 *
 * <p>Encoding: Windows-1252 (Cp1252). Separador: {@code ';'}.
 * O CSV NAO usa aspas; campos podem conter virgulas internas. Por isso o
 * parser e' configurado sem quote-char ({@link CsvParserBuilder#forCharsetNoQuote}).
 *
 * <p>O CPF/CNPJ vem formatado (ex.: {@code 41.297.068/0001-61}); guardamos
 * tanto a forma original (campo {@code cpfCnpjFormatado}) quanto a normalizada
 * digit-only (campo {@code cpfCnpj}, indexada).
 *
 * <p>Idempotencia via {@link DataLoadMarker} ({@code source = trabalho_escravo_mte_v1}).
 */
@ApplicationScoped
public class TrabalhoEscravoMteLoader {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Charset CP1252 = Charset.forName("Cp1252");
    private static final int BATCH_SIZE = 500;

    static final String SOURCE = "trabalho_escravo_mte_v1";

    @ConfigProperty(name = "app.data.load-trabalho-escravo-on-startup", defaultValue = "false")
    boolean loadOnStartup;

    @ConfigProperty(name = "app.data.mte-csv-path", defaultValue = "docs/mte/tr_escravo.csv")
    String csvPath;

    @Inject
    TrabalhoEscravoMteRepository repository;

    @Inject
    DataLoadMarkerRepository markerRepository;

    void onStart(@Observes StartupEvent event) {
        if (!loadOnStartup) {
            Log.info("MTE Trabalho Escravo data loading disabled (app.data.load-trabalho-escravo-on-startup=false)");
            return;
        }
        runLoad();
    }

    void runLoad() {
        if (isAlreadyLoaded()) {
            Log.infof("Data already loaded for source=%s, skipping", SOURCE);
            return;
        }

        Path file = Paths.get(csvPath);
        if (!Files.isRegularFile(file)) {
            Log.warnf("MTE file not found: %s", csvPath);
            return;
        }

        long startTime = System.currentTimeMillis();
        Log.infof("Loading MTE Trabalho Escravo from %s", file);
        int inserted = loadFile(file);
        long elapsed = System.currentTimeMillis() - startTime;
        Log.infof("MTE Trabalho Escravo data load completed: %d records in %.1fs", inserted, elapsed / 1000.0);

        markLoaded();
    }

    boolean isAlreadyLoaded() {
        return markerRepository.findById(SOURCE) != null;
    }

    @Transactional
    void markLoaded() {
        DataLoadMarker marker = new DataLoadMarker();
        marker.setSource(SOURCE);
        LocalDateTime now = LocalDateTime.now();
        marker.setLoadedAt(now);
        marker.setVersion(now.toString());
        markerRepository.persist(marker);
    }

    private int loadFile(Path file) {
        int inserted = 0;
        int errors = 0;
        List<TrabalhoEscravoMte> batch = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = CsvParserBuilder.forCharsetNoQuote(CP1252).open(file)) {
            String[] line;
            int lineNum = 1;
            while ((line = reader.readNext()) != null) {
                lineNum++;
                try {
                    TrabalhoEscravoMte t = parseRow(line);
                    if (t != null) {
                        batch.add(t);
                        if (batch.size() >= BATCH_SIZE) {
                            inserted += persistBatch(batch);
                            batch.clear();
                        }
                    }
                } catch (Exception e) {
                    errors++;
                    if (errors <= 5) {
                        Log.warnf("Error parsing line %d in %s: %s", lineNum, file.getFileName(), e.getMessage());
                    }
                }
            }
            if (!batch.isEmpty()) {
                inserted += persistBatch(batch);
            }
        } catch (Exception e) {
            Log.errorf(e, "Error reading file: %s", file);
        }

        Log.infof("  - MTE: inserted=%d errors=%d", inserted, errors);
        return inserted;
    }

    @Transactional
    int persistBatch(List<TrabalhoEscravoMte> batch) {
        for (TrabalhoEscravoMte t : batch) {
            repository.persist(t);
        }
        repository.flush();
        return batch.size();
    }

    TrabalhoEscravoMte parseRow(String[] f) {
        if (f.length < 10) {
            return null;
        }
        TrabalhoEscravoMte t = new TrabalhoEscravoMte();
        t.setIdOrigem(parseInt(f, 0));
        t.setAnoAcaoFiscal(parseInt(f, 1));
        t.setUf(parse(f, 2));
        t.setEmpregador(parse(f, 3));

        String cpfCnpjFormatado = parse(f, 4);
        t.setCpfCnpjFormatado(cpfCnpjFormatado);
        t.setCpfCnpj(DocumentoUtil.limpar(cpfCnpjFormatado));

        t.setEstabelecimento(parse(f, 5));
        t.setTrabalhadoresEnvolvidos(parseInt(f, 6));
        t.setCnae(parse(f, 7));
        t.setDecisaoAdmProcedencia(parseDate(f, 8));
        t.setInclusaoCadastroEmpregadores(parseDate(f, 9));
        return t;
    }

    private static String parse(String[] f, int idx) {
        if (idx >= f.length) return null;
        String v = f[idx];
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    private static Integer parseInt(String[] f, int idx) {
        String v = parse(f, idx);
        if (v == null) return null;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parseDate(String[] f, int idx) {
        String v = parse(f, idx);
        if (v == null) return null;
        try {
            return LocalDate.parse(v, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
