(() => {
  'use strict';

  const config = window.PASSENGER_EXPERIENCE_CONFIG || {};
  const state = {
    lang: 'pt',
    step: 0,
    preferences: { temperature: null, music: null, interaction: null, note: '' },
    feedback: { rating: null, note: '' }
  };

  const translations = {
    pt: {
      brand:'Hospitalidade a bordo', experience:'Experiência de bordo', heroTitle:'Sua viagem,<br>no seu ritmo.',
      heroLead:'Bem-vindo. Este espaço foi pensado para que sua viagem seja tranquila, confortável e agradável.',
      messages:['Que o seu dia siga leve — e que esta viagem seja uma boa parte dele.','Que esta viagem traga alguns minutos de calma para o seu dia.','Conforto também está nos pequenos detalhes. Fique à vontade.'],
      personalize:'Personalizar minha viagem', optional:'Opcional • rápido • sem identificação', progress:'Sua experiência',
      comfortEyebrow:'Conforto', comfortTitle:'Como você prefere viajar?', comfortDesc:'Escolha apenas o que quiser. Se precisar de algo diferente, pode falar comigo a qualquer momento.',
      temperature:'Conforto térmico', oneChoice:'1 escolha', cooler:'Mais fresco', justRight:'Está ótimo', warmer:'Mais quente',
      sound:'Ambiente sonoro', instrumental:'Instrumental suave', mpb:'MPB tranquila', pop:'Pop leve', silence:'Prefiro silêncio',
      interaction:'Preferência de atendimento', talk:'Pode conversar', quiet:'Prefiro silêncio', comfortable:'Estou à vontade', help:'Preciso de ajuda',
      anythingElse:'Algo mais?', optionalSmall:'opcional', preferencePlaceholder:'Escreva aqui alguma preferência ou necessidade...', continue:'Continuar', skip:'Pular por enquanto',
      feedbackEyebrow:'Sua opinião', feedbackTitle:'Como está sendo sua experiência?', feedbackDesc:'Sua percepção me ajuda a melhorar. A avaliação é opcional e não pede identificação.',
      excellent:'Excelente', goodRating:'Boa', improve:'Pode melhorar', suggestion:'Sugestão', feedbackPlaceholder:'Atendimento, conforto, limpeza, veículo ou algum detalhe que eu possa melhorar.', finish:'Finalizar', skipEvaluation:'Continuar sem avaliar',
      thankYou:'Obrigado', finalTitle:'Desejo uma excelente viagem.', finalDesc:'Fique à vontade. Se precisar ajustar qualquer detalhe, pode falar comigo durante a viagem.',
      whatsappTitle:'Contato profissional pelo WhatsApp', whatsappDesc:'Disponível para contato futuro, quando apropriado.', restart:'Recomeçar',
      privacyTitle:'Privacidade por padrão.', privacyText:' Este protótipo não solicita nome, telefone, e-mail ou localização. Nesta primeira versão, as escolhas permanecem somente neste navegador.', footer:'Experiência independente de hospitalidade a bordo.'
    },
    en: {
      brand:'On-board hospitality', experience:'Ride experience', heroTitle:'Your ride,<br>at your pace.', heroLead:'Welcome. This space was designed to make your ride calm, comfortable and pleasant.',
      messages:['May your day feel a little lighter — and may this ride be a good part of it.','May these few minutes bring a little calm to your day.','Comfort lives in the small details. Please make yourself at ease.'],
      personalize:'Personalize my ride', optional:'Optional • quick • no identification', progress:'Your experience', comfortEyebrow:'Comfort', comfortTitle:'How would you like to travel?', comfortDesc:'Choose only what you want. If you need anything else, feel free to tell me at any time.',
      temperature:'Cabin temperature', oneChoice:'1 choice', cooler:'Cooler', justRight:'Just right', warmer:'Warmer', sound:'Sound environment', instrumental:'Soft instrumental', mpb:'Brazilian music', pop:'Light pop', silence:'I prefer silence',
      interaction:'Interaction preference', talk:'Conversation is welcome', quiet:'I prefer quiet', comfortable:'I am comfortable', help:'I need assistance', anythingElse:'Anything else?', optionalSmall:'optional', preferencePlaceholder:'Write any preference or need here...', continue:'Continue', skip:'Skip for now',
      feedbackEyebrow:'Your opinion', feedbackTitle:'How is your experience so far?', feedbackDesc:'Your feedback helps me improve. It is optional and does not ask for identification.', excellent:'Excellent', goodRating:'Good', improve:'Could improve', suggestion:'Suggestion', feedbackPlaceholder:'Service, comfort, cleanliness, vehicle condition, or anything I could improve.', finish:'Finish', skipEvaluation:'Continue without rating',
      thankYou:'Thank you', finalTitle:'I wish you an excellent ride.', finalDesc:'Please make yourself comfortable. If you need any adjustment, feel free to tell me during the ride.', whatsappTitle:'Professional contact via WhatsApp', whatsappDesc:'Available for future contact, when appropriate.', restart:'Start again',
      privacyTitle:'Privacy by default.', privacyText:' This prototype does not request your name, phone, email or location. In this first version, choices stay only in this browser.', footer:'Independent on-board hospitality experience.'
    },
    zh: {
      brand:'车内贴心服务', experience:'乘车体验', heroTitle:'按您的节奏，<br>享受旅程。', heroLead:'欢迎乘车。这个页面是为了让您的旅程更加安静、舒适和愉快。',
      messages:['愿您今天心情轻松，也愿这段旅程成为美好的一部分。','愿这几分钟的旅程，为您带来一点轻松与宁静。','舒适来自每一个小细节。请随意告诉我您的需要。'],
      personalize:'设置我的乘车偏好', optional:'自愿 • 快速 • 无需身份信息', progress:'您的乘车体验', comfortEyebrow:'舒适度', comfortTitle:'您希望怎样乘车？', comfortDesc:'只需选择您想调整的项目。如果还有其他需要，随时可以告诉我。',
      temperature:'车内温度', oneChoice:'选择一项', cooler:'凉一点', justRight:'现在正好', warmer:'暖一点', sound:'音乐环境', instrumental:'轻柔纯音乐', mpb:'巴西轻音乐', pop:'轻松流行音乐', silence:'我喜欢安静',
      interaction:'交流偏好', talk:'可以聊天', quiet:'我喜欢安静', comfortable:'现在很舒适', help:'我需要帮助', anythingElse:'还有其他需要吗？', optionalSmall:'可选', preferencePlaceholder:'可以在这里写下您的其他偏好或需要……', continue:'继续', skip:'暂时跳过',
      feedbackEyebrow:'您的意见', feedbackTitle:'这次乘车体验怎么样？', feedbackDesc:'您的反馈可以帮助我改进服务。评价完全自愿，也不需要提供身份信息。', excellent:'非常好', goodRating:'很好', improve:'可以改进', suggestion:'建议', feedbackPlaceholder:'您可以对服务、舒适度、清洁、车辆状况或其他方面提出建议。', finish:'完成', skipEvaluation:'不评价，继续',
      thankYou:'谢谢', finalTitle:'祝您旅途愉快。', finalDesc:'请放松乘车。如果需要调整任何细节，途中随时可以告诉我。', whatsappTitle:'WhatsApp 工作联系', whatsappDesc:'如有需要，可用于今后的工作联系。', restart:'重新开始',
      privacyTitle:'默认保护隐私。', privacyText:' 此原型不会要求姓名、电话、电子邮件或位置。在当前版本中，您的选择只保存在这个浏览器里。', footer:'独立的车内贴心服务体验。'
    }
  };

  const $ = (selector) => document.querySelector(selector);
  const $$ = (selector) => [...document.querySelectorAll(selector)];

  function setLanguage(lang) {
    if (!translations[lang]) lang = 'pt';
    state.lang = lang;
    localStorage.setItem('passenger-language', lang);
    document.documentElement.lang = lang === 'zh' ? 'zh-CN' : lang === 'en' ? 'en' : 'pt-BR';
    const t = translations[lang];
    $$('[data-i18n]').forEach(el => { const key = el.dataset.i18n; if (t[key] !== undefined) el.textContent = t[key]; });
    $$('[data-i18n-html]').forEach(el => { const key = el.dataset.i18nHtml; if (t[key] !== undefined) el.innerHTML = t[key]; });
    $$('[data-i18n-placeholder]').forEach(el => { const key = el.dataset.i18nPlaceholder; if (t[key] !== undefined) el.placeholder = t[key]; });
    $('#positive-message').textContent = t.messages[new Date().getDate() % t.messages.length];
  }

  function showStep(step) {
    state.step = step;
    $('#hero').classList.toggle('hidden', step > 0);
    $('#flow').classList.toggle('hidden', step === 0);
    [1,2,3].forEach(n => $(`#step-${n}`).classList.toggle('hidden', n !== step));
    if (step > 0) {
      $('#step-label').textContent = `${step} de 3`;
      $('#progress-fill').style.width = `${step * 33.333}%`;
      requestAnimationFrame(() => $(`#step-${step}`).scrollIntoView({behavior:'smooth', block:'start'}));
    } else {
      requestAnimationFrame(() => window.scrollTo({top:0, behavior:'smooth'}));
    }
  }

  function getSelectedMusicProfile() {
    const key = state.preferences.music;
    if (!key) return null;
    return config.musicProfiles?.[key] || { label: key, spotifyPlaylistUrl: '' };
  }

  function storeSession() {
    const data = {
      updatedAt: new Date().toISOString(),
      preferences: state.preferences,
      feedback: state.feedback,
      selectedMusicProfile: getSelectedMusicProfile()
    };
    sessionStorage.setItem('passenger-experience-session', JSON.stringify(data));
  }

  function setSingleSelection(container, selectedButton) {
    container.querySelectorAll('button').forEach(item => {
      const selected = item === selectedButton;
      item.classList.toggle('selected', selected);
      item.setAttribute('aria-pressed', selected ? 'true' : 'false');
    });
  }

  function setupChoices() {
    $$('.choice-group').forEach(group => {
      const key = group.dataset.group;
      group.querySelectorAll('.choice').forEach(button => {
        button.setAttribute('aria-pressed', 'false');
        button.addEventListener('click', () => {
          setSingleSelection(group, button);
          state.preferences[key] = button.dataset.value;
          storeSession();
        });
      });
    });
  }

  function setupRating() {
    $$('.rating button').forEach(button => {
      button.setAttribute('aria-pressed', 'false');
      button.addEventListener('click', () => {
        setSingleSelection($('.rating'), button);
        state.feedback.rating = Number(button.dataset.rating);
        storeSession();
      });
    });
  }

  function setupWhatsApp() {
    const link = $('#whatsapp');
    const number = String(config.whatsappNumber || '').replace(/\D/g, '');
    if (!number) return;
    const message = encodeURIComponent(config.whatsappMessage || 'Olá!');
    link.href = `https://wa.me/${number}?text=${message}`;
    link.classList.remove('hidden');
  }

  $('#language').addEventListener('change', event => setLanguage(event.target.value));
  $('#start').addEventListener('click', () => showStep(1));
  $('#save-preferences').addEventListener('click', () => {
    state.preferences.note = $('#preference-note').value.trim();
    storeSession();
    showStep(2);
  });
  $('#skip-preferences').addEventListener('click', () => showStep(2));
  $('#save-feedback').addEventListener('click', () => {
    state.feedback.note = $('#feedback-note').value.trim();
    storeSession();
    showStep(3);
  });
  $('#skip-feedback').addEventListener('click', () => { storeSession(); showStep(3); });
  $('#restart').addEventListener('click', () => {
    state.preferences = { temperature: null, music: null, interaction: null, note: '' };
    state.feedback = { rating: null, note: '' };
    $$('.selected').forEach(el => el.classList.remove('selected'));
    $$('[aria-pressed="true"]').forEach(el => el.setAttribute('aria-pressed', 'false'));
    $('#preference-note').value = '';
    $('#feedback-note').value = '';
    sessionStorage.removeItem('passenger-experience-session');
    showStep(0);
  });

  setupChoices();
  setupRating();
  setupWhatsApp();
  const savedLanguage = localStorage.getItem('passenger-language') || 'pt';
  $('#language').value = savedLanguage;
  setLanguage(savedLanguage);
})();
