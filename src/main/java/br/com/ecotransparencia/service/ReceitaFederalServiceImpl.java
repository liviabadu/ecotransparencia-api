package br.com.ecotransparencia.service;

import br.com.ecotransparencia.client.CnpjaApiClient;
import br.com.ecotransparencia.client.CnpjaApiResponse;
import br.com.ecotransparencia.dto.SituacaoCadastralDto;
import br.com.ecotransparencia.util.DocumentoUtil;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementacao real do servico de consulta na Receita Federal.
 * Utiliza a CNPJA API Open para consultar situacao cadastral de CNPJ.
 * Documentacao: https://cnpja.com/api/open
 *
 * Para CPF, apenas valida o formato (digitos verificadores) pois nao ha API gratuita disponivel.
 */
@ApplicationScoped
@IfBuildProperty(name = "ecotransparencia.receita-federal.use-real-api", stringValue = "true", enableIfMissing = false)
public class ReceitaFederalServiceImpl implements ReceitaFederalService {

    private static final Logger LOG = Logger.getLogger(ReceitaFederalServiceImpl.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Inject
    @RestClient
    CnpjaApiClient cnpjaApiClient;

    @Override
    public SituacaoCadastralDto consultarCnpj(String cnpj) {
        LOG.infof("Consultando CNPJ na CNPJA API: %s", mascararDocumento(cnpj));

        // Valida formato do CNPJ primeiro
        if (!DocumentoUtil.validarCnpj(cnpj)) {
            LOG.warnf("CNPJ com formato invalido: %s", mascararDocumento(cnpj));
            return criarResultado(false, "INVALIDO",
                "CNPJ com formato invalido (digitos verificadores incorretos)");
        }

        try {
            CnpjaApiResponse response = cnpjaApiClient.consultarCnpj(cnpj);

            String situacao = response.getDescricaoSituacaoCadastral();
            boolean ativa = response.isAtiva();

            String mensagem = ativa
                ? "Cadastro ativo na Receita Federal"
                : String.format("CNPJ com situacao '%s' na Receita Federal. Analise ASG nao disponivel para empresas inativas.", situacao);

            LOG.infof("CNPJ %s - Situacao: %s (ativa=%s)", mascararDocumento(cnpj), situacao, ativa);

            return criarResultado(true, situacao, mensagem);

        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            LOG.errorf("Erro HTTP %d ao consultar CNPJ %s: %s", status, mascararDocumento(cnpj), e.getMessage());

            return tratarErroHttp(status, cnpj);

        } catch (jakarta.ws.rs.ProcessingException e) {
            // ProcessingException engloba timeout, conexao recusada, etc
            String mensagemErro = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            LOG.errorf("Erro de processamento ao consultar CNPJ %s: %s", mascararDocumento(cnpj), mensagemErro);

            if (mensagemErro != null && mensagemErro.toLowerCase().contains("timeout")) {
                return criarErroConsulta(
                    "Tempo limite excedido ao consultar a Receita Federal. Tente novamente mais tarde.",
                    408
                );
            }

            if (mensagemErro != null && (mensagemErro.toLowerCase().contains("connect") ||
                                         mensagemErro.toLowerCase().contains("refused"))) {
                return criarErroConsulta(
                    "Nao foi possivel conectar ao servico da Receita Federal. Verifique sua conexao.",
                    503
                );
            }

            return criarErroConsulta(
                "Erro de comunicacao com a Receita Federal. Tente novamente mais tarde.",
                503
            );

        } catch (Exception e) {
            LOG.errorf(e, "Erro inesperado ao consultar CNPJ %s na CNPJA API", mascararDocumento(cnpj));
            return criarErroConsulta(
                "Erro inesperado ao consultar a Receita Federal. Tente novamente mais tarde.",
                null
            );
        }
    }

    private SituacaoCadastralDto tratarErroHttp(int status, String cnpj) {
        return switch (status) {
            case 400 -> criarErroConsulta(
                "CNPJ em formato invalido para consulta na Receita Federal.",
                400
            );
            case 404 -> criarResultado(false, "NAO_ENCONTRADO",
                "CNPJ nao encontrado na base da Receita Federal."
            );
            case 429 -> {
                LOG.warnf("Rate limit atingido ao consultar CNPJ %s", mascararDocumento(cnpj));
                yield criarErroConsulta(
                    "Limite de consultas por minuto atingido. Aguarde alguns instantes e tente novamente.",
                    429
                );
            }
            case 500, 502, 503, 504 -> criarErroConsulta(
                "Servico da Receita Federal temporariamente indisponivel. Tente novamente mais tarde.",
                status
            );
            default -> criarErroConsulta(
                "Erro ao consultar a Receita Federal (HTTP " + status + "). Tente novamente mais tarde.",
                status
            );
        };
    }

    @Override
    public SituacaoCadastralDto consultarCpf(String cpf) {
        LOG.debugf("Consultando CPF: %s", mascararDocumento(cpf));

        // Para CPF, apenas validamos o formato (nao ha API gratuita disponivel)
        if (!DocumentoUtil.validarCpf(cpf)) {
            return criarResultado(false, "INVALIDO",
                "CPF com formato invalido (digitos verificadores incorretos)");
        }

        // CPF valido - retorna como REGULAR (sem consulta externa)
        return criarResultado(true, "REGULAR",
            "CPF com formato valido. Validacao de situacao cadastral nao disponivel para CPF.");
    }

    private SituacaoCadastralDto criarResultado(boolean valido, String situacao, String mensagem) {
        SituacaoCadastralDto resultado = valido
            ? SituacaoCadastralDto.valido(situacao, mensagem)
            : SituacaoCadastralDto.invalido(situacao, mensagem);
        resultado.setDataConsulta(agora());
        return resultado;
    }

    private SituacaoCadastralDto criarErroConsulta(String mensagem, Integer codigoErro) {
        SituacaoCadastralDto resultado = SituacaoCadastralDto.erroConsulta(mensagem, codigoErro);
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
