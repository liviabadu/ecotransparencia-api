# Dicionário de Dados - Embargos IBAMA

## Fonte

Este dicionário de dados documenta os campos disponíveis nos registros de embargos do sistema de fiscalização do IBAMA (Instituto Brasileiro do Meio Ambiente e dos Recursos Naturais Renováveis), vinculado ao Ministério do Meio Ambiente.

---

## Campos de Identificação do Termo de Embargo

### SEQ_TAD

Número sequencial que funciona como chave primária e identifica unicamente cada termo de embargo no sistema.

### NUM_TAD

Número impresso no formulário que compõe a identificação do Termo de Embargo. Os Termos de Embargo emitidos até 07/10/2019, pelo Sistema de Cadastro, Arrecadação e Fiscalização (Sicafi), utilizam um padrão composto de números acompanhado da respectiva Série. Os Termos de Embargo emitidos a partir de 08/10/2019, pelo Sistema do Auto de Infração Eletrônico (AIe), utilizam um padrão alfanumérico.

### SER_TAD

Caractere impresso no formulário que contém a respectiva série e compõe a identificação do documento em conexão com o número do termo de embargo no Sistema de Cadastro, Arrecadação e Fiscalização (Sicafi). Nos Termos de Embargo alfanuméricos, emitidos a partir de 08/10/2019 pelo Sistema do Auto de Infração Eletrônico (AIe), esse campo não é preenchido.

---

## Campos Temporais

### DAT_EMBARGO

Data em que o termo de embargo foi lavrado.

### DAT_ULT_ALTERACAO

Data da última alteração dos atributos do termo de embargo.

### DAT_ULT_ALTER_GEOM

Data da última alteração da geometria espacial do embargo.

---

## Campos de Localização Geográfica

### COD_UF_TAD

Código da Unidade da Federação do local embargado.

### SIG_UF_TAD

Sigla da Unidade da Federação do local embargado (exemplo: SP, MG, AM).

### COD_MUNICIPIO_TAD

Código do município segundo o IBGE referente ao local embargado.

### NOM_MUNICIPIO_TAD

Nome do município do local embargado.

### NUM_LONGITUDE_TAD

Longitude do local embargado expressa em formato decimal.

### NUM_LATITUDE_TAD

Latitude do local embargado expressa em formato decimal.

### NUM_LONGITUDE_GMS_TAD

Longitude do local embargado expressa em grau, minuto e segundo.

### NUM_LATITUDE_GMS_TAD

Latitude do local embargado expressa em grau, minuto e segundo.

### NOME_IMOVEL

Denominação do imóvel objeto do embargo.

### DES_LOCALIZACAO_TAD

Descrição textual detalhada do local embargado.

### WKT_GEOM_AREA_EMBARGADA

Geometria espacial do embargo no formato texto WKT (Well-Known Text). Este campo permite representar polígonos, pontos ou linhas que delimitam a área embargada em sistemas de informação geográfica.

---

## Campos de Identificação do Embargado

### NOME_PESSOA_EMBARGADA

Nome da pessoa (física ou jurídica) identificada como responsável pelo local ou área embargada.

### CPF_CNPJ_EMBARGADO

Número de Cadastro Nacional de Pessoa Jurídica (CNPJ) ou Cadastro de Pessoa Física (CPF) da pessoa embargada. Este campo pode conter 11 dígitos para CPF ou 14 dígitos para CNPJ.

---

## Campos de Caracterização do Embargo

### SIT_DESMATAMENTO

Indicador que classifica se o embargo está relacionado a desmatamento ilegal. Os valores possíveis são: "D" indica que o embargo está classificado como "Desmatamento", "Queimada" ou "Desmatamento e Queimada", ou não há classificação mas está vinculado a auto de infração com essas classificações, significando que o embargo decorre de uso econômico de áreas desmatadas ilegalmente no imóvel rural. "N" indica demais situações onde o embargo não decorre de uso econômico de áreas desmatadas ilegalmente no imóvel rural.

### TP_AREA_EMBARGADA

Tipo de dano identificado na área embargada.

### DS_OUTROS_TIPO_AREA

Descrição complementar com maiores detalhes quando, no campo tipo de dano da área embargada (TP_AREA_EMBARGADA), for escolhida a opção "Outros".

### ST_AREA_DESMATADA_ILEGAL

Indicador booleano de uso econômico de áreas desmatadas ilegalmente no imóvel rural.

### QTD_AREA_EMBARGADA

Área total objeto do embargo, expressa em hectares.

### QTD_AREA_DESMATADA

Área total autuada expressa em hectares. Este valor pode diferir de QTD_AREA_EMBARGADA quando a autuação cobre uma área diferente do embargo.

### DES_TAD

Descrição textual das circunstâncias que motivaram o embargo, incluindo justificativa e normas legais que fundamentam a aplicação da medida.

---

## Campos de Informação Ambiental

### COD_TIPO_BIOMA

Código identificador do bioma vinculado ao auto de infração.

### DES_TIPO_BIOMA

Descrição do bioma vinculado ao auto de infração (exemplos: Amazônia, Cerrado, Mata Atlântica, Caatinga, Pampa, Pantanal).

### DES_INFRACAO

Descrição da infração ambiental vinculada ao auto de infração que originou o embargo.

---

## Campos Administrativos e de Fiscalização

### OPERACAO

Nome da operação de fiscalização que resultou no embargo. Operações de grande porte do IBAMA frequentemente recebem nomes específicos para fins de identificação e rastreamento.

### UNID_IBAMA_CONTROLE

Nome da Unidade do IBAMA responsável pelo termo de embargo.

### ORDEM_FISCALIZACAO

Identificador da ordem de fiscalização que motivou a ação fiscalizatória.

### ACAO_FISCALIZATORIA

Número da ação fiscalizatória de origem do auto de infração.

### NUM_PROCESSO

Número do processo administrativo do IBAMA relacionado ao Termo de Embargo aplicado pelo agente ambiental.

---

## Campos de Vinculação com Auto de Infração

### NUM_AUTO_INFRACAO

Número do auto de infração vinculado ao termo de embargo. Um termo de embargo geralmente está associado a um auto de infração que documenta a irregularidade constatada.

### SER_AUTO_INFRACAO

Caractere que identifica a série do auto de infração. Os valores possíveis são: "A" ou "D" para formulários analógicos (em papel), "E" para formulário eletrônico oriundo de aplicativo Windows Mobile utilizado entre 2013 e 2019, e sem série para formulário eletrônico oriundo de aplicativo Android do sistema Sabiá-Fiscalização.

---

## Observações Técnicas para Integração

Este dicionário é essencial para a implementação do adaptador de integração com a base de embargos do IBAMA no sistema EcoTransparência. Os campos CPF_CNPJ_EMBARGADO e NOME_PESSOA_EMBARGADA são os principais campos de busca para localizar registros de uma entidade específica. Os campos de coordenadas geográficas (latitude e longitude) e o campo WKT_GEOM_AREA_EMBARGADA permitem visualização espacial dos embargos em mapas. O campo DAT_EMBARGO é utilizado para ordenação cronológica dos resultados, enquanto DAT_ULT_ALTERACAO indica a atualidade dos dados.
