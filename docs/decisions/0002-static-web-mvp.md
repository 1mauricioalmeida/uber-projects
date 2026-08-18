# ADR 0002 — MVP web estático para a experiência do passageiro

## Status

Aceito para o primeiro marco.

## Contexto

O protótipo visual anterior foi distribuído como um arquivo `.html`. Em iPhone, a abertura pelo visualizador de arquivos exibiu a interface, mas não ofereceu a experiência confiável de uma aplicação web publicada. O requisito imediato é validar a experiência real do passageiro por HTTPS, no Safari/iPhone e Android, antes de introduzir backend, painel ou automações.

## Decisão

Implementar o primeiro marco como aplicação estática em HTML, CSS e JavaScript, sem dependências de runtime, hospedada por GitHub Pages a partir de `passenger-experience/site/`.

O MVP inclui navegação interativa, seleção de preferências, avaliação, internacionalização PT/EN/中文 e configuração pública opcional para WhatsApp profissional.

Os dados do fluxo permanecem somente no navegador do passageiro nesta fase. Nenhuma mensagem da interface deve afirmar que as preferências foram transmitidas ao motorista enquanto não existir backend.

## Consequências positivas

- mecanismo mínimo suficiente para testar a experiência publicada;
- deploy simples e reversível;
- baixa superfície operacional e de segurança;
- ausência de dependências de frontend para o primeiro marco;
- separação clara entre validação de UX e sincronização remota.

## Limitações aceitas

- preferências ainda não chegam ao motorista;
- avaliações ainda não são agregadas entre dispositivos;
- contato por WhatsApp só aparece após configuração explícita de um número profissional público;
- o QR Code definitivo deve ser gerado apenas depois da URL HTTPS estar validada.

## Próxima decisão necessária

Definir o backend mínimo para sincronização anônima de preferências e avaliações com o painel do motorista, incluindo modelo de dados, retenção, autorização e mecanismo de atualização em tempo próximo do real.
