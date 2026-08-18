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

## Segurança operacional

O app não deve exigir leitura ou toque enquanto o veículo estiver em movimento. O botão flutuante é um atalho de consulta para quando for seguro usar a tela. A música deve ser automatizada; solicitações manuais podem usar voz e/ou notificação persistente conforme configuração.

## Música

`MusicAutomationController` é o ponto único de integração. O MVP já encaminha o perfil escolhido para esse componente automaticamente, mas o adaptador de reprodução real ainda não está implementado. Ele será escolhido depois de validar no tablet qual player pode ser controlado de forma estável e compatível com suas regras de uso.

## Localização

A localização é coletada somente no app do motorista. O MVP mantém a última posição em memória e registra localmente os pontos de início e fim, com precisão. Não há rastreamento do aparelho do passageiro.

## Build

O workflow `Build driver Android MVP` gera um APK de debug como artefato do GitHub Actions.

## Próximas integrações

1. autenticação/pareamento seguro do tablet com o Supabase;
2. criação e encerramento de `ride` no backend;
3. vínculo automático do envio do passageiro com a viagem ativa;
4. recebimento em tempo real dos pedidos reais;
5. persistência de início/fim e endereço no Supabase;
6. adaptador de música automática;
7. conciliação posterior com dados do Uber Driver.
