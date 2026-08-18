# ADR 0004 — Supabase para preferências em tempo real

## Status
Aceito.

## Contexto
A experiência do passageiro já funciona como site estático em GitHub Pages, mas as escolhas ainda precisam chegar ao motorista durante a viagem sem exigir login, nome, telefone, e-mail ou localização do passageiro.

## Decisão
Usar um projeto Supabase exclusivo do Hospitalidade a Bordo, na região `sa-east-1` (São Paulo), como backend do MVP.

- `passenger_sessions` armazena apenas preferências, observações opcionais e avaliação, vinculadas a um UUID opaco de sessão.
- RLS permanece habilitado.
- Passageiros não recebem acesso direto de leitura ou escrita à tabela.
- Escritas públicas passam por uma Edge Function com validação explícita de origem, enums e limites de texto.
- O painel do motorista deverá usar Supabase Auth e uma allowlist em `driver_users` para leitura e atualização.
- `passenger_sessions` participa da publicação `supabase_realtime` para atualização do painel.
- Chaves secretas/service-role nunca são enviadas ao navegador.

## Consequências
O site continua sem login para passageiros e pode operar com degradação graciosa quando estiver offline. O painel do motorista exigirá autenticação. O endpoint público precisa de monitoramento e, antes de uso amplo, proteção adicional contra abuso/rate limiting.

## Privacidade
Não coletar identidade, telefone, e-mail ou geolocalização do passageiro neste MVP. Observações livres permanecem opcionais e com tamanho limitado.
