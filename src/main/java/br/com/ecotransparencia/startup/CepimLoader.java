package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.Cepim;
import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.repository.CepimRepository;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import br.com.ecotransparencia.util.CsvParserBuilder;
import br.com.ecotransparencia.util.LatestCsvByPattern;
import com.opencsv.CSVReader;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Carrega CEPIM (Cadastro de Entidades Privadas Sem Fins Lucrativos Impedidas).
 *
 * <p>Encoding ISO-8859-1, separador {@code ';'}, quote {@code '"'}.
 * Glob por padrao {@code _CEPIM.csv$} para tolerar prefixo de data.
 * Idempotencia via {@link DataLoadMarker} ({@code source = cepim_v1}).
 */
@ApplicationScoped
public class CepimLoader {

    private static final int BATCH_SIZE = 500;

    static final String SOURCE = "cepim_v1";

    @ConfigProperty(name = "app.data.load-cepim-on-startup", defaultValue = "false")
    boolean loadOnStartup;

    @ConfigProperty(name = "app.data.adm-publica-csv-dir", defaultValue = "docs/adm_publica")
    String csvDir;

    @Inject
    CepimRepository repository;

    @Inject
    DataLoadMarkerRepository markerRepository;

    void onStart(@Observes StartupEvent event) {
        if (!loadOnStartup) {
            Log.info("CEPIM data loading disabled (app.data.load-cepim-on-startup=false)");
            return;
        }
        runLoad();
    }

    void runLoad() {
        if (isAlreadyLoaded()) {
            Log.infof("Data already loaded for source=%s, skipping", SOURCE);
            return;
        }

        Path dir = Paths.get(csvDir);
        Optional<Path> file = LatestCsvByPattern.findLatest(dir, "_CEPIM\\.csv$");
        if (file.isEmpty()) {
            Log.warnf("No CEPIM file found in directory: %s", csvDir);
            return;
        }

        long startTime = System.currentTimeMillis();
        Log.infof("Loading CEPIM from %s", file.get());
        int inserted = loadFile(file.get());
        long elapsed = System.currentTimeMillis() - startTime;
        Log.infof("CEPIM data load completed: %d records in %.1fs", inserted, elapsed / 1000.0);

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
        List<Cepim> batch = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = CsvParserBuilder.forCharset(StandardCharsets.ISO_8859_1).open(file)) {
            String[] line;
            int lineNum = 1;
            while ((line = reader.readNext()) != null) {
                lineNum++;
                try {
                    Cepim c = parseRow(line);
                    if (c != null) {
                        batch.add(c);
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

        Log.infof("  - CEPIM: inserted=%d errors=%d", inserted, errors);
        return inserted;
    }

    @Transactional
    int persistBatch(List<Cepim> batch) {
        for (Cepim c : batch) {
            repository.persist(c);
        }
        repository.flush();
        return batch.size();
    }

    Cepim parseRow(String[] f) {
        if (f.length < 5) {
            return null;
        }
        Cepim c = new Cepim();
        c.setCnpjEntidade(parse(f, 0));
        c.setNomeEntidade(parse(f, 1));
        c.setNumeroConvenio(parse(f, 2));
        c.setOrgaoConcedente(parse(f, 3));
        c.setMotivoImpedimento(parse(f, 4));
        return c;
    }

    private static String parse(String[] f, int idx) {
        if (idx >= f.length) return null;
        String v = f[idx];
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }
}
