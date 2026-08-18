# ADR 0003 — Perfis musicais e integração com Spotify

## Status

Proposto para o próximo marco operacional.

## Contexto

O passageiro deve poder escolher o clima musical da viagem sem precisar autenticar em um serviço de streaming. A escolha precisa ser simples, rápida e compatível com o restante da experiência anônima.

O Spotify oferece APIs de playlist e controle de reprodução, mas o controle de playback depende de autenticação OAuth da conta do motorista, de conta Premium para os endpoints de Player e das regras vigentes da Spotify Platform. A documentação atual também impõe restrições a integrações comerciais de streaming.

## Decisão

1. A interface do passageiro trabalha com **perfis musicais internos**, não diretamente com IDs do Spotify.
2. Cada perfil pode apontar para uma playlist curada pelo motorista.
3. O passageiro nunca precisa autenticar no Spotify.
4. O backend/painel do motorista receberá apenas a chave do perfil escolhido, por exemplo `instrumental`, `mpb`, `pop` ou `silence`.
5. A reprodução automática no Spotify fica **desabilitada por padrão** e será tratada como adaptação opcional.
6. A eventual integração de playback só será ativada após validar os requisitos técnicos, OAuth, conta Premium e conformidade com as políticas vigentes da Spotify Platform.
7. O produto deve continuar útil mesmo sem integração automática: o painel pode mostrar a playlist correspondente para o motorista executar de forma segura quando apropriado.

## Autenticação futura

Para uma aplicação web em que o segredo não pode ficar no navegador, usar Authorization Code com PKCE para a autorização da conta Spotify do motorista, salvo mudança futura nas recomendações oficiais.

## Perfis iniciais

- `instrumental`: instrumental suave, lo-fi limpo, piano e ambient discreto.
- `mpb`: MPB tranquila e acústica.
- `pop`: pop leve, conhecido e não agressivo.
- `silence`: nenhuma música.

## Regras de curadoria

- Evitar conteúdo explícito por padrão.
- Evitar mudanças abruptas de volume e energia.
- Priorizar faixas adequadas a um ambiente compartilhado.
- Manter playlists suficientemente longas para reduzir repetição entre corridas.
- Não permitir que a escolha musical do passageiro elimine o controle operacional do motorista.

## Consequências

A UX fica desacoplada do fornecedor de streaming. Se a integração automática com Spotify não puder ser usada, as mesmas preferências continuam funcionando com execução manual ou outro provedor no futuro.
