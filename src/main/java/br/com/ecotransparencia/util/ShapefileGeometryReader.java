package br.com.ecotransparencia.util;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Geometry;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Le um shapefile e devolve um {@code Map<Integer, Geometry>} indexado pelo
 * atributo numerico fornecido (ex.: {@code vw_num_auto}, {@code vw_num_emb}).
 *
 * <p>Atributos do .dbf sao ignorados; este utilitario serve apenas como
 * fonte de geometria. Atributos completos vem do XLSX (estrategia hibrida).
 *
 * <p>Charset DBF forcado a Latin-1 (default em shapefiles do governo BR
 * sem .cpg). Como nao usamos os atributos textuais, isso so impacta a
 * leitura do nome do atributo-chave (que e' ASCII puro), entao e' seguro.
 */
public final class ShapefileGeometryReader {

    private ShapefileGeometryReader() {}

    /**
     * Le todas as geometrias do shapefile, indexadas pelo atributo-chave inteiro.
     */
    public static Map<Integer, Geometry> read(Path shp, String keyAttribute) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("url", shp.toUri().toURL());
        params.put("charset", StandardCharsets.ISO_8859_1.name());

        DataStore store = DataStoreFinder.getDataStore(params);
        if (store == null) {
            throw new IllegalStateException("Could not open shapefile data store: " + shp);
        }

        try {
            String typeName = store.getTypeNames()[0];
            SimpleFeatureSource source = store.getFeatureSource(typeName);
            Map<Integer, Geometry> result = new HashMap<>();
            try (FeatureIterator<SimpleFeature> it = source.getFeatures().features()) {
                while (it.hasNext()) {
                    SimpleFeature f = it.next();
                    Object key = f.getAttribute(keyAttribute);
                    if (key instanceof Number) {
                        Geometry geom = (Geometry) f.getDefaultGeometry();
                        if (geom != null) {
                            result.put(((Number) key).intValue(), geom);
                        }
                    }
                }
            }
            return result;
        } finally {
            store.dispose();
        }
    }
}
