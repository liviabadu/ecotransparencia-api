package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.domain.CadastroSancao;
import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.entity.SancaoAdmPublica;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import br.com.ecotransparencia.repository.SancaoAdmPublicaRepository;
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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Carrega CEIS + CNEP do Portal da Transparencia.
 *
 * <p>Ambos os cadastros vivem na mesma tabela {@code sancao_adm_publica},
 * discriminados por {@link CadastroSancao}. CEIS tem 24 colunas; CNEP tem 25
 * (apenas {@code VALOR DA MULTA} adicional na posicao 10, deslocando todas
 * as colunas subsequentes em +1).
 *
 * <p>Encoding: ISO-8859-1. Separador: {@code ';'}. Quote: {@code '"'}.
 *
 * <p>Glob por padrao de sufixo via {@link LatestCsvByPattern} para tolerar
 * o prefixo de data dos arquivos do Portal da Transparencia
 * (ex.: {@code 20260424_CEIS.csv}).
 *
 * <p>Idempotencia via {@link DataLoadMarker} ({@code source = sancao_adm_publica_v1}).
 */
@ApplicationScoped
public class SancaoAdmPublicaLoader {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int BATCH_SIZE = 500;

    /** Identificador de fonte usado em data_load_marker. */
    static final String SOURCE = "sancao_adm_publica_v1";

    @ConfigProperty(name = "app.data.load-sancao-adm-publica-on-startup", defaultValue = "false")
    boolean loadOnStartup;

    @ConfigProperty(name = "app.data.adm-publica-csv-dir", defaultValue = "docs/adm_publica")
    String csvDir;

    @Inject
    SancaoAdmPublicaRepository repository;

    @Inject
    DataLoadMarkerRepository markerRepository;

    void onStart(@Observes StartupEvent event) {
        if (!loadOnStartup) {
            Log.info("Sancao Adm Publica data loading disabled (app.data.load-sancao-adm-publica-on-startup=false)");
            return;
        }
        runLoad();
    }

    /**
     * Executa a carga. Visivel a testes; gate de idempotencia interno.
     */
    void runLoad() {
        if (isAlreadyLoaded()) {
            Log.infof("Data already loaded for source=%s, skipping", SOURCE);
            return;
        }

        Path dir = Paths.get(csvDir);
        Optional<Path> ceis = LatestCsvByPattern.findLatest(dir, "_CEIS\\.csv$");
        Optional<Path> cnep = LatestCsvByPattern.findLatest(dir, "_CNEP\\.csv$");

        if (ceis.isEmpty() && cnep.isEmpty()) {
            Log.warnf("No CEIS/CNEP files found in directory: %s", csvDir);
            return;
        }

        long startTime = System.currentTimeMillis();
        int totalInserted = 0;

        if (ceis.isPresent()) {
            Log.infof("Loading CEIS from %s", ceis.get());
            totalInserted += loadFile(ceis.get(), CadastroSancao.CEIS);
        }
        if (cnep.isPresent()) {
            Log.infof("Loading CNEP from %s", cnep.get());
            totalInserted += loadFile(cnep.get(), CadastroSancao.CNEP);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        Log.infof("Sancao Adm Publica data load completed: %d records in %.1fs", totalInserted, elapsed / 1000.0);

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

    private int loadFile(Path file, CadastroSancao cadastro) {
        int inserted = 0;
        int errors = 0;
        List<SancaoAdmPublica> batch = new ArrayList<>(BATCH_SIZE);

        try (CSVReader reader = CsvParserBuilder.forCharset(StandardCharsets.ISO_8859_1).open(file)) {
            String[] line;
            int lineNum = 1;
            while ((line = reader.readNext()) != null) {
                lineNum++;
                try {
                    SancaoAdmPublica s = parseRow(line, cadastro);
                    if (s != null) {
                        batch.add(s);
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

        Log.infof("  - %s: inserted=%d errors=%d", cadastro, inserted, errors);
        return inserted;
    }

    @Transactional
    int persistBatch(List<SancaoAdmPublica> batch) {
        for (SancaoAdmPublica s : batch) {
            repository.persist(s);
        }
        repository.flush();
        return batch.size();
    }

    /**
     * Parsea uma linha respeitando o offset de coluna entre CEIS e CNEP.
     *
     * <p>CNEP adiciona {@code VALOR DA MULTA} na posicao 10, deslocando todas
     * as colunas subsequentes em +1. Helper {@link #idx(int, CadastroSancao)}
     * aplica o offset.
     */
    SancaoAdmPublica parseRow(String[] f, CadastroSancao cadastro) {
        if (f.length < 24) {
            return null;
        }

        SancaoAdmPublica s = new SancaoAdmPublica();
        s.setCadastro(cadastro);

        // Colunas comuns (mesmas posicoes em CEIS e CNEP):
        // 0=CADASTRO, 1=CODIGO_SANCAO, 2=TIPO_PESSOA, 3=CPF_CNPJ, 4=NOME,
        // 5=NOME_INFORMADO, 6=RAZAO_SOCIAL, 7=NOME_FANTASIA, 8=NUMERO_PROCESSO,
        // 9=CATEGORIA_SANCAO
        s.setCodigoSancao(parse(f, 1));
        s.setTipoPessoa(parse(f, 2));
        s.setCpfCnpj(parse(f, 3));
        s.setNomeSancionado(parse(f, 4));
        s.setNomeOrgaoSancionador(parse(f, 5));
        s.setRazaoSocialReceita(parse(f, 6));
        s.setNomeFantasiaReceita(parse(f, 7));
        s.setNumeroProcesso(parse(f, 8));
        s.setCategoriaSancao(parse(f, 9));

        // CNEP-only: VALOR DA MULTA na posicao 10
        if (cadastro == CadastroSancao.CNEP) {
            s.setValorMulta(parseDecimalPtBr(f, 10));
        }

        // Posicoes a partir de 10 em CEIS / 11 em CNEP (apply offset).
        s.setDataInicioSancao(parseDate(f, idx(10, cadastro)));
        s.setDataFimSancao(parseDate(f, idx(11, cadastro)));
        s.setDataPublicacao(parseDate(f, idx(12, cadastro)));
        s.setPublicacao(parse(f, idx(13, cadastro)));
        s.setDetalhamentoMeioPublicacao(parse(f, idx(14, cadastro)));
        s.setDataTransitoJulgado(parseDate(f, idx(15, cadastro)));
        s.setAbrangenciaSancao(parse(f, idx(16, cadastro)));
        s.setOrgaoSancionador(parse(f, idx(17, cadastro)));
        s.setUfOrgao(parse(f, idx(18, cadastro)));
        s.setEsferaOrgao(parse(f, idx(19, cadastro)));
        s.setFundamentacaoLegal(parse(f, idx(20, cadastro)));
        s.setDataOrigemInformacao(parseDate(f, idx(21, cadastro)));
        s.setOrigemInformacoes(parse(f, idx(22, cadastro)));
        s.setObservacoes(parse(f, idx(23, cadastro)));

        return s;
    }

    /**
     * Aplica offset de coluna: CNEP tem +1 a partir do indice CEIS 10
     * (porque insere VALOR DA MULTA na posicao 10).
     */
    private static int idx(int ceisIndex, CadastroSancao cadastro) {
        return cadastro == CadastroSancao.CNEP ? ceisIndex + 1 : ceisIndex;
    }

    private static String parse(String[] f, int idx) {
        if (idx >= f.length) return null;
        String v = f[idx];
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
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

    /**
     * Decimal pt-BR: {@code "517662,90"} -> {@code 517662.90}.
     * Remove separador de milhar ('.') antes de trocar virgula por ponto.
     */
    private static BigDecimal parseDecimalPtBr(String[] f, int idx) {
        String v = parse(f, idx);
        if (v == null) return null;
        String normalized = v.replace(".", "").replace(",", ".");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
