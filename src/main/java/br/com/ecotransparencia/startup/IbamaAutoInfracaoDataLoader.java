package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.AutoInfracao;
import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.repository.AutoInfracaoRepository;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import br.com.ecotransparencia.util.CsvParserBuilder;
import com.opencsv.CSVReader;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Carrega dados de Autos de Infracao do IBAMA a partir dos arquivos CSV no startup.
 * Os arquivos estao organizados por ano em docs/ibama/autos/auto_infracao_ano_YYYY.csv
 */
@ApplicationScoped
public class IbamaAutoInfracaoDataLoader {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER_ALT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int BATCH_SIZE = 1000;

    /** Identificador de fonte usado em data_load_marker. */
    static final String SOURCE = "ibama_auto_infracao";

    @ConfigProperty(name = "app.data.load-autos-on-startup", defaultValue = "false")
    boolean loadOnStartup;

    @ConfigProperty(name = "app.data.autos-csv-dir", defaultValue = "docs/ibama/autos")
    String csvDir;

    @Inject
    AutoInfracaoRepository repository;

    @Inject
    DataLoadMarkerRepository markerRepository;

    void onStart(@Observes StartupEvent event) {
        if (!loadOnStartup) {
            Log.info("Auto Infracao data loading disabled (app.data.load-autos-on-startup=false)");
            return;
        }

        if (isAlreadyLoaded()) {
            Log.infof("Data already loaded for source=%s, skipping Auto Infracao data load", SOURCE);
            return;
        }

        loadData();
        markLoaded();
    }

    /**
     * Indica se a fonte {@link #SOURCE} ja foi carregada anteriormente.
     */
    boolean isAlreadyLoaded() {
        return markerRepository.findById(SOURCE) != null;
    }

    /**
     * Marca a fonte como carregada, em uma transacao propria.
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
        Log.infof("Starting Auto Infracao data load from directory: %s", csvDir);
        long startTime = System.currentTimeMillis();

        File dir = new File(csvDir);
        if (!dir.exists() || !dir.isDirectory()) {
            Log.errorf("Directory not found: %s", csvDir);
            return;
        }

        File[] csvFiles = dir.listFiles((d, name) -> name.startsWith("auto_infracao_ano_") && name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            Log.warn("No auto_infracao CSV files found in directory");
            return;
        }

        int totalInserted = 0;
        int totalErrors = 0;

        for (File csvFile : csvFiles) {
            Log.infof("Processing file: %s", csvFile.getName());
            int[] result = loadFile(csvFile);
            totalInserted += result[0];
            totalErrors += result[1];
        }

        long elapsed = System.currentTimeMillis() - startTime;
        Log.info("Auto Infracao data load completed:");
        Log.infof("  - Files processed: %d", csvFiles.length);
        Log.infof("  - Total inserted: %d", totalInserted);
        Log.infof("  - Total errors: %d", totalErrors);
        Log.infof("  - Time: %.1f seconds", elapsed / 1000.0);
    }

    private int[] loadFile(File csvFile) {
        int inserted = 0;
        int errors = 0;
        List<AutoInfracao> batch = new ArrayList<>(BATCH_SIZE);

        try (CSVReader csvReader = CsvParserBuilder
                .forCharset(StandardCharsets.UTF_8)
                .open(csvFile.toPath())) {

            String[] line;
            int lineNum = 1;
            while ((line = csvReader.readNext()) != null) {
                lineNum++;
                try {
                    AutoInfracao auto = parseAutoInfracao(line);
                    if (auto == null) {
                        continue;
                    }
                    batch.add(auto);

                    if (batch.size() >= BATCH_SIZE) {
                        inserted += persistBatch(batch);
                        batch.clear();
                    }
                } catch (Exception e) {
                    errors++;
                    if (errors <= 5) {
                        Log.warnf("Error parsing line %d in %s: %s", lineNum, csvFile.getName(), e.getMessage());
                    }
                }
            }

            if (!batch.isEmpty()) {
                inserted += persistBatch(batch);
            }

        } catch (Exception e) {
            Log.errorf(e, "Error reading file: %s", csvFile.getName());
        }

        return new int[]{inserted, errors};
    }

    @Transactional
    int persistBatch(List<AutoInfracao> batch) {
        for (AutoInfracao auto : batch) {
            repository.persist(auto);
        }
        repository.flush();
        return batch.size();
    }

    AutoInfracao parseAutoInfracao(String[] fields) {
        if (fields.length < 10) {
            return null;
        }

        Long seqAutoInfracao = parseLong(fields[0]);
        if (seqAutoInfracao == null) {
            return null;
        }

        AutoInfracao auto = new AutoInfracao();
        auto.setSeqAutoInfracao(seqAutoInfracao);

        // Campos de identificacao
        auto.setStatusFormulario(parseString(fields, 1));
        auto.setSituacaoAuto(parseString(fields, 2));
        auto.setSituacaoCancelado(parseString(fields, 3));
        auto.setNumAutoInfracao(parseString(fields, 4));
        auto.setSerAutoInfracao(parseString(fields, 5));
        // fields[6] = CD_ORIGINAL_AUTO_INFRACAO (ignorado)
        auto.setTipoAuto(parseString(fields, 7));
        auto.setTipoMulta(parseString(fields, 8));
        auto.setValorAutoInfracao(parseDecimal(fields, 9));
        auto.setFundamentacaoMulta(parseString(fields, 10));
        // fields[11] = PATRIMONIO_APURACAO (ignorado)
        auto.setGravidadeInfracao(parseString(fields, 12));
        auto.setCodigoNivelGravidade(parseString(fields, 13));
        auto.setMotivacaoConduta(parseString(fields, 14));
        auto.setEfeitoMeioAmbiente(parseString(fields, 15));
        auto.setEfeitoSaudePublica(parseString(fields, 16));
        auto.setPassivelRecuperacao(parseString(fields, 17));
        // fields[18] = UNID_ARRECADACAO (ignorado)
        auto.setDescricaoAutoInfracao(parseString(fields, 19));
        auto.setDataHoraAutoInfracao(parseDateTime(fields, 20));
        // fields[21] = FORMA_ENTREGA (ignorado)
        auto.setDataCienciaAutuacao(parseDateTime(fields, 22));
        auto.setDataFatoInfracional(parseDateTime(fields, 23));
        // fields[24-26] = DT_INICIO_ATO_INEQUIVOCO, DT_FIM_ATO_INEQUIVOCO, DS_UNID_CONCILIACAO (ignorados)
        auto.setCodigoMunicipio(parseInteger(fields, 27));
        auto.setMunicipio(parseString(fields, 28));
        auto.setUf(parseString(fields, 29));
        auto.setNumeroProcesso(parseString(fields, 30));
        // fields[31] = NU_PROCESSO_FORMATADO (ignorado)
        auto.setCodigoInfracao(parseInteger(fields, 32));
        auto.setDescricaoInfracao(parseString(fields, 33));
        auto.setTipoInfracao(parseString(fields, 34));
        // fields[35-36] = CD_RECEITA_AUTO_INFRACAO, DES_RECEITA (ignorados)
        auto.setTipoPessoaInfrator(parseString(fields, 37));
        // fields[38] = NUM_PESSOA_INFRATOR (ignorado)
        auto.setNomeInfrator(parseString(fields, 39));
        auto.setCpfCnpjInfrator(parseString(fields, 40));
        auto.setQuantidadeArea(parseDecimal(fields, 41));
        auto.setInfracaoArea(parseString(fields, 42));
        // fields[43] = DES_OUTROS_TIPO_AREA (ignorado)
        auto.setClassificacaoArea(parseString(fields, 44));
        // fields[45] = DS_FATOR_AJUSTE (ignorado)
        auto.setLongitude(parseDecimal(fields, 46));
        auto.setLatitude(parseDecimal(fields, 47));
        // fields[48] = DS_WKT (ignorado)
        auto.setDescricaoLocalInfracao(parseString(fields, 49));
        // fields[50-52] = DS_REFERENCIA_ACAO_FISCALIZATORIA, UNIDADE_CONSERVACAO, ID_SICAFI_BIOMAS_ATINGIDOS_INFRACAO (ignorados)
        auto.setBiomasAtingidos(parseString(fields, 53));
        // fields[54-60] = SEQ_NOTIFICACAO ate DENUNCIA_SISLIV (ignorados)
        auto.setOperacao(parseString(fields, 59));
        // fields[61-66] = SEQ_ORDEM_FISCALIZACAO ate OPERACAO_SOL_RECURSO (ignorados)
        auto.setDataLancamento(parseDateTime(fields, 67));
        // fields[68] = TP_ULT_ALTERACAO (ignorado)
        auto.setDataUltimaAlteracao(parseDateTime(fields, 69));
        // fields[70-76] = JUSTIFICATIVA_ALTERACAO ate ST_AUTO_MIGRADO_AIE (ignorados)
        auto.setEnquadramentoAdministrativo(parseString(fields, 77));
        // fields[78-82] = DS_ENQUADRAMENTO_NAO_ADMINISTRATIVO ate TP_ORIGEM_REGISTRO_AUTO (ignorados)
        auto.setUltimaAtualizacaoRelatorio(parseDateTime(fields, 83));

        return auto;
    }

    private String parseString(String[] fields, int index) {
        if (index >= fields.length) return null;
        String value = fields[index];
        if (value == null || value.isBlank()) return null;
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value.isBlank() ? null : value;
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String[] fields, int index) {
        String value = parseString(fields, index);
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String[] fields, int index) {
        String value = parseString(fields, index);
        if (value == null) return null;
        try {
            // Formato brasileiro: 15000,00 -> 15000.00
            value = value.replace(",", ".");
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String[] fields, int index) {
        String value = parseString(fields, index);
        if (value == null) return null;
        try {
            // Tenta diferentes formatos
            if (value.contains(" ")) {
                return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
            } else if (value.contains("T")) {
                return LocalDateTime.parse(value, DATE_TIME_FORMATTER_ALT);
            } else {
                return LocalDate.parse(value, DATE_FORMATTER).atStartOfDay();
            }
        } catch (Exception e) {
            return null;
        }
    }
}
