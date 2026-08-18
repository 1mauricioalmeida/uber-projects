# Passenger Experience

## Objetivo

Criar uma experiência web premium e acolhedora para passageiros, acessível por QR Code e otimizada para celular.

## Escopo atual

O passageiro pode, opcionalmente e sem identificação pessoal:

- receber uma mensagem de boas-vindas;
- escolher o conforto térmico;
- escolher o ambiente sonoro;
- indicar preferência por conversa ou silêncio;
- avisar se precisa de ajuda;
- enviar uma observação curta;
- avaliar a experiência;
- deixar uma sugestão anônima;
- acessar um contato profissional futuro quando configurado.

## Idiomas

- Português brasileiro.
- Inglês.
- Chinês simplificado.

## Implementação

A primeira versão publicada usa HTML, CSS e JavaScript sem dependências de runtime. Essa escolha reduz complexidade no MVP e permite hospedagem estática via GitHub Pages.

Arquivos públicos:

- `site/index.html`: estrutura e conteúdo da experiência;
- `site/styles.css`: identidade visual mobile-first;
- `site/app.js`: fluxo, seleções, idiomas e estado local;
- `site/config.js`: configuração pública, inclusive WhatsApp profissional;
- `site/.nojekyll`: impede processamento Jekyll desnecessário.

## Execução local

Abra `site/index.html` por um servidor HTTP local. Para uso real no iPhone/Android, utilize a URL HTTPS publicada; não abra o arquivo `.html` pelo visualizador de arquivos do celular.

## Publicação

O workflow `.github/workflows/deploy-passenger-experience.yml` publica o conteúdo de `passenger-experience/site/` no GitHub Pages após alterações entrarem em `main`.

URL esperada após o GitHub Pages estar habilitado:

`https://1mauricioalmeida.github.io/uber-projects/`

## Configuração do WhatsApp

Edite `site/config.js` e preencha `whatsappNumber` somente com um número profissional que possa ficar público, usando código do país e DDD, sem espaços ou símbolos. Enquanto o campo estiver vazio, o botão de WhatsApp permanece oculto.

## Privacidade e dados

Esta versão não solicita nome, telefone, e-mail ou localização do passageiro. As escolhas e a avaliação são armazenadas apenas em `sessionStorage` no navegador do passageiro para suportar o fluxo da própria sessão.

**Limitação intencional do MVP:** ainda não existe sincronização com o celular do motorista. Portanto, esta versão valida a experiência publicada e interativa, mas não deve afirmar ao passageiro que as preferências foram enviadas ao motorista.

A sincronização em tempo real pertence ao próximo marco e exigirá um backend definido e documentado antes da coleta remota de dados.

## Validação

Critérios do MVP web:

- abrir por HTTPS no Safari/iPhone e navegadores Android;
- botão inicial iniciar o fluxo de personalização;
- seleções funcionarem com feedback visual;
- navegação entre as três etapas funcionar;
- troca entre PT, EN e 中文 funcionar;
- avaliação e campos opcionais funcionarem;
- nenhum dado pessoal ser solicitado;
- site não depender da abertura de arquivo HTML local.

## Estado

`mvp-web`

Próximo marco: habilitar e validar o deploy no GitHub Pages, testar a URL em um celular real e então projetar o backend de sincronização com o painel do motorista.
