package br.com.ecotransparencia.service;

import br.com.ecotransparencia.dto.*;
import br.com.ecotransparencia.entity.AutoInfracao;
import br.com.ecotransparencia.entity.Cepim;
import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.entity.SancaoAdmPublica;
import br.com.ecotransparencia.entity.TrabalhoEscravoMte;
import br.com.ecotransparencia.repository.AutoInfracaoRepository;
import br.com.ecotransparencia.repository.CepimRepository;
import br.com.ecotransparencia.repository.EmbargoRepository;
import br.com.ecotransparencia.repository.SancaoAdmPublicaRepository;
import br.com.ecotransparencia.repository.TrabalhoEscravoMteRepository;
import br.com.ecotransparencia.util.DocumentoUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servico de busca que agrega dados de multiplas fontes e calcula o Score ASG.
 */
@ApplicationScoped
public class SearchService {

    private static final String SOURCE_IBAMA = "IBAMA";
    private static final String SOURCE_URL_EMBARGO = "https://servicos.ibama.gov.br/ctf/publico/areasembargadas/ConsultaPublicaAreasEmbargadas.php";
    private static final String SOURCE_URL_AUTO = "https://servicos.ibama.gov.br/ctf/publico/autoinfracao/";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Inject
    EmbargoRepository embargoRepository;

    @Inject
    AutoInfracaoRepository autoInfracaoRepository;

    @Inject
    SancaoAdmPublicaRepository sancaoAdmPublicaRepository;

    @Inject
    CepimRepository cepimRepository;

    @Inject
    TrabalhoEscravoMteRepository trabalhoEscravoMteRepository;

    @Inject
    AsgScoreCalculator asgScoreCalculator;

    @Inject
    ReceitaFederalService receitaFederalService;

    public SearchResponse searchByDocument(String document, String type) {
        // US-007: Validar situacao cadastral do CNPJ antes de prosseguir
        if ("cnpj".equalsIgnoreCase(type)) {
            SituacaoCadastralDto situacao = receitaFederalService.consultarCnpj(document);

            // Bloqueia se CNPJ nao esta ATIVO (ou se houve erro na consulta)
            if (!isSituacaoPermitida(situacao)) {
                return SearchResponse.bloqueadoPorSituacaoCadastral(situacao);
            }
        }

        // Busca IBAMA (Fase A) com o documento como veio do request
        List<Embargo> embargos = embargoRepository.findByDocument(document);
        List<AutoInfracao> autosInfracao = autoInfracaoRepository.findByDocument(document);

        // Busca fontes Fase B (CEIS/CNEP/CEPIM/MTE) com o documento normalizado
        // (digit-only via DocumentoUtil; estas tabelas guardam apenas digitos).
        String normalized = DocumentoUtil.limpar(document);
        List<SancaoAdmPublica> sancoes = sancaoAdmPublicaRepository.findByCpfCnpj(normalized);
        List<Cepim> cepim = cepimRepository.findByCpfCnpj(normalized);
        List<TrabalhoEscravoMte> mte = trabalhoEscravoMteRepository.findByCpfCnpj(normalized);

        boolean noOccurrences = embargos.isEmpty()
                && autosInfracao.isEmpty()
                && sancoes.isEmpty()
                && cepim.isEmpty()
                && mte.isEmpty();
        if (noOccurrences) {
            return SearchResponse.notFound();
        }

        SearchResponse response;
        if (!embargos.isEmpty() || !autosInfracao.isEmpty()) {
            EntityDto entity = buildEntityDto(embargos, autosInfracao, sancoes, cepim, mte, document, type);
            response = SearchResponse.found(entity);
        } else {
            // Apenas ocorrencias Fase B; nao ha EntityDto agregado por enquanto.
            response = new SearchResponse(true);
        }

        response.setSancoesAdmPublica(sancoes.stream().map(this::toSancaoOccurrence).toList());
        response.setImpedimentosCepim(cepim.stream().map(this::toCepimOccurrence).toList());
        response.setTrabalhoEscravo(mte.stream().map(this::toMteOccurrence).toList());

        return response;
    }

    /**
     * Verifica se a situacao cadastral permite prosseguir com a analise (US-007).
     * Apenas ATIVA e permitida para CNPJ.
     */
    private boolean isSituacaoPermitida(SituacaoCadastralDto situacao) {
        if (situacao == null || !situacao.isValido()) {
            return false;
        }
        // Apenas "ATIVA" permite prosseguir
        return "ATIVA".equalsIgnoreCase(situacao.getSituacao());
    }

    public SearchResponse searchByName(String name) {
        // Busca em todas as fontes
        List<Embargo> embargos = embargoRepository.findByNameContaining(name);
        List<AutoInfracao> autosInfracao = autoInfracaoRepository.findByNameContaining(name);

        if (embargos.isEmpty() && autosInfracao.isEmpty()) {
            return SearchResponse.notFound();
        }

        // Infere documento do primeiro resultado encontrado
        String document = null;
        String documentType = "unknown";

        if (!embargos.isEmpty()) {
            document = embargos.get(0).getCpfCnpjEmbargado();
        } else if (!autosInfracao.isEmpty()) {
            document = autosInfracao.get(0).getCpfCnpjInfrator();
        }

        if (document != null) {
            documentType = inferDocumentType(document);
        }

        EntityDto entity = buildEntityDto(embargos, autosInfracao, document, documentType);
        return SearchResponse.found(entity);
    }

    String inferDocumentType(String document) {
        if (document == null) {
            return "unknown";
        }
        return switch (document.length()) {
            case 11 -> "cpf";
            case 14 -> "cnpj";
            default -> "unknown";
        };
    }

    private EntityDto buildEntityDto(List<Embargo> embargos, List<AutoInfracao> autosInfracao,
                                     String document, String type) {
        return buildEntityDto(embargos, autosInfracao,
                java.util.Collections.emptyList(),
                java.util.Collections.emptyList(),
                java.util.Collections.emptyList(),
                document, type);
    }

    private EntityDto buildEntityDto(List<Embargo> embargos, List<AutoInfracao> autosInfracao,
                                     List<SancaoAdmPublica> sancoes,
                                     List<Cepim> cepimList,
                                     List<TrabalhoEscravoMte> mteList,
                                     String document, String type) {
        EntityDto entity = new EntityDto();

        // Define ID e nome do primeiro registro encontrado
        if (!embargos.isEmpty()) {
            Embargo first = embargos.get(0);
            entity.setId(String.valueOf(first.getSeqTad()));
            entity.setName(first.getNomePessoaEmbargada());
        } else if (!autosInfracao.isEmpty()) {
            AutoInfracao first = autosInfracao.get(0);
            entity.setId(String.valueOf(first.getSeqAutoInfracao()));
            entity.setName(first.getNomeInfrator());
        }

        entity.setDocument(document);
        entity.setDocumentType(type);

        // Consulta situacao cadastral na Receita Federal
        if (document != null) {
            entity.setSituacaoCadastral(receitaFederalService.consultar(document));
        }

        // Calcula Score ASG (inclui Fase B: CEIS/CNEP/CEPIM/MTE)
        AsgScoreDto asgScore = asgScoreCalculator.calculate(embargos, autosInfracao, sancoes, cepimList, mteList);
        entity.setAsgScore(asgScore);
        entity.setScore(asgScore.getScore());
        entity.setRiskLevel(asgScore.getRiskLevel());

        // Monta ocorrencias agrupadas
        OcorrenciasAgrupadasDto ocorrencias = new OcorrenciasAgrupadasDto();

        List<OccurrenceDto> embargosDtos = embargos.stream()
                .map(this::toOccurrenceDto)
                .toList();
        ocorrencias.setEmbargos(embargosDtos);

        List<AutoInfracaoDto> autosDtos = autosInfracao.stream()
                .map(this::toAutoInfracaoDto)
                .toList();
        ocorrencias.setAutosInfracao(autosDtos);

        entity.setOcorrencias(ocorrencias);

        // Retrocompatibilidade: mantem lista de embargos no campo antigo
        entity.setOccurrences(embargosDtos);

        return entity;
    }

    private OccurrenceDto toOccurrenceDto(Embargo embargo) {
        OccurrenceDto dto = new OccurrenceDto();
        dto.setId("emb-" + embargo.getSeqTad());
        dto.setCategory(embargo.getTpAreaEmbargada());
        dto.setDate(formatEmbargoDate(embargo));
        dto.setDescription(embargo.getDesTad());
        dto.setSource(SOURCE_IBAMA);
        dto.setSourceUrl(SOURCE_URL_EMBARGO);
        dto.setStatus(determineEmbargoStatus(embargo));

        dto.setAutoInfracao(formatAutoInfracaoEmbargo(embargo));
        dto.setDesmatamento("D".equals(embargo.getSitDesmatamento()));
        dto.setAreaEmbargada(embargo.getQtdAreaEmbargada());
        dto.setBiome(embargo.getDesTipoBioma());
        dto.setLocation(buildEmbargoLocation(embargo));

        return dto;
    }

    private AutoInfracaoDto toAutoInfracaoDto(AutoInfracao auto) {
        AutoInfracaoDto dto = new AutoInfracaoDto();
        dto.setId("auto-" + auto.getSeqAutoInfracao());
        dto.setNumeroAuto(auto.getNumAutoInfracao());
        dto.setData(formatAutoDate(auto));
        dto.setDescricao(auto.getDescricaoAutoInfracao());
        dto.setTipoInfracao(auto.getTipoInfracao());
        dto.setValorMulta(auto.getValorAutoInfracao());
        dto.setStatus(auto.getStatusFormulario());
        dto.setGravidade(auto.getGravidadeInfracao());
        dto.setMotivacaoConduta(auto.getMotivacaoConduta());
        dto.setEfeitoMeioAmbiente(auto.getEfeitoMeioAmbiente());
        dto.setBiomasAtingidos(auto.getBiomasAtingidos());
        dto.setEnquadramentoLegal(auto.getEnquadramentoAdministrativo());
        dto.setSource(SOURCE_IBAMA);
        dto.setLocation(buildAutoLocation(auto));

        return dto;
    }

    private String formatAutoInfracaoEmbargo(Embargo embargo) {
        String num = embargo.getNumAutoInfracao();
        String ser = embargo.getSerAutoInfracao();
        if (num == null) return null;
        if (ser == null || ser.isEmpty()) return num;
        return num + "-" + ser;
    }

    private LocationDto buildEmbargoLocation(Embargo embargo) {
        if (embargo.getSigUfTad() == null &&
            embargo.getNomMunicipioTad() == null &&
            embargo.getNomeImovel() == null) {
            return null;
        }
        LocationDto location = new LocationDto();
        location.setUf(embargo.getSigUfTad());
        location.setMunicipio(embargo.getNomMunicipioTad());
        location.setImovel(embargo.getNomeImovel());
        return location;
    }

    private LocationDto buildAutoLocation(AutoInfracao auto) {
        if (auto.getUf() == null && auto.getMunicipio() == null) {
            return null;
        }
        LocationDto location = new LocationDto();
        location.setUf(auto.getUf());
        location.setMunicipio(auto.getMunicipio());
        return location;
    }

    private String formatEmbargoDate(Embargo embargo) {
        if (embargo.getDatEmbargo() == null) {
            return null;
        }
        return embargo.getDatEmbargo().format(ISO_FORMATTER) + ".000Z";
    }

    private String formatAutoDate(AutoInfracao auto) {
        if (auto.getDataHoraAutoInfracao() == null) {
            return null;
        }
        return auto.getDataHoraAutoInfracao().format(ISO_FORMATTER) + ".000Z";
    }

    private String determineEmbargoStatus(Embargo embargo) {
        return embargo.isBaixado() ? "Baixado" : "Ativo";
    }

    // ---------------------------------------------------------------
    // Converters Fase B
    // ---------------------------------------------------------------

    private SancaoAdmPublicaOccurrence toSancaoOccurrence(SancaoAdmPublica s) {
        SancaoAdmPublicaOccurrence dto = new SancaoAdmPublicaOccurrence();
        dto.setCadastro(s.getCadastro());
        dto.setCodigoSancao(s.getCodigoSancao());
        dto.setNomeSancionado(s.getNomeSancionado());
        dto.setCategoriaSancao(s.getCategoriaSancao());
        dto.setValorMulta(s.getValorMulta());
        dto.setDataInicioSancao(s.getDataInicioSancao());
        dto.setDataFimSancao(s.getDataFimSancao());
        dto.setOrgaoSancionador(s.getOrgaoSancionador());
        dto.setUfOrgao(s.getUfOrgao());
        dto.setEsferaOrgao(s.getEsferaOrgao());
        dto.setFundamentacaoLegal(s.getFundamentacaoLegal());
        return dto;
    }

    private CepimOccurrence toCepimOccurrence(Cepim c) {
        CepimOccurrence dto = new CepimOccurrence();
        dto.setCnpjEntidade(c.getCnpjEntidade());
        dto.setNomeEntidade(c.getNomeEntidade());
        dto.setNumeroConvenio(c.getNumeroConvenio());
        dto.setOrgaoConcedente(c.getOrgaoConcedente());
        dto.setMotivoImpedimento(c.getMotivoImpedimento());
        return dto;
    }

    private TrabalhoEscravoOccurrence toMteOccurrence(TrabalhoEscravoMte m) {
        TrabalhoEscravoOccurrence dto = new TrabalhoEscravoOccurrence();
        dto.setAnoAcaoFiscal(m.getAnoAcaoFiscal());
        dto.setUf(m.getUf());
        dto.setEmpregador(m.getEmpregador());
        dto.setCpfCnpjFormatado(m.getCpfCnpjFormatado());
        dto.setEstabelecimento(m.getEstabelecimento());
        dto.setTrabalhadoresEnvolvidos(m.getTrabalhadoresEnvolvidos());
        dto.setCnae(m.getCnae());
        dto.setDecisaoAdmProcedencia(m.getDecisaoAdmProcedencia());
        dto.setInclusaoCadastroEmpregadores(m.getInclusaoCadastroEmpregadores());
        return dto;
    }

    // Metodos legados para retrocompatibilidade
    @Deprecated
    int calculateRiskScore(List<Embargo> embargos) {
        return asgScoreCalculator.calculateEmbargoScore(embargos);
    }

    @Deprecated
    String classifyRiskLevel(int score) {
        return asgScoreCalculator.classifyRiskLevel(score);
    }
}
