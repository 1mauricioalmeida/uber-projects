(() => {
  'use strict';

  const config = window.PASSENGER_EXPERIENCE_CONFIG || {};
  const backend = config.backend || {};
  if (!backend.enabled || !backend.submitUrl) return;

  const SESSION_ID_KEY = 'passenger-experience-session-id';
  const PENDING_KEY = 'passenger-experience-pending-sync';
  let timer = null;
  let inFlight = false;
  let queuedEvent = null;

  const privacyCopy = {
    pt: ' Este site não solicita nome, telefone, e-mail ou localização. Suas preferências podem ser enviadas de forma anônima ao motorista durante a viagem.',
    en: ' This site does not request your name, phone, email or location. Your preferences may be sent anonymously to the driver during the ride.',
    zh: ' 本页面不会要求姓名、电话、电子邮件或位置。您的乘车偏好可以匿名发送给司机。'
  };

  function getSessionId() {
    let id = sessionStorage.getItem(SESSION_ID_KEY);
    if (!id) {
      id = crypto.randomUUID();
      sessionStorage.setItem(SESSION_ID_KEY, id);
    }
    return id;
  }

  function readLocalState() {
    try {
      return JSON.parse(sessionStorage.getItem('passenger-experience-session') || '{}');
    } catch {
      return {};
    }
  }

  function buildPayload(event) {
    const local = readLocalState();
    const preferences = local.preferences || {};
    const feedback = local.feedback || {};
    const language = document.querySelector('#language')?.value || 'pt';

    return {
      sessionId: getSessionId(),
      event,
      language,
      temperature: preferences.temperature || null,
      music: preferences.music || null,
      interaction: preferences.interaction || null,
      preferenceNote: document.querySelector('#preference-note')?.value.trim() || preferences.note || null,
      rating: feedback.rating || null,
      feedbackNote: document.querySelector('#feedback-note')?.value.trim() || feedback.note || null
    };
  }

  async function send(event) {
    if (inFlight) {
      queuedEvent = event;
      return;
    }

    inFlight = true;
    const payload = buildPayload(event);
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 7000);

    try {
      const response = await fetch(backend.submitUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        signal: controller.signal,
        keepalive: true
      });

      if (!response.ok) throw new Error(`Backend returned ${response.status}`);
      sessionStorage.removeItem(PENDING_KEY);
    } catch (error) {
      sessionStorage.setItem(PENDING_KEY, JSON.stringify(payload));
      console.warn('Passenger experience sync deferred:', error);
    } finally {
      clearTimeout(timeout);
      inFlight = false;
      if (queuedEvent) {
        const next = queuedEvent;
        queuedEvent = null;
        void send(next);
      }
    }
  }

  function schedule(event, delay = 260) {
    clearTimeout(timer);
    timer = setTimeout(() => void send(event), delay);
  }

  function updatePrivacyCopy() {
    const lang = document.querySelector('#language')?.value || 'pt';
    const text = document.querySelector('.privacy-card span');
    if (text && privacyCopy[lang]) text.textContent = privacyCopy[lang];
  }

  document.querySelectorAll('.choice').forEach(button => {
    button.addEventListener('click', () => {
      const group = button.closest('.choice-group')?.dataset.group;
      const isHelp = group === 'interaction' && button.dataset.value === 'help';
      schedule('preferences', isHelp ? 0 : 220);
    });
  });

  document.querySelectorAll('.rating button').forEach(button => {
    button.addEventListener('click', () => schedule('feedback', 220));
  });

  document.querySelector('#preference-note')?.addEventListener('input', () => schedule('preferences', 700));
  document.querySelector('#feedback-note')?.addEventListener('input', () => schedule('feedback', 700));

  document.querySelector('#save-preferences')?.addEventListener('click', () => void send('preferences'));
  document.querySelector('#skip-preferences')?.addEventListener('click', () => void send('preferences'));
  document.querySelector('#save-feedback')?.addEventListener('click', () => void send('feedback'));
  document.querySelector('#skip-feedback')?.addEventListener('click', () => void send('feedback'));

  document.querySelector('#restart')?.addEventListener('click', () => {
    clearTimeout(timer);
    sessionStorage.removeItem(SESSION_ID_KEY);
    sessionStorage.removeItem(PENDING_KEY);
  });

  document.querySelector('#language')?.addEventListener('change', () => {
    setTimeout(updatePrivacyCopy, 0);
    schedule('preferences', 300);
  });

  window.addEventListener('online', () => {
    const pending = sessionStorage.getItem(PENDING_KEY);
    if (!pending) return;
    try {
      const parsed = JSON.parse(pending);
      void send(parsed.event === 'feedback' ? 'feedback' : 'preferences');
    } catch {
      sessionStorage.removeItem(PENDING_KEY);
    }
  });

  updatePrivacyCopy();
})();
