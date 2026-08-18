# uber-projects

Hub central para projetos, automações, ferramentas e experimentos relacionados à experiência de motorista e passageiro em mobilidade.

O repositório reúne aplicações web, painéis, integrações, QR Codes, playlists, analytics, documentação e protótipos, com foco em conforto, atendimento, eficiência operacional e evolução contínua.

## Projetos

| Projeto | Objetivo | Estado |
| --- | --- | --- |
| `passenger-experience/` | Experiência premium acessada pelo passageiro via URL/QR Code | Bootstrap |
| `driver-dashboard/` | Painel operacional do motorista | Planejado |
| `automations/` | Automações e integrações futuras | Planejado |
| `experiments/` | Provas de conceito isoladas | Planejado |

## Primeiro objetivo

Publicar uma experiência web mobile-first em que o passageiro possa, sem login e de forma opcional:

- personalizar temperatura e ambiente sonoro;
- informar preferência de interação;
- solicitar ajuda;
- avaliar anonimamente a experiência;
- enviar sugestões;
- acessar contato profissional futuro quando apropriado.

A aplicação deverá funcionar por HTTPS no Safari/iPhone e Android e posteriormente será acessada por QR Code no veículo.

## Documentação

- [`AGENTS.md`](AGENTS.md) — regras operacionais e de continuidade do repositório.
- [`docs/architecture.md`](docs/architecture.md) — arquitetura inicial.
- [`docs/roadmap.md`](docs/roadmap.md) — evolução planejada.
- [`docs/decisions/`](docs/decisions/) — decisões arquiteturais registradas.

## Princípios

- privacidade por padrão;
- baixo atrito para o passageiro;
- segurança operacional para o motorista;
- separação entre protótipo e produção;
- decisões rastreáveis e reversíveis;
- mecanismo mínimo suficiente antes de adicionar automação.
