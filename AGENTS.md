# AGENTS.md

## Missão

Este repositório é o hub central para projetos relacionados à experiência de motorista e passageiro em mobilidade.

## Princípios de trabalho

1. Separar claramente experiência do passageiro, operação do motorista, automações, integrações e experimentos.
2. Preferir o mecanismo mínimo suficiente, verificável e reversível.
3. Toda mudança material deve preservar rastreabilidade: requisito → implementação → validação → decisão.
4. Não coletar dados pessoais do passageiro sem necessidade explícita e documentação da finalidade.
5. Experimentos não devem ser confundidos com componentes de produção.
6. Código público não deve conter segredos, tokens, números privados, credenciais ou dados de passageiros.

## Estrutura inicial

- `passenger-experience/`: experiência web acessada pelo passageiro, inclusive via QR Code.
- `driver-dashboard/`: painel operacional do motorista.
- `automations/`: automações e integrações.
- `docs/`: arquitetura, decisões, roadmap e documentação transversal.
- `experiments/`: provas de conceito e testes descartáveis.

## Convenções

- Branch principal: `main`.
- Mudanças relevantes devem preferencialmente passar por branch + pull request.
- Decisões arquiteturais duráveis devem ser registradas em `docs/decisions/`.
- Cada projeto deve ter README próprio com objetivo, estado, execução, validação e pendências.

## Estado atual

Primeiro projeto ativo: experiência premium do passageiro, com personalização de conforto, avaliação anônima e futura integração com painel do motorista.
