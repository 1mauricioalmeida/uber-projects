# Hospitalidade a Bordo — app Android do motorista

## Objetivo

Executar as funções que precisam continuar confiáveis enquanto Uber Driver e Waze estão em primeiro plano, sem exigir interação constante durante a condução.

## Primeiro teste no tablet

Este MVP testa, de forma isolada, os pontos de maior risco técnico no dispositivo real:

- serviço em primeiro plano durante uma viagem ativa;
- captura de localização do tablet do motorista;
- latitude, longitude, precisão e geocodificação reversa local;
- notificação persistente;
- heads-up opcional para novos pedidos;
- leitura por voz opcional;
- botão flutuante sobre outros apps;
- menu rápido do botão flutuante;
- perfis de aviso configuráveis;
- contrato para automação musical;
- simulação local de uma solicitação do passageiro.

## Botão flutuante

O overlay do motorista deve permanecer útil sem ocupar espaço excessivo da navegação:

- arrastar com um dedo para reposicionar livremente pela tela;
- gesto de pinça com dois dedos para reduzir ou ampliar;
- limite entre 32 dp e 72 dp para evitar que desapareça ou cubra área demais;
- toque simples abre ou fecha o menu rápido;
- posição e tamanho são persistidos e restaurados na próxima sessão;
- o menu rápido abre preferencialmente no lado oposto ao botão, para reduzir sobreposição com Uber Driver e Waze.

## Segurança operacional

O app não deve exigir leitura ou toque enquanto o veículo estiver em movimento. O botão flutuante é um atalho de consulta para quando for seguro usar a tela. A música deve ser automatizada; solicitações manuais podem usar voz e/ou notificação persistente conforme configuração.

## Música

`MusicAutomationController` é o ponto único de integração. O MVP já encaminha o perfil escolhido para esse componente automaticamente, mas o adaptador de reprodução real ainda não está implementado. Ele será escolhido depois de validar no tablet qual player pode ser controlado de forma estável e compatível com suas regras de uso.

## Localização

A localização é coletada somente no app do motorista. O MVP mantém a última posição em memória e registra localmente os pontos de início e fim, com precisão. Não há rastreamento do aparelho do passageiro.

## Acessibilidade e comunicação

O produto deve permitir que uma pessoa comunique necessidades sem precisar falar e sem precisar declarar uma deficiência ou diagnóstico. As escolhas devem descrever necessidades operacionais — silêncio, forma de comunicação, temperatura, som e ajuda — e não criar perfis sensíveis de passageiros.

A pesquisa e os requisitos derivados estão registrados em `docs/research/2026-08-rider-needs-accessibility.md`.

## Build

O workflow `Build driver Android MVP` gera um APK de debug como artefato do GitHub Actions.

## Próximas integrações

1. autenticação/pareamento seguro do tablet com o Supabase;
2. criação e encerramento de `ride` no backend;
3. vínculo automático do envio do passageiro com a viagem ativa;
4. recebimento em tempo real dos pedidos reais;
5. persistência de início/fim e endereço no Supabase;
6. adaptador de música automática;
7. conciliação posterior com dados do Uber Driver;
8. evolução do formulário do passageiro para comunicação sem fala e necessidades sensoriais, sem exigir diagnóstico.
