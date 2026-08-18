grant select on public.driver_users to authenticated;

create policy "driver_can_read_own_allowlist_entry"
on public.driver_users
for select
to authenticated
using (
  (select auth.uid()) is not null
  and user_id = (select auth.uid())
);
