package br.com.ecotransparencia.service;

import br.com.ecotransparencia.dto.SituacaoCadastralDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementacao stub do servico de consulta na Receita Federal.
 * Sempre retorna que o documento e valido.
 *
 * TODO: Substituir por implementacao real quando disponivel integracao com RF.
 */
@ApplicationScoped
public class ReceitaFederalServiceStub implements ReceitaFederalService {

    private static final Logger LOG = Logger.getLogger(ReceitaFederalServiceStub.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public SituacaoCadastralDto consultarCnpj(String cnpj) {
        LOG.debugf("Consultando CNPJ (stub): %s", mascararDocumento(cnpj));

        SituacaoCadastralDto resultado = SituacaoCadastralDto.valido(
            "ATIVA",
            "Cadastro ativo na Receita Federal (stub)"
        );
        resultado.setDataConsulta(agora());

        return resultado;
    }

    @Override
    public SituacaoCadastralDto consultarCpf(String cpf) {
        LOG.debugf("Consultando CPF (stub): %s", mascararDocumento(cpf));

        SituacaoCadastralDto resultado = SituacaoCadastralDto.valido(
            "REGULAR",
            "Situacao cadastral regular na Receita Federal (stub)"
        );
        resultado.setDataConsulta(agora());

        return resultado;
    }

    private String agora() {
        return LocalDateTime.now().format(ISO_FORMATTER) + ".000Z";
    }

    private String mascararDocumento(String documento) {
        if (documento == null || documento.length() < 4) {
            return "***";
        }
        return documento.substring(0, 3) + "***" + documento.substring(documento.length() - 2);
    }
}
