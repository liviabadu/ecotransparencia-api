package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.DataLoadMarker;
import br.com.ecotransparencia.entity.IcmbioAutoInfracao;
import br.com.ecotransparencia.entity.IcmbioEmbargo;
import br.com.ecotransparencia.repository.DataLoadMarkerRepository;
import br.com.ecotransparencia.repository.IcmbioAutoInfracaoRepository;
import br.com.ecotransparencia.repository.IcmbioEmbargoRepository;
import br.com.ecotransparencia.util.DocumentoUtil;
import br.com.ecotransparencia.util.ShapefileGeometryReader;
import br.com.ecotransparencia.util.XlsxStreamReader;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loader hibrido do ICMBio.
 *
 * <p>Estrategia: atributos vem dos XLSX (Apache POI streaming, 40k+/14k+ linhas),
 * geometria vem dos shapefiles (GeoTools, EPSG:4674). Join por
 * {@code vw_num_auto} / {@code vw_num_emb} (PK natural em ambos formatos).
 *
 * <p>Idempotente via {@link DataLoadMarker} ({@code icmbio_auto_v1}, {@code icmbio_embargo_v1}).
 *
 * <p>Para linhas do XLSX sem geometria correspondente no SHP, persiste com
 * {@code geometria = null} e incrementa contador (logado).
 */
@ApplicationScoped
public class IcmbioLoader {

    static final String SOURCE_AUTOS = "icmbio_auto_v1";
    static final String SOURCE_EMBARGOS = "icmbio_embargo_v1";

    private static final int BATCH_SIZE = 500;

    /** Datas no XLSX podem vir como yyyy/MM/dd ou yyyy-MM-dd. */
    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .toFormatter();

    @ConfigProperty(name = "app.data.load-icmbio-on-startup", defaultValue = "false")
    boolean loadOnStartup;

    @ConfigProperty(name = "app.data.icmbio-autos-xlsx-path",
            defaultValue = "docs/icmbio/autos_infracao_icmbio.xlsx")
    String autosXlsxPath;

    @ConfigProperty(name = "app.data.icmbio-autos-shp-path",
            defaultValue = "docs/icmbio/autos_infracao_icmbio_shp/autos_infracao_icmbio.shp")
    String autosShpPath;

    @ConfigProperty(name = "app.data.icmbio-embargos-xlsx-path",
            defaultValue = "docs/icmbio/embargos_icmbio.xlsx")
    String embargosXlsxPath;

    @ConfigProperty(name = "app.data.icmbio-embargos-shp-path",
            defaultValue = "docs/icmbio/embargos_icmbio_shp/embargos_icmbio_shp.shp")
    String embargosShpPath;

    @Inject
    IcmbioAutoInfracaoRepository autoRepo;

    @Inject
    IcmbioEmbargoRepository embargoRepo;

    @Inject
    DataLoadMarkerRepository markerRepo;

    void onStart(@Observes StartupEvent event) {
        if (!loadOnStartup) {
            Log.info("ICMBio data loading disabled (app.data.load-icmbio-on-startup=false)");
            return;
        }
        loadAutos();
        loadEmbargos();
    }

    void loadAutos() {
        if (isAlreadyLoaded(SOURCE_AUTOS)) {
            Log.infof("Data already loaded for source=%s, skipping", SOURCE_AUTOS);
            return;
        }
        Path xlsx = Paths.get(autosXlsxPath);
        Path shp = Paths.get(autosShpPath);
        if (!Files.exists(xlsx)) {
            Log.warnf("ICMBio autos XLSX not found: %s", xlsx);
            return;
        }

        long start = System.currentTimeMillis();
        Map<Integer, Geometry> geometries = readShapefile(shp, "vw_num_auto");
        Log.infof("ICMBio autos: %d geometrias lidas do SHP", geometries.size());

        int[] counters = {0, 0, 0}; // inserted, withGeom, withoutGeom
        List<IcmbioAutoInfracao> batch = new ArrayList<>(BATCH_SIZE);

        try {
            XlsxStreamReader.read(xlsx, row -> {
                try {
                    IcmbioAutoInfracao e = parseAuto(row);
                    if (e == null || e.getVwNumAuto() == null) return;
                    Geometry g = geometries.get(e.getVwNumAuto());
                    if (g instanceof Point p) {
                        e.setLocalizacao(p);
                        counters[1]++;
                    } else if (g != null && g.getNumPoints() > 0) {
                        // Geometria nao-Point (ex.: MultiPoint) -> pega 1o ponto
                        e.setLocalizacao(g.getFactory().createPoint(g.getCoordinate()));
                        counters[1]++;
                    } else {
                        counters[2]++;
                    }
                    batch.add(e);
                    if (batch.size() >= BATCH_SIZE) {
                        counters[0] += persistAutoBatch(batch);
                        batch.clear();
                    }
                } catch (Exception ex) {
                    Log.warnf("Error parsing autos row: %s", ex.getMessage());
                }
            });
        } catch (Exception ex) {
            Log.errorf(ex, "Error streaming ICMBio autos XLSX");
            return;
        }

        if (!batch.isEmpty()) {
            counters[0] += persistAutoBatch(batch);
        }

        long elapsed = System.currentTimeMillis() - start;
        Log.infof("ICMBio autos load: inserted=%d withGeom=%d withoutGeom=%d in %.1fs",
                counters[0], counters[1], counters[2], elapsed / 1000.0);

        markLoaded(SOURCE_AUTOS);
    }

    void loadEmbargos() {
        if (isAlreadyLoaded(SOURCE_EMBARGOS)) {
            Log.infof("Data already loaded for source=%s, skipping", SOURCE_EMBARGOS);
            return;
        }
        Path xlsx = Paths.get(embargosXlsxPath);
        Path shp = Paths.get(embargosShpPath);
        if (!Files.exists(xlsx)) {
            Log.warnf("ICMBio embargos XLSX not found: %s", xlsx);
            return;
        }

        long start = System.currentTimeMillis();
        Map<Integer, Geometry> geometries = readShapefile(shp, "vw_num_emb");
        Log.infof("ICMBio embargos: %d geometrias lidas do SHP", geometries.size());

        int[] counters = {0, 0, 0};
        List<IcmbioEmbargo> batch = new ArrayList<>(BATCH_SIZE);

        try {
            XlsxStreamReader.read(xlsx, row -> {
                try {
                    IcmbioEmbargo e = parseEmbargo(row);
                    if (e == null || e.getVwNumEmb() == null) return;
                    Geometry g = geometries.get(e.getVwNumEmb());
                    if (g != null) {
                        e.setGeometria(g);
                        counters[1]++;
                    } else {
                        counters[2]++;
                    }
                    batch.add(e);
                    if (batch.size() >= BATCH_SIZE) {
                        counters[0] += persistEmbargoBatch(batch);
                        batch.clear();
                    }
                } catch (Exception ex) {
                    Log.warnf("Error parsing embargos row: %s", ex.getMessage());
                }
            });
        } catch (Exception ex) {
            Log.errorf(ex, "Error streaming ICMBio embargos XLSX");
            return;
        }

        if (!batch.isEmpty()) {
            counters[0] += persistEmbargoBatch(batch);
        }

        long elapsed = System.currentTimeMillis() - start;
        Log.infof("ICMBio embargos load: inserted=%d withGeom=%d withoutGeom=%d in %.1fs",
                counters[0], counters[1], counters[2], elapsed / 1000.0);

        markLoaded(SOURCE_EMBARGOS);
    }

    boolean isAlreadyLoaded(String source) {
        return markerRepo.findById(source) != null;
    }

    @Transactional
    void markLoaded(String source) {
        DataLoadMarker marker = new DataLoadMarker();
        marker.setSource(source);
        LocalDateTime now = LocalDateTime.now();
        marker.setLoadedAt(now);
        marker.setVersion(now.toString());
        markerRepo.persist(marker);
    }

    /** Le shapefile retornando map vazio em caso de erro (geometria e' opcional). */
    Map<Integer, Geometry> readShapefile(Path shp, String keyAttribute) {
        if (!Files.exists(shp)) {
            Log.warnf("Shapefile not found: %s (geometry will be null)", shp);
            return Map.of();
        }
        try {
            return ShapefileGeometryReader.read(shp, keyAttribute);
        } catch (Exception e) {
            Log.errorf(e, "Error reading shapefile: %s", shp);
            return Map.of();
        }
    }

    @Transactional
    int persistAutoBatch(List<IcmbioAutoInfracao> batch) {
        for (IcmbioAutoInfracao e : batch) autoRepo.persist(e);
        autoRepo.flush();
        return batch.size();
    }

    @Transactional
    int persistEmbargoBatch(List<IcmbioEmbargo> batch) {
        for (IcmbioEmbargo e : batch) embargoRepo.persist(e);
        embargoRepo.flush();
        return batch.size();
    }

    // ----------------- parsers -----------------

    static IcmbioAutoInfracao parseAuto(Map<String, String> row) {
        IcmbioAutoInfracao e = new IcmbioAutoInfracao();
        e.setVwNumAuto(parseInt(row, "vw_num_auto"));
        if (e.getVwNumAuto() == null) return null;
        e.setNumeroAi(get(row, "numero_ai"));
        e.setSerie(get(row, "serie"));
        e.setOrigem(get(row, "origem"));
        e.setTipo(get(row, "tipo"));
        e.setValorMulta(parseDecimal(row, "valor_mult"));
        e.setEmbargo(get(row, "embargo"));
        e.setApreensao(get(row, "apreensao"));
        e.setAutuado(get(row, "autuado"));
        String cpfCnpjRaw = get(row, "cpf_cnpj");
        e.setCpfCnpjFormatado(cpfCnpjRaw);
        e.setCpfCnpj(DocumentoUtil.limpar(cpfCnpjRaw));
        e.setDescAi(get(row, "desc_ai"));
        e.setDescSanc(get(row, "desc_sanc"));
        e.setData(parseDate(row, "data"));
        e.setAno(parseInt(row, "ano"));
        e.setArtigo1(get(row, "artigo_1"));
        e.setArtigo2(get(row, "artigo_2"));
        e.setTipoInfra(get(row, "tipo_infra"));
        e.setNomeUc(get(row, "nome_uc"));
        e.setCnuc(get(row, "cnuc"));
        e.setMunicipio(get(row, "municipio"));
        e.setUf(get(row, "uf"));
        e.setTermosEmb(get(row, "termos_emb"));
        e.setTermosApr(get(row, "termos_apr"));
        e.setOrdemFisc(get(row, "ordem_fisc"));
        e.setProcesso(get(row, "processo"));
        e.setJulgamento(get(row, "julgamento"));
        return e;
    }

    static IcmbioEmbargo parseEmbargo(Map<String, String> row) {
        IcmbioEmbargo e = new IcmbioEmbargo();
        e.setVwNumEmb(parseInt(row, "vw_num_emb"));
        if (e.getVwNumEmb() == null) return null;
        e.setNumeroEmb(get(row, "numero_emb"));
        e.setSerie(get(row, "serie"));
        e.setOrigem(get(row, "origem"));
        e.setNumeroAi(get(row, "numero_ai"));
        String cpfCnpjRaw = get(row, "cpf_cnpj");
        e.setCpfCnpjFormatado(cpfCnpjRaw);
        e.setCpfCnpj(DocumentoUtil.limpar(cpfCnpjRaw));
        e.setAutuado(get(row, "autuado"));
        e.setDescInfra(get(row, "desc_infra"));
        e.setDescSanc(get(row, "desc_sanc"));
        e.setArtigo1(get(row, "artigo_1"));
        e.setArtigo2(get(row, "artigo_2"));
        e.setTipoInfra(get(row, "tipo_infra"));
        e.setNomeUc(get(row, "nome_uc"));
        e.setCnuc(get(row, "cnuc"));
        e.setMunicipio(get(row, "municipio"));
        e.setUf(get(row, "uf"));
        e.setData(parseDate(row, "data"));
        e.setAno(parseInt(row, "ano"));
        e.setObs(get(row, "obs"));
        e.setJulgamento(get(row, "julgamento"));
        e.setArea(parseDecimal(row, "area"));
        e.setProcesso(get(row, "processo"));
        return e;
    }

    private static String get(Map<String, String> row, String key) {
        String v = row.get(key);
        if (v == null) return null;
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    private static Integer parseInt(Map<String, String> row, String key) {
        String v = get(row, key);
        if (v == null) return null;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            // POI sometimes formats integers as "123.0"
            try {
                return (int) Double.parseDouble(v);
            } catch (NumberFormatException e2) {
                return null;
            }
        }
    }

    private static BigDecimal parseDecimal(Map<String, String> row, String key) {
        String v = get(row, key);
        if (v == null) return null;
        // pt-BR: "1.234,56" -> "1234.56"
        String normalized = v.replace(".", "").replace(",", ".");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            // Maybe POI gave us en format already
            try { return new BigDecimal(v); }
            catch (NumberFormatException e2) { return null; }
        }
    }

    private static LocalDate parseDate(Map<String, String> row, String key) {
        String v = get(row, key);
        if (v == null) return null;
        try {
            return LocalDate.parse(v, DATE_FORMATTER);
        } catch (Exception e) {
            return null;
        }
    }
}
