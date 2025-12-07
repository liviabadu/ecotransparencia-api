# Changelog

Todas as mudanças notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/),
e este projeto adere ao [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [1.0.0] - 2025-12-07

### Adicionado
- API REST para consulta de entidades com áreas embargadas pelo IBAMA
- Endpoints para busca de pessoas físicas e jurídicas
- Integração com banco de dados H2 (dev/test) e MySQL (produção)
- Carregamento de dados do IBAMA via CSV
- Documentação OpenAPI/Swagger UI
- Testes de contrato com Pact
- Dockerfile multi-stage para build e deploy
- Compatibilidade com Google Cloud Build
- Swagger UI habilitado em todos os ambientes
