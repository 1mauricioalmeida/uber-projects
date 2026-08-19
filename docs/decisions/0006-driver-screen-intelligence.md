# ADR 0006 — Inteligência operacional por leitura passiva da interface

## Status

Aceita para protótipo v0.2.

## Contexto

O objetivo do app do motorista é medir performance operacional sem exigir interação durante a condução. Parte dos dados necessários existe apenas na interface do Uber Driver no momento em que uma oferta ou etapa da viagem é exibida.

## Decisão

Usar um `AccessibilityService` Android com escopo restrito ao pacote `com.ubercab.driver` para observar a árvore de acessibilidade e converter somente informações operacionais reconhecidas em eventos estruturados.

A primeira versão reconhece ofertas e calcula métricas previstas. Ela não executa gestos, cliques, aceite, recusa ou qualquer ação dentro do Uber Driver.

## Princípios

- observação passiva, sem automação de decisões do motorista;
- não armazenar a árvore bruta da interface por padrão;
- persistir apenas campos estruturados necessários à análise;
- atribuir confiança à interpretação e registrar falhas de parser;
- tratar layouts do Uber como fonte instável e calibrar com dispositivo real;
- separar dado exibido pela plataforma, dado coletado pelo nosso GPS e métricas derivadas;
- nenhuma recomendação de performance pode incentivar direção insegura.

## Dados estruturados iniciais

- instante em que a oferta foi observada;
- categoria;
- valor ofertado;
- R$/km exibido, quando presente;
- avaliação e quantidade de avaliações, quando presentes;
- sinalizadores como verificado, exclusivo, adicional e viagem longa;
- tempo, distância e endereço até o embarque;
- tempo, distância e endereço da viagem;
- tempo/distância totais previstos;
- R$/km, R$/hora e R$/min calculados;
- custo e lucro previstos quando o motorista informar custo/km;
- confiança do parser.

## Consequências

O usuário precisa habilitar explicitamente o serviço nas configurações de Acessibilidade do Android. Mudanças de layout do Uber podem reduzir a taxa de interpretação e exigirão ajustes do parser. O teste inicial deve ser feito com o veículo parado.
