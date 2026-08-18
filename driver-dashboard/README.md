# Driver Dashboard

## Objetivo

Centralizar a operação do motorista sem expor informações aos passageiros e sem exigir interação constante durante a condução.

## Escopo atual

- app Android nativo em `driver-dashboard/android/`;
- viagem ativa com serviço em primeiro plano;
- localização capturada somente no tablet do motorista;
- notificação persistente de solicitações;
- voz, heads-up, vibração e alertas configuráveis;
- botão flutuante e menu rápido sobre outros apps;
- música tratada como automação, não como tarefa manual do motorista;
- início/fim da viagem preparados para posterior conciliação com Uber Driver.

## Segurança operacional

Durante a condução, o app deve trabalhar em segundo plano. Música deve ser automatizada. Solicitações manuais podem chegar por voz e/ou notificação persistente. Controles de consulta e encerramento devem ser usados somente quando for seguro interagir com a tela.

## Estado

`em implementação`

O primeiro MVP Android foi iniciado para validar permissões, localização, notificações e overlay no tablet real antes de conectar todo o fluxo ao backend.
