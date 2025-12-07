package br.com.ecotransparencia.service;

import br.com.ecotransparencia.dto.EntityDto;
import br.com.ecotransparencia.dto.OccurrenceDto;
import br.com.ecotransparencia.dto.SearchResponse;
import br.com.ecotransparencia.entity.Embargo;
import br.com.ecotransparencia.repository.EmbargoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class SearchService {

    private static final String SOURCE = "IBAMA";
    private static final String SOURCE_URL = "https://ibama.gov.br/consulta/";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Inject
    EmbargoRepository embargoRepository;

    public SearchResponse searchByDocument(String document, String type) {
        List<Embargo> embargos = embargoRepository.findByDocument(document);

        if (embargos.isEmpty()) {
            return SearchResponse.notFound();
        }

        EntityDto entity = buildEntityDto(embargos, document, type);
        return SearchResponse.found(entity);
    }

    private EntityDto buildEntityDto(List<Embargo> embargos, String document, String type) {
        Embargo first = embargos.get(0);

        EntityDto entity = new EntityDto();
        entity.setId(String.valueOf(first.getSeqTad()));
        entity.setName(first.getNomePessoaEmbargada());
        entity.setDocument(document);
        entity.setDocumentType(type);

        List<OccurrenceDto> occurrences = embargos.stream()
                .map(this::toOccurrenceDto)
                .toList();

        entity.setOccurrences(occurrences);

        int score = calculateRiskScore(embargos);
        entity.setScore(score);
        entity.setRiskLevel(classifyRiskLevel(score));

        return entity;
    }

    private OccurrenceDto toOccurrenceDto(Embargo embargo) {
        OccurrenceDto dto = new OccurrenceDto();
        dto.setId("occ-" + embargo.getSeqTad());
        dto.setCategory(embargo.getTpAreaEmbargada());
        dto.setDate(formatDate(embargo));
        dto.setDescription(embargo.getDesTad());
        dto.setSource(SOURCE);
        dto.setSourceUrl(SOURCE_URL + embargo.getNumAutoInfracao());
        dto.setStatus(determineStatus(embargo));
        return dto;
    }

    private String formatDate(Embargo embargo) {
        if (embargo.getDatEmbargo() == null) {
            return null;
        }
        return embargo.getDatEmbargo().format(ISO_FORMATTER) + ".000Z";
    }

    private String determineStatus(Embargo embargo) {
        // Simple logic: if updated recently, consider "Ativo", otherwise "Baixado"
        // This can be enhanced with actual business rules
        return "Baixado";
    }

    int calculateRiskScore(List<Embargo> embargos) {
        int score = 0;

        // +10 per embargo
        score += embargos.size() * 10;

        // +5 if deforestation related
        for (Embargo embargo : embargos) {
            if ("D".equals(embargo.getSitDesmatamento())) {
                score += 5;
            }
        }

        // Cap at 100
        return Math.min(score, 100);
    }

    String classifyRiskLevel(int score) {
        if (score >= 80) {
            return "Crítico";
        } else if (score >= 51) {
            return "Alto";
        } else if (score >= 26) {
            return "Médio";
        } else {
            return "Baixo";
        }
    }
}
