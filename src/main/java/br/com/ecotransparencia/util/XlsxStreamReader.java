package br.com.ecotransparencia.util;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Leitor streaming de XLSX usando o event model do Apache POI (SAX).
 *
 * <p>Para arquivos grandes (40k+ linhas), evita carregar o workbook inteiro
 * em memoria. A primeira linha (linha 1) e' tratada como cabecalho e usada
 * para mapear colunas. Cada linha subsequente e' entregue ao consumer
 * como Map<headerName, cellValueAsString>.
 *
 * <p>Cells vazias sao omitidas do mapa (consumer deve usar
 * {@link Map#getOrDefault} ou {@link Map#get} com null check).
 */
public final class XlsxStreamReader {

    private XlsxStreamReader() {}

    /**
     * Le um xlsx em streaming, invocando {@code rowConsumer} uma vez por linha de dados.
     */
    public static void read(Path xlsx, Consumer<Map<String, String>> rowConsumer) throws Exception {
        try (OPCPackage pkg = OPCPackage.open(xlsx.toFile())) {
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            try (InputStream sheetStream = reader.getSheetsData().next()) {
                XMLReader parser = XMLHelper.newXMLReader();
                SheetHandler handler = new SheetHandler(rowConsumer);
                parser.setContentHandler(new XSSFSheetXMLHandler(styles, strings, handler, false));
                parser.parse(new InputSource(sheetStream));
            }
        }
    }

    /**
     * Handler interno: primeira linha e' header; cada linha subsequente
     * vira um Map<String, String> entregue ao consumer.
     */
    private static final class SheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

        private final Consumer<Map<String, String>> rowConsumer;
        private final Map<String, String> headerByColumn = new HashMap<>();
        private Map<String, String> currentRow;
        private int currentRowNum = -1;
        private boolean isHeaderRow = true;

        SheetHandler(Consumer<Map<String, String>> rowConsumer) {
            this.rowConsumer = rowConsumer;
        }

        @Override
        public void startRow(int rowNum) {
            this.currentRowNum = rowNum;
            this.isHeaderRow = (rowNum == 0);
            if (!isHeaderRow) {
                this.currentRow = new LinkedHashMap<>();
            }
        }

        @Override
        public void endRow(int rowNum) {
            if (!isHeaderRow && currentRow != null && !currentRow.isEmpty()) {
                rowConsumer.accept(currentRow);
            }
            currentRow = null;
        }

        @Override
        public void cell(String cellReference, String formattedValue, org.apache.poi.xssf.usermodel.XSSFComment comment) {
            // Extrai a letra da coluna de "A1", "B12", "AA205" -> "A", "B", "AA"
            String colLetter = extractColumnLetter(cellReference);
            if (colLetter == null) return;

            if (isHeaderRow) {
                if (formattedValue != null) {
                    headerByColumn.put(colLetter, formattedValue.trim());
                }
            } else {
                String header = headerByColumn.get(colLetter);
                if (header != null && formattedValue != null && !formattedValue.isEmpty()) {
                    currentRow.put(header, formattedValue);
                }
            }
        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            // Ignored
        }

        private static String extractColumnLetter(String cellReference) {
            if (cellReference == null || cellReference.isEmpty()) return null;
            int i = 0;
            while (i < cellReference.length() && Character.isLetter(cellReference.charAt(i))) {
                i++;
            }
            return i > 0 ? cellReference.substring(0, i) : null;
        }
    }
}
