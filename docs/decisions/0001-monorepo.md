# ADR 0001 — Usar um monorepo para projetos de mobilidade

- Status: Aceito
- Data: 2026-08-18

## Contexto

Há vários projetos relacionados à mesma operação: experiência do passageiro, painel do motorista, automações, integrações, analytics e experimentos. Eles compartilham contexto, requisitos e decisões.

## Decisão

Manter esses projetos no repositório `uber-projects`, separados por diretórios de domínio.

## Consequências

### Positivas

- contexto e decisões permanecem próximos do código;
- documentação transversal fica centralizada;
- mudanças que atravessam mais de um componente podem ser rastreadas juntas;
- reduz proliferação prematura de repositórios.

### Riscos

- acoplamento excessivo entre projetos;
- crescimento desorganizado do repositório.

## Mitigações

- cada projeto terá README próprio;
- componentes terão fronteiras explícitas;
- experimentos ficarão isolados;
- se um componente adquirir ciclo de vida, permissões ou implantação muito diferentes, poderá ser extraído para outro repositório futuramente.
