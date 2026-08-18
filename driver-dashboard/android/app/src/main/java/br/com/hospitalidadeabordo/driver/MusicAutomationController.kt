package br.com.hospitalidadeabordo.driver

import android.content.Context
import android.util.Log

/**
 * Contrato único para a automação musical.
 *
 * O MVP Android já recebe o perfil escolhido e chama este controlador sem
 * exigir interação do motorista. O adaptador de reprodução (Spotify, Apple
 * Music ou outro player aprovado) será plugado aqui após o teste de
 * compatibilidade no tablet real.
 */
class MusicAutomationController(private val context: Context) {
    fun applyProfile(profile: String?) {
        if (profile.isNullOrBlank()) return
        if (!context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
                .getBoolean(MainActivity.KEY_MUSIC_AUTOMATION, true)) return

        context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_MUSIC_PROFILE, profile)
            .apply()

        Log.i("HospitalidadeMusic", "Perfil musical solicitado automaticamente: $profile")
    }

    companion object {
        const val KEY_LAST_MUSIC_PROFILE = "last_music_profile"
    }
}
