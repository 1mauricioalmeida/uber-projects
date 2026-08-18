create table public.driver_users (
  user_id uuid primary key references auth.users(id) on delete cascade,
  created_at timestamptz not null default now()
);

alter table public.driver_users enable row level security;

revoke all on public.driver_users from anon, authenticated;
revoke all on public.passenger_sessions from anon;
grant select, update on public.passenger_sessions to authenticated;

DROP POLICY IF EXISTS "authenticated_driver_can_read_sessions" ON public.passenger_sessions;
DROP POLICY IF EXISTS "authenticated_driver_can_update_status" ON public.passenger_sessions;

create policy "registered_driver_can_read_sessions"
on public.passenger_sessions
for select
to authenticated
using (
  exists (
    select 1 from public.driver_users d
    where d.user_id = (select auth.uid())
  )
);

create policy "registered_driver_can_update_sessions"
on public.passenger_sessions
for update
to authenticated
using (
  exists (
    select 1 from public.driver_users d
    where d.user_id = (select auth.uid())
  )
)
with check (
  exists (
    select 1 from public.driver_users d
    where d.user_id = (select auth.uid())
  )
);

comment on table public.driver_users is 'Allowlist of authenticated users authorized to view the driver dashboard.';
