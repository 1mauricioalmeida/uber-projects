# ADR 0005 — App Android nativo para operação do motorista

## Status

Aceito para MVP.

## Contexto

O motorista precisa usar Uber Driver e Waze em primeiro plano. O Hospitalidade a Bordo precisa continuar recebendo solicitações, manter uma notificação persistente, capturar localização do tablet e oferecer um atalho flutuante sem depender de uma aba web ativa.

A experiência do passageiro continua web. A operação do motorista passa a ter um componente Android nativo separado.

## Decisão

Criar um app Android nativo dedicado ao motorista, com um serviço em primeiro plano do tipo `location` iniciado pelo usuário quando a viagem começa.

O MVP deve:

- capturar localização apenas no tablet do motorista;
- manter a sessão ativa enquanto outros apps estão em primeiro plano;
- oferecer notificação persistente;
- permitir voz, heads-up, vibração e alertas como preferências do motorista;
- oferecer overlay opcional mediante permissão explícita `SYSTEM_ALERT_WINDOW`;
- tratar música como automação por um adaptador próprio;
- não exigir toque durante o movimento;
- persistir somente os pontos operacionais necessários, evitando guardar trajeto completo por padrão.

## Fundamentação de plataforma

- Android 14+ exige que serviços em primeiro plano declarem um tipo adequado. Para localização, o tipo é `location`, com `FOREGROUND_SERVICE_LOCATION` e permissão de localização em runtime.
- A permissão de overlay precisa ser concedida explicitamente pelo usuário e pode ser verificada com `Settings.canDrawOverlays()`.
- Notificações de importância alta podem ser usadas para avisos pontuais, enquanto o serviço em primeiro plano mantém uma notificação persistente de baixa importância.

Referências oficiais:

- https://developer.android.com/develop/background-work/services/fgs/service-types
- https://developer.android.com/reference/android/provider/Settings#canDrawOverlays(android.content.Context)
- https://developer.android.com/develop/ui/views/notifications/channels

## Música

A automação musical será implementada por `MusicAutomationController`. A escolha do player/adaptador real fica separada deste ADR porque precisa de validação técnica e de política no tablet e no serviço de música usado.

## Consequências

### Positivas

- melhor confiabilidade em segundo plano;
- localização vem do aparelho correto;
- menor dependência de interação manual;
- overlay e notificações podem coexistir com Uber Driver/Waze;
- arquitetura separa passageiro, operação e integrações.

### Custos/limitações

- exige instalação de APK e permissões Android;
- overlay pode ser bloqueado por determinadas telas/apps;
- automação musical depende do player escolhido;
- autenticação/pareamento seguro do tablet ainda precisa ser implementado antes do backend real.
