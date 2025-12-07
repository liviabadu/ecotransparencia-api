package br.com.ecotransparencia.startup;

import br.com.ecotransparencia.entity.Embargo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de unidade para IbamaDataLoader.
 *
 * US-INFRA-001: Carregar base de dados do IBAMA no startup
 *
 * Seguindo TDD: RED -> GREEN -> REFACTOR
 */
class IbamaDataLoaderTest {

    private IbamaDataLoader loader;

    @BeforeEach
    void setUp() {
        loader = new IbamaDataLoader();
    }

    @Nested
    @DisplayName("Parsing de campos String")
    class ParseStringTests {

        @Test
        @DisplayName("Deve retornar valor quando string é válida")
        void shouldReturnValueWhenStringIsValid() {
            String[] fields = createFieldsWithValue(0, "TESTE");
            Embargo embargo = loader.parseEmbargo(fields);
            // SEQ_TAD é Long, testamos com NOME_PESSOA_EMBARGADA (índice 15)
            String[] fields2 = createFieldsWithValue(15, "João da Silva");
            Embargo embargo2 = loader.parseEmbargo(fields2);
            assertEquals("João da Silva", embargo2.getNomePessoaEmbargada());
        }

        @Test
        @DisplayName("Deve retornar null quando string está vazia")
        void shouldReturnNullWhenStringIsEmpty() {
            String[] fields = createFieldsWithValue(15, "");
            Embargo embargo = loader.parseEmbargo(fields);
            assertNull(embargo.getNomePessoaEmbargada());
        }

        @Test
        @DisplayName("Deve retornar null quando string contém apenas espaços")
        void shouldReturnNullWhenStringIsBlank() {
            String[] fields = createFieldsWithValue(15, "   ");
            Embargo embargo = loader.parseEmbargo(fields);
            assertNull(embargo.getNomePessoaEmbargada());
        }

        @Test
        @DisplayName("Deve remover aspas da string")
        void shouldRemoveQuotesFromString() {
            String[] fields = createFieldsWithValue(15, "\"Empresa Teste Ltda\"");
            Embargo embargo = loader.parseEmbargo(fields);
            assertEquals("Empresa Teste Ltda", embargo.getNomePessoaEmbargada());
        }
    }

    @Nested
    @DisplayName("Parsing de campos numéricos")
    class ParseNumericTests {

        @Test
        @DisplayName("Deve parsear Long corretamente")
        void shouldParseLongCorrectly() {
            String[] fields = createFieldsWithValue(0, "1829644");
            Embargo embargo = loader.parseEmbargo(fields);
            assertEquals(1829644L, embargo.getSeqTad());
        }

        @Test
        @DisplayName("Deve retornar embargo null para SEQ_TAD inválido (não numérico)")
        void shouldReturnNullForInvalidLong() {
            String[] fields = createFieldsWithValue(0, "abc");
            Embargo embargo = loader.parseEmbargo(fields);
            // SEQ_TAD é obrigatório, então embargo inteiro é null se SEQ_TAD é inválido
            assertNull(embargo, "Embargo deve ser null quando SEQ_TAD é inválido");
        }

        @Test
        @DisplayName("Deve parsear Integer corretamente")
        void shouldParseIntegerCorrectly() {
            String[] fields = createFieldsWithValue(34, "4");
            Embargo embargo = loader.parseEmbargo(fields);
            assertEquals(4, embargo.getCodTipoBioma());
        }

        @Test
        @DisplayName("Deve parsear BigDecimal com ponto")
        void shouldParseBigDecimalWithDot() {
            String[] fields = createFieldsWithValue(21, "10.5");
            Embargo embargo = loader.parseEmbargo(fields);
            assertEquals(new BigDecimal("10.5"), embargo.getQtdAreaEmbargada());
        }

        @Test
        @DisplayName("Deve parsear BigDecimal com vírgula (formato brasileiro)")
        void shouldParseBigDecimalWithComma() {
            String[] fields = createFieldsWithValue(21, "10,5000");
            Embargo embargo = loader.parseEmbargo(fields);
            assertEquals(new BigDecimal("10.5000"), embargo.getQtdAreaEmbargada());
        }

        @Test
        @DisplayName("Deve retornar null para BigDecimal inválido")
        void shouldReturnNullForInvalidBigDecimal() {
            String[] fields = createFieldsWithValue(21, "abc");
            Embargo embargo = loader.parseEmbargo(fields);
            assertNull(embargo.getQtdAreaEmbargada());
        }
    }

    @Nested
    @DisplayName("Parsing de campos de data")
    class ParseDateTests {

        @Test
        @DisplayName("Deve parsear data no formato dd/MM/yyyy HH:mm:ss")
        void shouldParseDateCorrectly() {
            String[] fields = createFieldsWithValue(3, "16/12/1987 15:40:00");
            Embargo embargo = loader.parseEmbargo(fields);

            LocalDateTime expected = LocalDateTime.of(1987, 12, 16, 15, 40, 0);
            assertEquals(expected, embargo.getDatEmbargo());
        }

        @Test
        @DisplayName("Deve parsear data com aspas")
        void shouldParseDateWithQuotes() {
            String[] fields = createFieldsWithValue(3, "\"03/08/1988 14:45:00\"");
            Embargo embargo = loader.parseEmbargo(fields);

            LocalDateTime expected = LocalDateTime.of(1988, 8, 3, 14, 45, 0);
            assertEquals(expected, embargo.getDatEmbargo());
        }

        @Test
        @DisplayName("Deve retornar null para data inválida")
        void shouldReturnNullForInvalidDate() {
            String[] fields = createFieldsWithValue(3, "data-invalida");
            Embargo embargo = loader.parseEmbargo(fields);
            assertNull(embargo.getDatEmbargo());
        }

        @Test
        @DisplayName("Deve retornar null para data vazia")
        void shouldReturnNullForEmptyDate() {
            String[] fields = createFieldsWithValue(3, "");
            Embargo embargo = loader.parseEmbargo(fields);
            assertNull(embargo.getDatEmbargo());
        }
    }

    @Nested
    @DisplayName("Validação de campos obrigatórios")
    class RequiredFieldsTests {

        @Test
        @DisplayName("Deve retornar null quando SEQ_TAD está vazio")
        void shouldReturnNullWhenSeqTadIsEmpty() {
            String[] fields = createFieldsWithValue(0, "");
            Embargo embargo = loader.parseEmbargo(fields);
            assertNull(embargo, "Embargo deve ser null quando SEQ_TAD está vazio");
        }

        @Test
        @DisplayName("Deve retornar null quando SEQ_TAD é null")
        void shouldReturnNullWhenSeqTadIsNull() {
            String[] fields = createFieldsWithValue(0, null);
            fields[0] = null;
            Embargo embargo = loader.parseEmbargo(fields);
            assertNull(embargo, "Embargo deve ser null quando SEQ_TAD é null");
        }

        @Test
        @DisplayName("Deve retornar embargo válido quando SEQ_TAD está presente")
        void shouldReturnValidEmbargoWhenSeqTadIsPresent() {
            String[] fields = createFieldsWithValue(0, "1829644");
            Embargo embargo = loader.parseEmbargo(fields);
            assertNotNull(embargo, "Embargo deve ser criado quando SEQ_TAD está presente");
            assertEquals(1829644L, embargo.getSeqTad());
        }
    }

    @Nested
    @DisplayName("Mapeamento completo de campos")
    class FieldMappingTests {

        @Test
        @DisplayName("Deve mapear todos os campos de identificação")
        void shouldMapIdentificationFields() {
            String[] fields = createRealCsvLine();
            Embargo embargo = loader.parseEmbargo(fields);

            assertEquals(1600976L, embargo.getSeqTad());
            assertEquals("278153", embargo.getNumTad());
            assertEquals("A", embargo.getSerTad());
        }

        @Test
        @DisplayName("Deve mapear campos de localização")
        void shouldMapLocationFields() {
            String[] fields = createRealCsvLine();
            Embargo embargo = loader.parseEmbargo(fields);

            assertEquals("MT", embargo.getSigUfTad());
            assertEquals("Apiacás", embargo.getNomMunicipioTad());
            assertEquals("Fazenda Mautra", embargo.getNomeImovel());
        }

        @Test
        @DisplayName("Deve mapear campos do embargado")
        void shouldMapEmbargadoFields() {
            String[] fields = createRealCsvLine();
            Embargo embargo = loader.parseEmbargo(fields);

            assertEquals("RAIMUNDO CIRINO DE SOUZA", embargo.getNomePessoaEmbargada());
            assertEquals("02330709234", embargo.getCpfCnpjEmbargado());
        }

        @Test
        @DisplayName("Deve mapear campos de caracterização")
        void shouldMapCharacterizationFields() {
            String[] fields = createRealCsvLine();
            Embargo embargo = loader.parseEmbargo(fields);

            assertEquals("D", embargo.getSitDesmatamento());
            assertEquals(new BigDecimal("10.0000"), embargo.getQtdAreaEmbargada());
        }

        @Test
        @DisplayName("Deve mapear campos de bioma")
        void shouldMapBiomeFields() {
            String[] fields = createRealCsvLine();
            Embargo embargo = loader.parseEmbargo(fields);

            assertEquals(4, embargo.getCodTipoBioma());
            assertEquals("Amazonia", embargo.getDesTipoBioma());
        }

        @Test
        @DisplayName("Deve mapear campos do auto de infração")
        void shouldMapAutoInfracaoFields() {
            String[] fields = createRealCsvLine();
            Embargo embargo = loader.parseEmbargo(fields);

            assertEquals("789209", embargo.getNumAutoInfracao());
            assertEquals("A", embargo.getSerAutoInfracao());
        }
    }

    // ===========================================
    // Helper methods
    // ===========================================

    /**
     * Cria um array de campos com valor padrão vazio,
     * exceto no índice especificado.
     * SEQ_TAD (índice 0) sempre terá um valor válido, a menos
     * que o índice especificado seja 0.
     */
    private String[] createFieldsWithValue(int index, String value) {
        String[] fields = new String[36];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = "";
        }
        // SEQ_TAD é obrigatório, então sempre defina um valor padrão
        // a menos que estejamos testando especificamente o SEQ_TAD
        if (index != 0) {
            fields[0] = "1";
        }
        fields[index] = value;
        return fields;
    }

    /**
     * Cria uma linha real do CSV para testes de mapeamento completo.
     * Baseado na linha 2 do arquivo areas_embargadas.csv
     */
    private String[] createRealCsvLine() {
        return new String[] {
            "1600976",                              // 0: SEQ_TAD
            "278153",                               // 1: NUM_TAD
            "A",                                    // 2: SER_TAD
            "03/08/1988 14:45:00",                  // 3: DAT_EMBARGO
            "22/09/2016 13:50:17",                  // 4: DAT_ULT_ALTERACAO
            "51",                                   // 5: COD_UF_TAD
            "MT",                                   // 6: SIG_UF_TAD
            "5100805",                              // 7: COD_MUNICIPIO_TAD
            "Apiacás",                              // 8: NOM_MUNICIPIO_TAD
            "0",                                    // 9: NUM_LONGITUDE_TAD
            "0",                                    // 10: NUM_LATITUDE_TAD
            "00° 00' 00.000'' W",                   // 11: NUM_LONGITUDE_GMS_TAD
            "00° 00' 00.000'' S",                   // 12: NUM_LATITUDE_GMS_TAD
            "Fazenda Mautra",                       // 13: NOME_IMOVEL
            "Fazenda Mautra, Estrada Mutum",        // 14: DES_LOCALIZACAO_TAD
            "RAIMUNDO CIRINO DE SOUZA",             // 15: NOME_PESSOA_EMBARGADA
            "02330709234",                          // 16: CPF_CNPJ_EMBARGADO
            "D",                                    // 17: SIT_DESMATAMENTO
            "",                                     // 18: TP_AREA_EMBARGADA
            "",                                     // 19: DS_OUTROS_TIPO_AREA
            "",                                     // 20: ST_AREA_DESMATADA_ILEGAL
            "10,0000",                              // 21: QTD_AREA_EMBARGADA
            "",                                     // 22: OPERACAO
            "Gerência Executiva do Ibama em Sinop/MT", // 23: UNID_IBAMA_CONTROLE
            "",                                     // 24: ORDEM_FISCALIZACAO
            "",                                     // 25: ACAO_FISCALIZATORIA
            "02013003267198812",                    // 26: NUM_PROCESSO
            "Por ter desmatado aproximadamente 10,00 ha de mata", // 27: DES_TAD
            "",                                     // 28: WKT_GEOM_AREA_EMBARGADA
            "06/09/2016 15:11:48",                  // 29: DAT_ULT_ALTER_GEOM
            "789209",                               // 30: NUM_AUTO_INFRACAO
            "A",                                    // 31: SER_AUTO_INFRACAO
            "",                                     // 32: QTD_AREA_DESMATADA
            "Infração da Flora(Não Classificada-Móvel)", // 33: DES_INFRACAO
            "4",                                    // 34: COD_TIPO_BIOMA
            "Amazonia"                              // 35: DES_TIPO_BIOMA
        };
    }
}
