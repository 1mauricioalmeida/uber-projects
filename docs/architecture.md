# Arquitetura inicial

## Visão geral

O repositório será organizado como um monorepo por domínio funcional.

```text
uber-projects/
├── AGENTS.md
├── README.md
├── docs/
│   ├── architecture.md
│   ├── roadmap.md
│   └── decisions/
├── passenger-experience/
├── driver-dashboard/
├── automations/
└── experiments/
```

## Domínios

### Passenger Experience
Interface mobile-first acessada pelo passageiro via URL/QR Code. Responsável por acolhimento, preferências de conforto, ambiente sonoro, interação e avaliação anônima.

### Driver Dashboard
Interface separada para o motorista visualizar solicitações, avaliações, configurações e estado operacional.

### Backend
Camada de persistência e sincronização. Deve permitir sessões anônimas, preferências, avaliações e configurações do motorista sem exigir identificação do passageiro.

### Automations
Integrações opcionais como notificações, playlists e futuras rotinas de atendimento. Devem falhar de forma segura e nunca exigir interação perigosa durante a condução.

## Princípios

- Mobile-first.
- Privacidade por padrão.
- Baixo atrito para o passageiro.
- Operação segura para o motorista.
- Separação entre protótipo, teste e produção.
- Observabilidade e evidência suficientes para diagnosticar falhas.
