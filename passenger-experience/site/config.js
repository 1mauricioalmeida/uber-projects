window.PASSENGER_EXPERIENCE_CONFIG = {
  whatsappNumber: "",
  whatsappMessage: "Olá! Entrei em contato pela experiência de bordo.",
  brandInitial: "M",

  backend: {
    enabled: true,
    submitUrl: "https://vxoocvpcdxobintflwbi.supabase.co/functions/v1/submit-passenger-experience"
  },

  // Perfis musicais escolhidos pelo passageiro. Cada perfil poderá apontar
  // para uma playlist curada pelo motorista sem alterar a interface.
  musicProfiles: {
    instrumental: {
      label: "Instrumental suave",
      spotifyPlaylistUrl: ""
    },
    mpb: {
      label: "MPB tranquila",
      spotifyPlaylistUrl: ""
    },
    pop: {
      label: "Pop leve",
      spotifyPlaylistUrl: ""
    },
    silence: {
      label: "Prefiro silêncio",
      spotifyPlaylistUrl: ""
    }
  },

  // Playback automático fica desligado por padrão. A eventual ativação
  // depende da autorização OAuth da conta e da conformidade com as regras
  // vigentes da Spotify Platform para uso comercial.
  spotify: {
    enabled: false,
    playbackAutomationEnabled: false
  }
};
