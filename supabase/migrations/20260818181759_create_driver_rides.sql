alter table public.driver_users
  add column if not exists public_code uuid not null default gen_random_uuid();

create unique index if not exists driver_users_public_code_key
  on public.driver_users(public_code);

create table public.rides (
  id uuid primary key default gen_random_uuid(),
  driver_user_id uuid not null references public.driver_users(user_id) on delete cascade,
  status text not null default 'active' check (status in ('active','completed','cancelled')),
  started_at timestamptz not null default now(),
  ended_at timestamptz,

  start_location_captured_at timestamptz,
  start_lat double precision,
  start_lng double precision,
  start_accuracy_m real,
  start_address text,
  start_street text,
  start_number text,
  start_district text,
  start_city text,
  start_state text,
  start_postal_code text,
  start_country_code text,
  start_geocoder text,

  end_location_captured_at timestamptz,
  end_lat double precision,
  end_lng double precision,
  end_accuracy_m real,
  end_address text,
  end_street text,
  end_number text,
  end_district text,
  end_city text,
  end_state text,
  end_postal_code text,
  end_country_code text,
  end_geocoder text,

  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),

  check ((status = 'active' and ended_at is null) or status <> 'active')
);

create unique index rides_one_active_per_driver_idx
  on public.rides(driver_user_id)
  where status = 'active';

create index rides_driver_started_at_idx
  on public.rides(driver_user_id, started_at desc);

create trigger rides_set_updated_at
before update on public.rides
for each row execute function public.set_updated_at();

alter table public.rides enable row level security;

create policy "driver_can_read_own_rides"
on public.rides
for select
to authenticated
using (
  driver_user_id = (select auth.uid())
  and exists (
    select 1 from public.driver_users d
    where d.user_id = (select auth.uid())
  )
);

create policy "driver_can_insert_own_rides"
on public.rides
for insert
to authenticated
with check (
  driver_user_id = (select auth.uid())
  and exists (
    select 1 from public.driver_users d
    where d.user_id = (select auth.uid())
  )
);

create policy "driver_can_update_own_rides"
on public.rides
for update
to authenticated
using (
  driver_user_id = (select auth.uid())
  and exists (
    select 1 from public.driver_users d
    where d.user_id = (select auth.uid())
  )
)
with check (
  driver_user_id = (select auth.uid())
  and exists (
    select 1 from public.driver_users d
    where d.user_id = (select auth.uid())
  )
);

alter table public.passenger_sessions
  add column if not exists ride_id uuid references public.rides(id) on delete set null;

create index if not exists passenger_sessions_ride_id_idx
  on public.passenger_sessions(ride_id);

alter publication supabase_realtime add table public.rides;

comment on table public.rides is 'Driver-owned ride sessions with start/end location snapshots for later Uber reconciliation.';
comment on column public.driver_users.public_code is 'Public non-secret identifier used to associate passenger QR experiences with a driver without exposing the auth user id.';
