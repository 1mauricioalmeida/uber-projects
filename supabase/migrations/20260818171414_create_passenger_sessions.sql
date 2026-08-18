create extension if not exists pgcrypto;

create table public.passenger_sessions (
  session_id uuid primary key,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  status text not null default 'new' check (status in ('new','seen','handled','closed')),
  language text not null default 'pt' check (language in ('pt','en','zh')),
  temperature text check (temperature is null or temperature in ('cooler','good','warmer')),
  music text check (music is null or music in ('instrumental','mpb','pop','silence')),
  interaction text check (interaction is null or interaction in ('talk','quiet','comfortable','help')),
  preference_note text check (preference_note is null or char_length(preference_note) <= 240),
  rating smallint check (rating is null or rating between 1 and 3),
  feedback_note text check (feedback_note is null or char_length(feedback_note) <= 360),
  submitted_preferences_at timestamptz,
  submitted_feedback_at timestamptz,
  handled_at timestamptz
);

create index passenger_sessions_created_at_idx on public.passenger_sessions (created_at desc);
create index passenger_sessions_status_created_at_idx on public.passenger_sessions (status, created_at desc);
create index passenger_sessions_interaction_idx on public.passenger_sessions (interaction) where interaction = 'help';

create or replace function public.set_updated_at()
returns trigger
language plpgsql
security invoker
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger passenger_sessions_set_updated_at
before update on public.passenger_sessions
for each row execute function public.set_updated_at();

alter table public.passenger_sessions enable row level security;

create policy "authenticated_driver_can_read_sessions"
on public.passenger_sessions
for select
to authenticated
using (true);

create policy "authenticated_driver_can_update_status"
on public.passenger_sessions
for update
to authenticated
using (true)
with check (true);

alter publication supabase_realtime add table public.passenger_sessions;

comment on table public.passenger_sessions is 'Anonymous passenger comfort preferences and optional feedback for Hospitalidade a Bordo.';
comment on column public.passenger_sessions.session_id is 'Opaque UUID generated in the passenger browser; contains no passenger identity.';
