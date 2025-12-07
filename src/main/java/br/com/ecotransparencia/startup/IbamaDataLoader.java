package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.repository.EmbargoRepository;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.FileReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Carrega dados do IBAMA a partir do arquivo CSV no startup da aplicação.
 *
 * US-INFRA-001: Carregar base de dados do IBAMA no startup
 */
@ApplicationScoped
public class IbamaDataLoader {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int BATCH_SIZE = 1000;

    @ConfigProperty(name = "app.data.load-on-startup", defaultValue = "false")
    boolean loadOnStartup;

    @ConfigProperty(name = "app.data.csv-path", defaultValue = "docs/ibama/areas_embargadas.csv")
    String csvPath;

    @Inject
    EmbargoRepository repository;

    // Getter para testes
    public boolean isLoadOnStartup() {
        return loadOnStartup;
    }

    void onStart(@Observes StartupEvent event) {
        if (!loadOnStartup) {
            Log.info("IBAMA data loading disabled (app.data.load-on-startup=false)");
            return;
        }

        long count = repository.count();
        if (count > 0) {
            Log.infof("Database already populated with %d records, skipping IBAMA data load", count);
            return;
        }

        loadData();
    }

    void loadData() {
        Log.infof("Starting IBAMA data load from %s", csvPath);
        long startTime = System.currentTimeMillis();

        int totalRecords = 0;
        int inserted = 0;
        int skipped = 0;
        int errors = 0;
        List<Embargo> batch = new ArrayList<>(BATCH_SIZE);

        try (Reader reader = new FileReader(csvPath, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                     .withSkipLines(1) // Skip header
                     .build()) {

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                totalRecords++;
                try {
                    Embargo embargo = parseEmbargo(line);
                    if (embargo == null) {
                        skipped++;
                        if (skipped <= 10) {
                            Log.warnf("Skipping line %d: missing SEQ_TAD (primary key)", totalRecords);
                        }
                        continue;
                    }
                    batch.add(embargo);

                    if (batch.size() >= BATCH_SIZE) {
                        inserted += persistBatch(batch);
                        int batchNum = (inserted / BATCH_SIZE);
                        Log.infof("Processing batch %d (%d records inserted)", batchNum, inserted);
                        batch.clear();
                    }
                } catch (Exception e) {
                    errors++;
                    if (errors <= 10) {
                        Log.warnf("Error parsing line %d: %s", totalRecords, e.getMessage());
                    }
                }
            }

            // Persist remaining records
            if (!batch.isEmpty()) {
                inserted += persistBatch(batch);
            }

        } catch (Exception e) {
            Log.errorf(e, "Error loading IBAMA data from %s", csvPath);
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        Log.info("IBAMA data load completed:");
        Log.infof("  - Total records: %d", totalRecords);
        Log.infof("  - Inserted: %d", inserted);
        Log.infof("  - Skipped (no SEQ_TAD): %d", skipped);
        Log.infof("  - Errors: %d", errors);
        Log.infof("  - Time: %.1f seconds", elapsed / 1000.0);
    }

    @Transactional
    int persistBatch(List<Embargo> batch) {
        for (Embargo embargo : batch) {
            repository.persist(embargo);
        }
        repository.flush();
        return batch.size();
    }

    Embargo parseEmbargo(String[] fields) {
        // SEQ_TAD é obrigatório (chave primária)
        Long seqTad = parseLong(fields[0]);
        if (seqTad == null) {
            return null;
        }

        Embargo embargo = new Embargo();

        // Campos de identificação
        embargo.setSeqTad(seqTad);
        embargo.setNumTad(parseString(fields[1]));
        embargo.setSerTad(parseString(fields[2]));

        // Campos temporais
        embargo.setDatEmbargo(parseDateTime(fields[3]));
        embargo.setDatUltAlteracao(parseDateTime(fields[4]));

        // Campos de localização
        // fields[5] = COD_UF_TAD (não mapeado na entity atual)
        embargo.setSigUfTad(parseString(fields[6]));
        // fields[7] = COD_MUNICIPIO_TAD (não mapeado na entity atual)
        embargo.setNomMunicipioTad(parseString(fields[8]));
        embargo.setNumLongitudeTad(parseDecimal(fields[9]));
        embargo.setNumLatitudeTad(parseDecimal(fields[10]));
        // fields[11] = NUM_LONGITUDE_GMS_TAD (não mapeado)
        // fields[12] = NUM_LATITUDE_GMS_TAD (não mapeado)
        embargo.setNomeImovel(parseString(fields[13]));
        embargo.setDesLocalizacaoTad(parseString(fields[14]));

        // Campos do embargado
        embargo.setNomePessoaEmbargada(parseString(fields[15]));
        embargo.setCpfCnpjEmbargado(parseString(fields[16]));

        // Campos de caracterização
        embargo.setSitDesmatamento(parseString(fields[17]));
        embargo.setTpAreaEmbargada(parseString(fields[18]));
        // fields[19] = DS_OUTROS_TIPO_AREA (não mapeado)
        // fields[20] = ST_AREA_DESMATADA_ILEGAL (não mapeado)
        embargo.setQtdAreaEmbargada(parseDecimal(fields[21]));

        // Campos administrativos
        embargo.setOperacao(parseString(fields[22]));
        embargo.setUnidIbamaControle(parseString(fields[23]));
        // fields[24] = ORDEM_FISCALIZACAO (não mapeado)
        // fields[25] = ACAO_FISCALIZATORIA (não mapeado)
        embargo.setNumProcesso(parseString(fields[26]));
        embargo.setDesTad(parseString(fields[27]));

        // fields[28] = WKT_GEOM_AREA_EMBARGADA (não mapeado)
        // fields[29] = DAT_ULT_ALTER_GEOM (não mapeado)

        // Campos do auto de infração
        embargo.setNumAutoInfracao(parseString(fields[30]));
        embargo.setSerAutoInfracao(parseString(fields[31]));
        embargo.setQtdAreaDesmatada(parseDecimal(fields[32]));
        embargo.setDesInfracao(parseString(fields[33]));

        // Campos de bioma
        embargo.setCodTipoBioma(parseInteger(fields[34]));
        embargo.setDesTipoBioma(parseString(fields[35]));

        return embargo;
    }

    private String parseString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        // Remove aspas se presentes
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.isBlank() ? null : value;
    }

    private Long parseLong(String value) {
        String cleaned = parseString(value);
        if (cleaned == null) return null;
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        String cleaned = parseString(value);
        if (cleaned == null) return null;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String value) {
        String cleaned = parseString(value);
        if (cleaned == null) return null;
        try {
            // Substitui vírgula por ponto (formato brasileiro)
            cleaned = cleaned.replace(",", ".");
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        String cleaned = parseString(value);
        if (cleaned == null) return null;
        try {
            return LocalDateTime.parse(cleaned, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
