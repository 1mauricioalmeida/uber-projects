const ALLOWED_ORIGINS = new Set([
  'https://1mauricioalmeida.github.io',
  'http://localhost:8000',
  'http://127.0.0.1:8000'
]);

const allowed = {
  language: new Set(['pt', 'en', 'zh']),
  temperature: new Set(['cooler', 'good', 'warmer']),
  music: new Set(['instrumental', 'mpb', 'pop', 'silence']),
  interaction: new Set(['talk', 'quiet', 'comfortable', 'help']),
  rating: new Set([1, 2, 3])
};

function cors(origin: string | null) {
  const safeOrigin = origin && ALLOWED_ORIGINS.has(origin) ? origin : '';
  return {
    'Access-Control-Allow-Origin': safeOrigin,
    'Access-Control-Allow-Headers': 'content-type',
    'Access-Control-Allow-Methods': 'POST, OPTIONS',
    'Vary': 'Origin'
  };
}

function json(body: unknown, status: number, origin: string | null) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...cors(origin), 'Content-Type': 'application/json; charset=utf-8' }
  });
}

function cleanText(value: unknown, max: number) {
  if (typeof value !== 'string') return null;
  const text = value.trim();
  if (!text) return null;
  return text.slice(0, max);
}

function nullableEnum(value: unknown, values: Set<string>) {
  if (value == null || value === '') return null;
  return typeof value === 'string' && values.has(value) ? value : undefined;
}

function validUuid(value: unknown) {
  return typeof value === 'string' && /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value);
}

async function dbRequest(path: string, init: RequestInit = {}) {
  const url = Deno.env.get('SUPABASE_URL');
  const secretKeysRaw = Deno.env.get('SUPABASE_SECRET_KEYS');
  if (!url || !secretKeysRaw) throw new Error('Supabase environment is incomplete');
  const secret = JSON.parse(secretKeysRaw).default;
  if (!secret) throw new Error('Default Supabase secret key is unavailable');

  const response = await fetch(`${url}/rest/v1/${path}`, {
    ...init,
    headers: {
      'apikey': secret,
      'Content-Type': 'application/json',
      ...(init.headers || {})
    }
  });

  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) throw new Error(data?.message || data?.error || `Database request failed (${response.status})`);
  return data;
}

Deno.serve(async (req: Request) => {
  const origin = req.headers.get('Origin');

  if (req.method === 'OPTIONS') {
    if (!origin || !ALLOWED_ORIGINS.has(origin)) return new Response(null, { status: 403 });
    return new Response('ok', { headers: cors(origin) });
  }

  if (req.method !== 'POST') return json({ error: 'method_not_allowed' }, 405, origin);
  if (!origin || !ALLOWED_ORIGINS.has(origin)) return json({ error: 'origin_not_allowed' }, 403, origin);

  let body: Record<string, unknown>;
  try {
    body = await req.json();
  } catch {
    return json({ error: 'invalid_json' }, 400, origin);
  }

  const sessionId = body.sessionId;
  if (!validUuid(sessionId)) return json({ error: 'invalid_session_id' }, 400, origin);

  const language = body.language == null ? 'pt' : body.language;
  if (typeof language !== 'string' || !allowed.language.has(language)) {
    return json({ error: 'invalid_language' }, 400, origin);
  }

  const temperature = nullableEnum(body.temperature, allowed.temperature);
  const music = nullableEnum(body.music, allowed.music);
  const interaction = nullableEnum(body.interaction, allowed.interaction);
  if (temperature === undefined || music === undefined || interaction === undefined) {
    return json({ error: 'invalid_preference' }, 400, origin);
  }

  let rating: number | null = null;
  if (body.rating != null && body.rating !== '') {
    const numeric = Number(body.rating);
    if (!allowed.rating.has(numeric)) return json({ error: 'invalid_rating' }, 400, origin);
    rating = numeric;
  }

  const preferenceNote = cleanText(body.preferenceNote, 240);
  const feedbackNote = cleanText(body.feedbackNote, 360);
  const event = body.event === 'feedback' ? 'feedback' : 'preferences';
  const now = new Date().toISOString();

  try {
    const existingRows = await dbRequest(`passenger_sessions?session_id=eq.${encodeURIComponent(sessionId as string)}&select=*`);
    const existing = Array.isArray(existingRows) && existingRows.length ? existingRows[0] : null;

    const row: Record<string, unknown> = {
      session_id: sessionId,
      language,
      temperature: temperature ?? existing?.temperature ?? null,
      music: music ?? existing?.music ?? null,
      interaction: interaction ?? existing?.interaction ?? null,
      preference_note: preferenceNote ?? existing?.preference_note ?? null,
      rating: rating ?? existing?.rating ?? null,
      feedback_note: feedbackNote ?? existing?.feedback_note ?? null,
      status: existing?.status ?? 'new'
    };

    if (event === 'preferences') row.submitted_preferences_at = now;
    if (event === 'feedback') row.submitted_feedback_at = now;
    if (interaction === 'help') row.status = 'new';

    if (existing) {
      await dbRequest(`passenger_sessions?session_id=eq.${encodeURIComponent(sessionId as string)}`, {
        method: 'PATCH',
        headers: { 'Prefer': 'return=minimal' },
        body: JSON.stringify(row)
      });
    } else {
      await dbRequest('passenger_sessions', {
        method: 'POST',
        headers: { 'Prefer': 'return=minimal' },
        body: JSON.stringify(row)
      });
    }

    return json({ ok: true, sessionId, event, savedAt: now }, 200, origin);
  } catch (error) {
    console.error(error);
    return json({ error: 'storage_failed' }, 500, origin);
  }
});
