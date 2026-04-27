package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import br.com.ecotransparencia.repository.EmbargoRepository;
import br.com.ecotransparencia.util.CsvParserBuilder;
import com.opencsv.CSVReader;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Carrega dados do IBAMA a partir do arquivo CSV no startup da aplicação.
 *
 * US-INFRA-001: Carregar base de dados do IBAMA no startup
 */
@ApplicationScoped
public class IbamaDataLoader {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int BATCH_SIZE = 1000;

    /** Identificador de fonte usado em data_load_marker. */
    static final String SOURCE = "ibama_embargo";

    @ConfigProperty(name = "app.data.load-on-startup", defaultValue = "false")
    boolean loadOnStartup;

    @ConfigProperty(name = "app.data.csv-path", defaultValue = "docs/ibama/areas_embargadas.csv")
    String csvPath;

    @Inject
    EmbargoRepository repository;

    @Inject
    DataLoadMarkerRepository markerRepository;

    // Getter para testes
    public boolean isLoadOnStartup() {
        return loadOnStartup;
    }

    void onStart(@Observes StartupEvent event) {
        if (!loadOnStartup) {
            Log.info("IBAMA data loading disabled (app.data.load-on-startup=false)");
            return;
        }

        if (isAlreadyLoaded()) {
            Log.infof("Data already loaded for source=%s, skipping IBAMA data load", SOURCE);
            return;
        }

        loadData();
        markLoaded();
    }

    /**
     * Indica se a fonte {@link #SOURCE} ja foi carregada anteriormente.
     * Substitui a heuristica antiga "tabela vazia?" por uma marker explicita,
     * evitando race-condition em cargas parciais.
     */
    boolean isAlreadyLoaded() {
        return markerRepository.findById(SOURCE) != null;
    }

    /**
     * Marca a fonte como carregada, em uma transacao propria.
     * Chamada apos {@link #loadData()} concluir com sucesso.
     */
    @Transactional
    void markLoaded() {
        DataLoadMarker marker = new DataLoadMarker();
        marker.setSource(SOURCE);
        LocalDateTime now = LocalDateTime.now();
        marker.setLoadedAt(now);
        marker.setVersion(now.toString());
        markerRepository.persist(marker);
    }

    void loadData() {
        Log.infof("Starting IBAMA data load from %s", csvPath);
        long startTime = System.currentTimeMillis();

        int totalRecords = 0;
        int inserted = 0;
        int skipped = 0;
        int duplicates = 0;
        int errors = 0;
        // Dedup por SEQ_TAD: o CSV do IBAMA pode conter linhas duplicadas
        // (atualizacoes nao removidas do dump). Mantem a ultima ocorrencia
        // (proxima ao final do arquivo, presumivelmente mais recente).
        Set<Long> seen = new HashSet<>();
        List<Embargo> batch = new ArrayList<>(BATCH_SIZE);

        try (CSVReader csvReader = CsvParserBuilder
                .forCharset(StandardCharsets.UTF_8)
                .open(Paths.get(csvPath))) {

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
                    if (!seen.add(embargo.getSeqTad())) {
                        duplicates++;
                        if (duplicates <= 5) {
                            Log.warnf("Duplicate SEQ_TAD=%d on line %d, keeping previous", embargo.getSeqTad(), totalRecords);
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
        Log.infof("  - Duplicates ignored: %d", duplicates);
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
